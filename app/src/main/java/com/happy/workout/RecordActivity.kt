package com.happy.workout

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.happy.workout.databinding.ActivityRecordBinding
import com.happy.workout.model.LocalRecordListItem
import com.happy.workout.utils.AuthManager
import com.happy.workout.viewmodel.UserViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class RecordActivity : AppCompatActivity() {

    private lateinit var selectedImageUri: Uri
    private lateinit var binding: ActivityRecordBinding
    private lateinit var userViewModel: UserViewModel

    private val storageReference = FirebaseStorage.getInstance().reference
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var walkImageUrl = ""

    private var isUploading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        userViewModel =
            ViewModelProvider(this.application as HappyWorkout)[UserViewModel::class.java]

        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)

        binding.selectImageButton.setOnClickListener {
            openGallery()
        }

        binding.saveButton.setOnClickListener {
            if(isUploading){
                Toast.makeText(this, "이미지 업로드 중입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (walkImageUrl == "" && binding.descriptionEditText.text.length < 2) {
                Toast.makeText(this, "이미지와 설명을 모두 추가해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            AlertDialog.Builder(this)
                .setTitle("새 게시물 작성하기")
                .setMessage("게시물을 작성하면 모든 사람이 게시물을 볼 수 있게 됩니다. 계속하겠습니까?")
                .setPositiveButton("네") { _, _ ->
                    binding.saveButton.text = "저장중..."
                    saveRecord()
                }
                .setNegativeButton("아니오") { _, _ -> }
                .show()
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
                    .addOnSuccessListener { taskSnapshot ->
                        isUploading = true
                        imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                            isUploading = false

                            walkImageUrl = imageUrl.toString()
                            Glide.with(this)
                                .load(imageUrl)
                                .into(binding.imageView)
                        }
                    }
                    .addOnFailureListener {
                        // 이미지 업로드 실패
                        isUploading = false
                        Toast.makeText(this, "이미지 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun saveRecord() {
        val userId = userViewModel.user.value?.uid ?: ""
        val description = binding.descriptionEditText.text.toString()

        fusedLocationClient.lastLocation.addOnSuccessListener {
            if (it == null) {
                // 위치 정보를 가져오지 못한 경우
                // 위치 권한이 부여되지 않거나, 위치가 사용할 수 없는 상황일 수 있음
                Toast.makeText(this, "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            // Firestore에 데이터 저장
            val recordData = LocalRecordListItem(
                userId,
                walkImageUrl,
                userViewModel.user.value?.nickname ?: "",
                description,
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                GeoPoint(it.latitude, it.longitude), // 위치 정보 추가,
                Timestamp.now()
            )

            firestore.collection("records")
                .add(recordData)
                .addOnSuccessListener {
                    Toast.makeText(this, "기록이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "기록 저장에 실패했습니다.", Toast.LENGTH_SHORT)
                        .show()
                }
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