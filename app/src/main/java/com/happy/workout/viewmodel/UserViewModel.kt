package com.happy.workout.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.happy.workout.model.User

class UserViewModel: ViewModel() {
    var user: MutableLiveData<User> = MutableLiveData()
}