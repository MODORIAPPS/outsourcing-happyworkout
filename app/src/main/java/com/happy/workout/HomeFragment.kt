package com.happy.workout

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.happy.workout.adapter.AdsBannerViewPagerAdapter
import com.happy.workout.adapter.LocalRecordRecyclerViewAdapter
import com.happy.workout.databinding.FragmentHomeBinding
import com.happy.workout.model.AdsBannerItem
import com.happy.workout.model.LocalRecordListItem
import com.happy.workout.viewmodel.UserViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Timer
import java.util.TimerTask


class HomeFragment : Fragment() {

    private val TAG = "HomeFragment"

    private lateinit var localAdapter: LocalRecordRecyclerViewAdapter
    private lateinit var binding: FragmentHomeBinding
    private lateinit var userViewModel: UserViewModel

    private var currentPage = 0
    private var timer: Timer? = null

    private val items = listOf(
        AdsBannerItem(
            "https://upload.wikimedia.org/wikipedia/commons/thumb/c/ca/Siberian-husky.jpg/280px-Siberian-husky.jpg",
            "허스키",
            "https://myaccount.google.com/?hl=ko&utm_source=OGB&utm_medium=act&pli=1"
        ),
        AdsBannerItem(
            "https://media.istockphoto.com/id/1214044812/photo/mame-shibainu.jpg?s=612x612&w=0&k=20&c=QkfIZX_0-EtMjJ5CPfaJTX9Pa0mpFwWW2cJ-cdaFZyQ=",
            "시바견",
            "https://myaccount.google.com/?hl=ko&utm_source=OGB&utm_medium=act&pli=1"
        ),
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val firestore = FirebaseFirestore.getInstance()

        userViewModel =
            ViewModelProvider(requireActivity().application as HappyWorkout)[UserViewModel::class.java]

        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater)

        // AdsBanner Setup
        val adapter = AdsBannerViewPagerAdapter(requireContext(), items)
        binding.adsViewPager.adapter = adapter

        // 광고 이미지 라운드 처리
        // adCardView.radius = resources.getDimension(R.dimen.card_corner_radius)

        // ViewPager 페이지 변경 리스너 설정
        binding.adsViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPage = position
            }
        })

        // 5초마다 다음 이미지로 자동 전환
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                requireActivity().runOnUiThread {
                    if (currentPage == items.size - 1) {
                        currentPage = 0
                    } else {
                        currentPage++
                    }
                    binding.adsViewPager.currentItem = currentPage
                }
            }
        }, 0, 10000)

        binding.loginButton.setOnClickListener {
            startActivity(android.content.Intent(requireContext(), LoginActivity::class.java))
        }

        // LocalRecord Setup
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.loginButton.visibility = View.GONE
                binding.listContainer.visibility = View.VISIBLE
                val list = mutableListOf<LocalRecordListItem>()
                firestore.collection("records")
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            val data = document.data
                            val item = LocalRecordListItem(
                                data["userId"] as String,
                                data["imageUrl"] as String,
                                data["title"] as String,
                                data["description"] as String,
                                data["date"] as String,
                                data["location"] as GeoPoint,
                                data["createdAt"] as Timestamp,
                            )
                            list.add(item)
                        }

                        Log.d(TAG, "list: $list")
                        localAdapter = LocalRecordRecyclerViewAdapter(requireContext(), list)
                        binding.locationsRecyclerView.layoutManager =
                            LinearLayoutManager(requireContext())
                        binding.locationsRecyclerView.adapter = localAdapter
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error getting documents.", exception)
                    }
            } else {
                binding.loginButton.visibility = View.VISIBLE
                binding.listContainer.visibility = View.GONE
            }
        }


        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}