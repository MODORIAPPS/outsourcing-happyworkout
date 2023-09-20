package com.happy.workout

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

class HappyWorkout : Application(), ViewModelStoreOwner {

    private lateinit var viewModelStore: ViewModelStore

    override fun onCreate() {
        super.onCreate()
        viewModelStore = ViewModelStore()
    }

    override fun getViewModelStore(): ViewModelStore {
        return viewModelStore
    }
}