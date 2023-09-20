package com.happy.workout.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class User (
    val firebaseUid: String,
    val uid: String,
    val nickname: String,
    val createdAt: Timestamp,
    val updatedAt: Timestamp,
    val profileImageUrl: String?,
)