package com.happy.workout

import android.content.Intent
import android.media.MediaPlayer.OnVideoSizeChangedListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.happy.workout.databinding.ActivityLoginBinding
import com.happy.workout.databinding.ActivityMainBinding
import com.happy.workout.model.User
import com.happy.workout.utils.AuthManager
import com.happy.workout.viewmodel.UserViewModel
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient

data class FirebaseUserDTO(
    val uid: String,
    val nickname: String,
    val createdAt: Timestamp,
    val updatedAt: Timestamp,
    val profileImageUrl: String?
)

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    val userViewModel by lazy {
        ViewModelProvider(this.application as HappyWorkout)[UserViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth

        val firestore = FirebaseFirestore.getInstance()

        binding.signUpButton.setOnClickListener {
            startActivity(
                Intent(
                    this, EmailSignUpActivity::class.java
                )
            )
        }

        // 카카오톡으로 로그인
        binding.kakaoLoginBtn.setOnClickListener {
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
                                TAG,
                                "사용자 정보 요청 성공" + "\n회원번호: ${user.id}" + "\n이메일: ${user.kakaoAccount?.email}" + "\n닉네임: ${user.kakaoAccount?.profile?.nickname}" + "\n프로필사진: ${user.kakaoAccount?.profile?.thumbnailImageUrl}"
                            )

                            val mUser = FirebaseUserDTO(
                                user.id.toString(),
                                user.kakaoAccount?.profile?.nickname.toString(),
                                Timestamp.now(),
                                Timestamp.now(),
                                "",
                            )

                            // firestore에 이미 있는지 체크
                            firestore.collection("users").whereEqualTo("uid", user.id.toString())
                                .get().addOnSuccessListener { documents ->
                                    if (documents.size() > 0) {
                                        userViewModel.user.value = User(
                                            documents.documents[0].getString("firebaseUid")
                                                .toString(),
                                            user.id.toString(),
                                            user.kakaoAccount?.profile?.nickname.toString(),
                                            Timestamp.now(),
                                            Timestamp.now(),
                                            user.kakaoAccount?.profile?.thumbnailImageUrl
                                        )
                                        Toast.makeText(
                                            this, "${mUser.nickname}님 안녕하세요!", Toast.LENGTH_SHORT
                                        ).show()
                                        AuthManager.saveLoginMethod(
                                            this, AuthManager.LOGIN_METHOD_KAKAO
                                        )
                                    } else {
                                        val usersRef = firestore.collection("users").document()
                                        val firebaseUid = usersRef.id
                                        usersRef.set(mUser).addOnSuccessListener {
                                                Toast.makeText(
                                                    this,
                                                    "${mUser.nickname}님 환영합니다!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                userViewModel.user.value = User(
                                                    firebaseUid,
                                                    user.id.toString(),
                                                    user.kakaoAccount?.profile?.nickname.toString(),
                                                    Timestamp.now(),
                                                    Timestamp.now(),
                                                    user.kakaoAccount?.profile?.thumbnailImageUrl
                                                )
                                                Log.d(TAG, "DocumentSnapshot successfully written!")
                                                AuthManager.saveLoginMethod(
                                                    this, AuthManager.LOGIN_METHOD_KAKAO
                                                )
                                            }.addOnFailureListener { e ->
                                                Log.w(TAG, "Error writing document", e)
                                            }
                                    }
                                }
                        }
                    }
                }

            }
        }

        binding.emailLoginBtn.setOnClickListener {
            binding.loginButtonContainer.visibility = ViewGroup.GONE
            binding.emailLoginContainer.visibility = ViewGroup.VISIBLE
        }

        binding.emailSignInBtn.setOnClickListener { emailSignIn() }

        userViewModel.user.observe(this) {
            if (it != null) {
                finish()
            }
        }
    }

    private fun emailSignIn() {
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "이메일과 비밀번호를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    val mUser = User(
                        user?.uid ?: "",
                        user?.uid.toString(),
                        user?.email.toString(),
                        Timestamp.now(),
                        Timestamp.now(),
                        null
                    )
                    userViewModel.user.postValue(mUser)
                    AuthManager.saveLoginMethod(this, AuthManager.LOGIN_METHOD_EMAIL)
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "이메일, 또는 패스워드를 다시 확인해주세요.",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
    }
}