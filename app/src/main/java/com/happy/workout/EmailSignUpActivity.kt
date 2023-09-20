package com.happy.workout

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.happy.workout.databinding.ActivityEmailSignUpBinding
import com.happy.workout.model.User
import com.happy.workout.utils.AuthManager
import com.happy.workout.viewmodel.UserViewModel
import java.util.UUID

class EmailSignUpActivity : AppCompatActivity() {

    private lateinit var selectedImageUri: Uri
    private val TAG = "EmailSignUpActivity"

    private lateinit var auth: FirebaseAuth

    val binding: ActivityEmailSignUpBinding by lazy {
        ActivityEmailSignUpBinding.inflate(layoutInflater)
    }

    private val storageReference = FirebaseStorage.getInstance().reference
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var userViewModel: UserViewModel

    private var profileImageUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth

        userViewModel =
            ViewModelProvider(this.application as HappyWorkout)[UserViewModel::class.java]

        binding.selectImageButton.setOnClickListener { openGallery() }
        binding.signUpBtn.setOnClickListener {
            val nickname = binding.nicknameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val passwordConfirm = binding.passwordConfirmEditText.text.toString()
            var profileImageUrl = ""

            if (password != passwordConfirm) {
                // 비밀번호가 일치하지 않는 경우
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.signUpBtn.text = "회원가입 중..."
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = auth.currentUser
                        val usersRef = firestore.collection("users").document()
                        val id = usersRef.id
                        val mUser = User(
                            firebaseUid = user?.uid ?: "",
                            uid = id ?: "",
                            nickname = nickname,
                            createdAt = Timestamp.now(),
                            updatedAt = Timestamp.now(),
                            profileImageUrl = profileImageUrl,
                        )

                        usersRef.set(mUser).addOnSuccessListener {
                            userViewModel.user.postValue(mUser)
                            AuthManager.saveLoginMethod(
                                this,
                                AuthManager.LOGIN_METHOD_EMAIL
                            )
                            Toast.makeText(
                                baseContext,
                                "${mUser.nickname}님 안녕하세요!",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "회원가입에 실패했습니다.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }

        userViewModel.user.observe(this) { user ->
            if (user != null) {
                finish()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_REQUEST && resultCode == RESULT_OK) {
            data?.data?.let {
                selectedImageUri = it
                val imageRef = storageReference.child("record_images/${UUID.randomUUID()}")
                imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener { taskSnapshot ->
                        imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                            Glide.with(this)
                                .load(imageUrl)
                                .into(binding.profileImageView)
                        }

                    }
                    .addOnFailureListener {
                        // 이미지 업로드 실패
                        Toast.makeText(this, "이미지 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    companion object {
        private const val IMAGE_PICK_REQUEST = 123
    }
}