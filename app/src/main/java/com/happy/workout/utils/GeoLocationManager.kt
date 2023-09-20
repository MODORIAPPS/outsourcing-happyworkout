package com.happy.workout.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log

object GeoLocationManager {

    private val TAG = "GeoLocationManager"

    fun fetchLocationDetails(context: Context, latitude: Double, longitude: Double): String? {
        val geocoder = Geocoder(context)
        val addresses: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

        if (addresses != null) {
            if (addresses.isNotEmpty()) {
                val address = addresses[0]
                val city = address.locality // 도시 (시 또는 군)
                val subLocality = address.subLocality // 구
                val thoroughfare = address.thoroughfare // 동

                Log.d(TAG, "ADDRESS: $address")

                val locationText = "도시: $city\n구: $subLocality\n동: $thoroughfare"
                Log.d(TAG, "LOCATION: $locationText")

                val hood = identifyNeighborhood(address)
                Log.d(TAG, "NEIGHBORHOOD: $hood")
                return hood
            }
            return null
        }

        return null
    }

    fun identifyNeighborhood(address: Address): String {
        // 주소 정보에서 동네 또는 지역 이름 추출
        val neighborhood = address.subLocality ?: ""
        val city = address.locality ?: ""

        return if (neighborhood.isNotEmpty()) {
            // 동네 정보가 있는 경우
            neighborhood
        } else if (city.isNotEmpty()) {
            // 동네 정보가 없는 경우 도시 정보 반환
            city
        } else {
            // 도시 정보도 없는 경우 알 수 없음을 반환
            "알 수 없는 위치"
        }
    }
}