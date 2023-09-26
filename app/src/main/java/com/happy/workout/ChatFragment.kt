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
import android.webkit.WebSettings
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
import com.happy.workout.utils.HappyWorkoutJavaScriptInterface
import com.happy.workout.viewmodel.UserViewModel


class ChatFragment : Fragment() {

    private val TAG = "ChatFragment"
    private lateinit var context: Context

    val mHood: MutableLiveData<String?> = MutableLiveData(null)

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    private val binding by lazy { FragmentChatBinding.inflate(layoutInflater) }

    private lateinit var userViewModel: UserViewModel

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        userViewModel =
            ViewModelProvider(requireActivity().application as HappyWorkout)[UserViewModel::class.java]

        if (userViewModel.user.value == null) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            return binding.root
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        val settings = binding.chatWebView.settings
        settings.javaScriptEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.builtInZoomControls = false
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        settings.domStorageEnabled = true
        settings.setSupportMultipleWindows(false)
        settings.setSupportZoom(false)

        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                // 위치 정보를 사용하여 구와 동 정보 추출
                binding.animContainer.visibility = View.GONE
                val hood = GeoLocationManager.fetchLocationDetails(
                    context,
                    location.latitude,
                    location.longitude
                ) ?: return@addOnSuccessListener

                if(activity == null) return@addOnSuccessListener

                binding.chatWebView.addJavascriptInterface(
                    HappyWorkoutJavaScriptInterface(
                        requireActivity(),
                        userViewModel.user.value!!.uid,
                        hood
                    ),
                    "HappyWorkout"
                )
                binding.chatWebView.loadUrl("${Const.BASE_URL}/chat/${hood}")
                Log.d(TAG, "onCreate: ${userViewModel.user.value!!.firebaseUid}")
            }

        return binding.root
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}