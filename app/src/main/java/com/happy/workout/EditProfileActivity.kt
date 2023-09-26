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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.happy.workout.databinding.ActivityEditProfileBinding
import com.happy.workout.model.LocalRecordListItem
import com.happy.workout.model.User
import com.happy.workout.viewmodel.UserViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class EditProfileActivity : AppCompatActivity() {

    private lateinit var selectedImageUri: Uri
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var userViewModel: UserViewModel

    private val storageReference = FirebaseStorage.getInstance().reference
    private val firestore = FirebaseFirestore.getInstance()

    private var profileImageUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userViewModel =
            ViewModelProvider(this.application as HappyWorkout)[UserViewModel::class.java]
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("EditProfileActivity", "user: ${userViewModel.user.value}")
        // init
        Glide.with(this)
            .load(userViewModel.user.value?.profileImageUrl)
            .into(binding.profileImageView)
        binding.nicknameEditText.setText(userViewModel.user.value?.nickname)

        binding.selectImageButton.setOnClickListener { openGallery() }
        binding.saveButton.setOnClickListener {
            if (binding.nicknameEditText.text.length < 2) {
                Toast.makeText(this, "닉네임은 2자 이상 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.saveButton.text = "저장중..."
            saveProfile()
        }

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
                    .addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                            profileImageUrl = imageUrl.toString()
                            Glide.with(this)
                                .load(selectedImageUri)
                                .into(binding.profileImageView)
                        }
                    }
            }
        }
    }

    private fun saveProfile() {

        val firebaseUid = userViewModel.user.value?.firebaseUid ?: ""
        val userId = userViewModel.user.value?.uid ?: ""

        val usersRef = firestore.collection("users").document(userId)
        val nickname = binding.nicknameEditText.text.toString()

        val updates = hashMapOf<String, Any>(
            "nickname" to nickname,
            "profileImageUrl" to profileImageUrl,
        )
        usersRef
            .update(updates)
            .addOnSuccessListener {
                val user = User(
                    firebaseUid,
                    userId,
                    nickname,
                    Timestamp.now(),
                    Timestamp.now(),
                    profileImageUrl
                )
                userViewModel.user.postValue(user)
                Toast.makeText(this, "프로필이 저장되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Log.d("EditProfileActivity", "Error writing document", it)
                Toast.makeText(this, "프로필 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        private const val IMAGE_PICK_REQUEST = 123
    }
}