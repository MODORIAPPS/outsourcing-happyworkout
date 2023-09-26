package com.happy.workout.model

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.type.DateTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class LocalRecordListItem(
    val userId: String,
    val imageUrl: String,
    val title: String,
    val description: String,
    val date: String,
    val location: GeoPoint,
    val createdAt: Timestamp
){
    fun readOnDate(targetDate: LocalDate): Boolean {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date1 = LocalDate.parse(date, formatter)
        Log.d("LocalRecordListItem", "date1: $date1, targetDate: $targetDate")
        return date1.isEqual(targetDate)
    }
}
