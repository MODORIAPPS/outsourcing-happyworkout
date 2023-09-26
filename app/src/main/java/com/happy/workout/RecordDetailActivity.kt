package com.happy.workout

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.happy.workout.databinding.ActivityRecordDetailBinding
import com.happy.workout.viewmodel.UserViewModel

class RecordDetailActivity : AppCompatActivity() {

    private val TAG = "RecordDetailActivity"
    private val binding by lazy { ActivityRecordDetailBinding.inflate(layoutInflater) }

    private lateinit var recordId: String
    private val userViewModel by lazy {
        ViewModelProvider(this.application as HappyWorkout)[UserViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val userId = intent.getStringExtra("userId")
        val date = intent.getStringExtra("date")
        val currentUserId = userViewModel.user.value?.uid

        if (userId == null || date == null || currentUserId == null) finish()

        Log.d(TAG, "userId: $userId, date: $date")

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("records")
            .whereEqualTo("userId", userId)
            .whereEqualTo("date", date)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(this, "존재하지 않는 기록입니다", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    recordId = result.documents[0].id
                    result.documents[0].getString("description")
                        ?.let { binding.descriptionEditText.setText(it) }
                    result.documents[0].getString("imageUrl")?.let {
                        Glide.with(this)
                            .load(it)
                            .into(binding.imageView)
                    }

                    val recordUserId = result.documents[0].getString("userId")
                    if (currentUserId == recordUserId) binding.deleteButton.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

        binding.deleteButton.setOnClickListener {
            binding.deleteButton.text = "삭제중..."
            firestore.collection("records")
                .document(recordId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "기록이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents.", exception)
                }
        }

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

}