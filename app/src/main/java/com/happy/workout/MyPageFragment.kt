package com.happy.workout

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.happy.workout.databinding.FragmentMyPageBinding
import com.happy.workout.utils.AuthManager
import com.happy.workout.viewmodel.UserViewModel

class MyPageFragment : Fragment() {

    private lateinit var binding: FragmentMyPageBinding
    private val userViewModel: UserViewModel by lazy {
        ViewModelProvider(requireActivity().application as HappyWorkout)[UserViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyPageBinding.inflate(layoutInflater)

        binding.loginButton.setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }

        binding.profileSettingButton.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.deleteUserButton.setOnClickListener {
            deleteUser()
        }

        binding.privacyItem.setOnClickListener {
            startActivity(Intent(requireContext(), PrivacyPolicyWebViewActivity::class.java))
        }

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                Log.d("MyPageFragment", "user: $user")
                binding.deleteUserButton.visibility = View.VISIBLE
                binding.loginButton.visibility = View.GONE
                binding.profileContainer.visibility = View.VISIBLE
                binding.nicknameTextView.text = user.nickname
                Glide.with(binding.profileImageView)
                    .load(user.profileImageUrl)
                    .circleCrop()
                    .into(binding.profileImageView)
            } else {
                binding.loginButton.visibility = View.VISIBLE
                binding.profileContainer.visibility = View.GONE
                binding.nicknameTextView.text = ""
            }
        }

        binding.logoutItem.setOnClickListener {
            val method = AuthManager.loadLoginMethod(requireContext())
            if (method == "email") {
                Firebase.auth.signOut()
                return@setOnClickListener
            }
            userViewModel.user.postValue(null)
            AuthManager.clearLoginMethod(requireContext())
        }

        return binding.root
    }

    private fun deleteUser () {
        val method = AuthManager.loadLoginMethod(requireContext())
        if(method == "email"){
            val user = Firebase.auth.currentUser
            user?.delete()
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "회원탈퇴가 완료되었습니다", Toast.LENGTH_SHORT).show()
                        userViewModel.user.postValue(null)
                    }
                }
        }else {
            // delete user from firebase firestore
            Firebase.firestore.collection("users").document(userViewModel.user.value!!.uid).delete().addOnCompleteListener {
                Toast.makeText(requireContext(), "회원탈퇴가 완료되었습니다", Toast.LENGTH_SHORT).show()
                userViewModel.user.postValue(null)
            }
        }


        AuthManager.clearLoginMethod(requireContext())

    }

}