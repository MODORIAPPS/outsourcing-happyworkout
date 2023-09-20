package com.happy.workout.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.happy.workout.model.LocalRecordListItem
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RecordViewModel : ViewModel() {

    val TAG = "RecordViewModel"

    private val firestore = Firebase.firestore
    private val recordData = MutableLiveData<List<LocalRecordListItem>>()

    fun getRecordData(): LiveData<List<LocalRecordListItem>> = recordData

    @OptIn(DelicateCoroutinesApi::class)
    fun fetchRecordData(userId: String) {
        Log.d(TAG, "fetchRecordData")
        val list = mutableListOf<LocalRecordListItem>()
        GlobalScope.launch(Dispatchers.IO) {
            val query =
                firestore.collection("records")
                    .whereEqualTo("userId", userId)
            query.get().addOnSuccessListener { documents ->
                for (document in documents) {
                    val data = document.data
                    val record = LocalRecordListItem(
                        userId = data["userId"] as String,
                        imageUrl = data["imageUrl"] as String,
                        title = data["title"] as String,
                        description = data["description"] as String,
                        date = data["date"] as String,
                        location = data["location"] as com.google.firebase.firestore.GeoPoint,
                        createdAt = data["createdAt"] as Timestamp,
                    )
                    Log.d(TAG, "record: $record")
                    list.add(record)
                }

                recordData.postValue(list)
            }.addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
        }
    }
}
