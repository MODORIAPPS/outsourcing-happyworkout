package com.happy.workout

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.happy.workout.databinding.FragmentCalendarBinding
import com.happy.workout.databinding.FragmentHomeBinding
import com.happy.workout.viewmodel.RecordViewModel
import com.happy.workout.viewmodel.UserViewModel
import com.kakao.sdk.user.UserApiClient
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


class CalendarFragment : Fragment() {

    private val TAG = "CalendarFragment"

    private lateinit var binding: FragmentCalendarBinding
    private lateinit var recordBookModal: RecordViewModel
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentCalendarBinding.inflate(layoutInflater)

        recordBookModal = activity?.run {
            ViewModelProvider(this)[RecordViewModel::class.java]
        } ?: throw Exception("Invalid Activity")
        userViewModel =
            ViewModelProvider(requireActivity().application as HappyWorkout)[UserViewModel::class.java]

        setupDayBinder()
        setupMonthBinder()

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)
        val endMonth = currentMonth.plusMonths(100)
        val firstDayOfWeek = firstDayOfWeekFromLocale()
        val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.SUNDAY)

        binding.calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        binding.calendarView.scrollToMonth(currentMonth)

        binding.calendarView.monthScrollListener = { month ->

            val yearMonth = month.yearMonth
            val year = yearMonth.year
            val monthKo = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.KOREA)

            val title = "${year}년 $monthKo"
            binding.monthYearTextView.text = title
        }

        recordBookModal.getRecordData().observe(viewLifecycleOwner) { recordList ->
            updateDayBinder()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val userId = userViewModel.user.value?.uid ?: return
        recordBookModal.fetchRecordData(userId)
    }

    private fun setupDayBinder() {
        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            @RequiresApi(Build.VERSION_CODES.O)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.textView.text = data.date.dayOfMonth.toString()
            }
        }
    }

    private fun setupMonthBinder() {
        binding.calendarView.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)

                @RequiresApi(Build.VERSION_CODES.O)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    container.titlesContainer.children.map { it as TextView }
                        .forEachIndexed { index, textView ->
                            val dayOfWeek = daysOfWeek()[index]
                            val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREA)
                            textView.text = title
                        }
                }
            }
    }

    private fun updateDayBinder() {
        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            @RequiresApi(Build.VERSION_CODES.O)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                updateDayView(container, day)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateDayView(container: DayViewContainer, day: CalendarDay) {

        val userId = userViewModel.user.value?.uid

        container.view.setOnClickListener {

            if (userId == null) {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                return@setOnClickListener
            }

            val date = day.date
            val dateStr = "${date.year}-${date.month.value}-${date.dayOfMonth}"
            val formattedDate = formatDateToyyyyMMdd(dateStr)
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("records")
                .whereEqualTo("userId", userId)
                .whereEqualTo("date", formattedDate)
                .get()
                .addOnSuccessListener { result ->
                    if (result.isEmpty) {
                        Log.d(TAG, "No record")
                        val intent = Intent(requireContext(), RecordActivity::class.java)
                        intent.putExtra("userId", userId)
                        intent.putExtra("date", formattedDate)
                        startActivity(intent)
                    } else {
                        Log.d(TAG, "Record exists")
                        val intent = Intent(requireContext(), RecordDetailActivity::class.java)
                        intent.putExtra("userId", userId)
                        intent.putExtra("date", formattedDate)
                        startActivity(intent)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents.", exception)
                }
        }

        container.textView.text = day.date.dayOfMonth.toString()

        recordBookModal.getRecordData().value?.firstOrNull { it.readOnDate(day.date) }?.let {
            Log.d(TAG, "imageUrl: ${it.imageUrl}")
            Glide.with(requireContext()).load(it.imageUrl).into(container.imageView)
        } ?: container.imageView.setImageDrawable(null)
    }

    private fun formatDateToyyyyMMdd(originalDateStr: String): String {

        val originalFormatter = DateTimeFormatter.ofPattern("yyyy-M-d")
        val parsedDate = LocalDate.parse(originalDateStr, originalFormatter)

        // "yyyy-MM-dd" 형식으로 포맷팅
        val targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        return parsedDate.format(targetFormatter);
    }
}

/**
 * ViewHolder
 */
class DayViewContainer(view: View) : ViewContainer(view) {
    val textView = view.findViewById<TextView>(R.id.calendarDayText)

    val imageView = view.findViewById<ImageView>(R.id.calendarDayImageView)

    // With ViewBinding
    // val textView = CalendarDayLayoutBinding.bind(view).calendarDayText
}

class MonthViewContainer(view: View) : ViewContainer(view) {
    val titlesContainer = view as ViewGroup
}