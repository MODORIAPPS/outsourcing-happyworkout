package com.happy.workout

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebSettings
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.happy.workout.databinding.ActivityChatWebViewBinding
import com.happy.workout.utils.GeoLocationManager
import com.happy.workout.utils.HappyWorkoutJavaScriptInterface
import com.happy.workout.viewmodel.UserViewModel

class ChatWebViewActivity : AppCompatActivity() {

    private val TAG = "ChatWebViewActivity"
    val userViewModel by lazy {
        ViewModelProvider(this.application as HappyWorkout)[UserViewModel::class.java]
    }

    val binding: ActivityChatWebViewBinding by lazy {
        ActivityChatWebViewBinding.inflate(layoutInflater)
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val user = userViewModel.user.value
        if (user == null) {
            finish()
            return
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                // 위치 정보를 사용하여 구와 동 정보 추출
                val hood = GeoLocationManager.fetchLocationDetails(
                    this,
                    location.latitude,
                    location.longitude
                )
                if(hood == null) {
                    finish()
                    return@addOnSuccessListener
                }
                binding.webview.addJavascriptInterface(
                    HappyWorkoutJavaScriptInterface(this, user.uid, hood),
                    "HappyWorkout"
                )
                binding.webview.loadUrl("${Const.BASE_URL}/chat/${hood}")
                Log.d(TAG, "onCreate: ${user.firebaseUid}"  )

            }

        val settings = binding.webview.settings
        settings.javaScriptEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.builtInZoomControls = false
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        settings.domStorageEnabled = true
        settings.setSupportMultipleWindows(false)
        settings.setSupportZoom(false)
    }
}