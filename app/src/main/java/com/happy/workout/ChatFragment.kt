package com.happy.workout

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.happy.workout.databinding.ActivityRecordDetailBinding
import com.happy.workout.databinding.FragmentChatBinding
import com.happy.workout.utils.GeoLocationManager
import com.happy.workout.viewmodel.UserViewModel


class ChatFragment : Fragment() {

    private lateinit var context: Context

    val mHood: MutableLiveData<String?> = MutableLiveData(null)

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    private val TAG = "ChatFragment"
    private val binding by lazy { FragmentChatBinding.inflate(layoutInflater) }

    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        userViewModel =
            ViewModelProvider(requireActivity().application as HappyWorkout)[UserViewModel::class.java]
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        fetchCurrentLocation()

        mHood.observe(viewLifecycleOwner) {
            if (it != null) {
                Log.d(TAG, "onCreateView: $it")
                binding.chatroomItem.visibility = View.VISIBLE
                binding.myLocationTextView.visibility = View.VISIBLE
                binding.chatroomNameTextView.text = "$it 사람들과 이야기하기"
                binding.myLocationTextView.text = "$it 대화해요!"
            }
        }

        binding.chatroomItem.setOnClickListener {
            if (userViewModel.user.value == null) {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            } else {
                val intent = Intent(requireContext(), ChatWebViewActivity::class.java)
                intent.putExtra("hood", mHood.value)
                startActivity(intent)
            }
        }

        return binding.root
    }

    @SuppressLint("MissingPermission")
    private fun fetchCurrentLocation() {
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location == null) {
                    return@addOnSuccessListener
                }
                // 위치 정보를 사용하여 구와 동 정보 추출
                val hood = GeoLocationManager.fetchLocationDetails(
                    context,
                    location.latitude,
                    location.longitude
                )
                mHood.postValue(hood)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}