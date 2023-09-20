package com.happy.workout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import com.happy.workout.databinding.ActivityPrivacyPolicyWebViewBinding
import com.happy.workout.utils.HappyWorkoutJavaScriptInterface

class PrivacyPolicyWebViewActivity : AppCompatActivity() {

    val binding: ActivityPrivacyPolicyWebViewBinding by lazy {
        ActivityPrivacyPolicyWebViewBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val settings = binding.policyWebView.settings
        settings.javaScriptEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.builtInZoomControls = false
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        settings.domStorageEnabled = true
        settings.setSupportMultipleWindows(false)
        settings.setSupportZoom(false)
        binding.policyWebView.addJavascriptInterface(
            HappyWorkoutJavaScriptInterface(this, "", ""),
            "HappyWorkout"
        )
        binding.policyWebView.loadUrl("${Const.BASE_URL}/policy")
    }
}