package com.happy.workout

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.Manifest
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.happy.workout.databinding.ActivityMainBinding
import com.happy.workout.model.AdsBannerItem
import com.happy.workout.model.User
import com.happy.workout.utils.AuthManager
import com.happy.workout.viewmodel.UserViewModel
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding

    private val LOCATION_PERMISSION_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        KakaoSdk.init(this, "b60ba8e2a5a004b17a7e349209c3ea24")
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        loadAuthState()

        // BottomNavigationView Setup
        binding.bottomNav.setOnNavigationItemSelectedListener(this)
        supportFragmentManager.beginTransaction().add(R.id.linearLayout, HomeFragment()).commit()

        checkLocationPermission()
    }

    private fun loadAuthState() {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val userViewModel =
            ViewModelProvider(this.application as HappyWorkout)[UserViewModel::class.java]

        val loginMethod = AuthManager.loadLoginMethod(this)
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        if (loginMethod == "email" && auth.currentUser != null) {
            firestore.collection("users")
                .whereEqualTo("firebaseUid", auth.currentUser!!.uid)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.size() > 0) {
                        userViewModel.user.value = User(
                            documents.documents[0].getString("firebaseUid").toString(),
                            documents.documents[0].getString("uid").toString(),
                            documents.documents[0].getString("nickname").toString(),
                            Timestamp.now(),
                            Timestamp.now(),
                            documents.documents[0].getString("profileImageUrl").toString(),
                        )
                        AuthManager.saveLoginMethod(
                            this,
                            AuthManager.LOGIN_METHOD_KAKAO
                        )
                    } else {
                        startActivity(
                            android.content.Intent(
                                this,
                                LoginActivity::class.java
                            )
                        )
                    }
                }
            return
        }



        if (loginMethod == "kakao") {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Log.e(TAG, "로그인 실패", error)
                } else if (token != null) {
                    Log.i(TAG, "로그인 성공 ${token.accessToken}")
                    // 사용자 정보 요청 (기본)
                    UserApiClient.instance.me { user, error ->
                        if (error != null) {
                            Log.e(TAG, "사용자 정보 요청 실패", error)
                        } else if (user != null) {
                            Log.i(
                                TAG, "사용자 정보 요청 성공" +
                                        "\n회원번호: ${user.id}" +
                                        "\n이메일: ${user.kakaoAccount?.email}" +
                                        "\n닉네임: ${user.kakaoAccount?.profile?.nickname}" +
                                        "\n프로필사진: ${user.kakaoAccount?.profile?.thumbnailImageUrl}"
                            )

                            // firestore에 이미 있는지 체크
                            firestore.collection("users")
                                .whereEqualTo("uid", user.id.toString())
                                .get()
                                .addOnSuccessListener { documents ->
                                    if (documents.size() > 0) {
                                        userViewModel.user.value = User(
                                            documents.documents[0].getString("firebaseUid")
                                                .toString(),
                                            user.id.toString(),
                                            documents.documents[0].getString("nickname").toString(),
                                            Timestamp.now(),
                                            Timestamp.now(),
                                            documents.documents[0].getString("profileImageUrl")
                                                .toString(),
                                        )
                                        AuthManager.saveLoginMethod(
                                            this,
                                            AuthManager.LOGIN_METHOD_KAKAO
                                        )
                                    } else {
                                        startActivity(
                                            android.content.Intent(
                                                this,
                                                LoginActivity::class.java
                                            )
                                        )
                                    }
                                }
                        }
                    }
                }

            }
        }

    }

    private fun checkLocationPermission() {
        // 위치 권한이 이미 허용되었는지 확인
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // 위치 권한이 이미 허용된 상태
            // 위치 정보를 사용하는 코드 추가
        } else {
            // 위치 권한 요청
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        }
    }

    // 위치 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 위치 권한이 허용됨
                // 위치 정보를 사용하는 코드 추가
            } else {
                // 위치 권한이 거부됨
                // 사용자에게 권한이 필요하다고 알릴 수 있음
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.page_home -> {
                supportFragmentManager.beginTransaction().replace(R.id.linearLayout, HomeFragment())
                    .commitAllowingStateLoss()
                return true
            }

            R.id.page_calendar -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.linearLayout, CalendarFragment()).commitAllowingStateLoss()
                return true
            }

            R.id.page_chat -> {
                supportFragmentManager.beginTransaction().replace(R.id.linearLayout, ChatFragment())
                    .commitAllowingStateLoss()
                return true
            }

            R.id.page_mypage -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.linearLayout, MyPageFragment()).commitAllowingStateLoss()
                return true
            }
        }

        return false
    }
}