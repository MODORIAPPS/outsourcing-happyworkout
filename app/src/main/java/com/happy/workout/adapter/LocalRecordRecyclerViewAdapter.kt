package com.happy.workout.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.happy.workout.R
import com.happy.workout.model.AdsBannerItem
import com.happy.workout.model.LocalRecordListItem
import java.text.SimpleDateFormat
import java.util.Locale

class LocalRecordRecyclerViewAdapter(
    private val context: Context,
    private val items: List<LocalRecordListItem>
) : RecyclerView.Adapter<LocalRecordRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.local_record_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dogImageView: ImageView = itemView.findViewById(R.id.image)
        private val title: TextView = itemView.findViewById(R.id.title)
        private val description: TextView = itemView.findViewById(R.id.description)
        private val date: TextView = itemView.findViewById(R.id.dateview)

        fun bind(item: LocalRecordListItem) {
            Glide.with(context).load(item.imageUrl).into(dogImageView)
            title.text = item.title
            description.text = item.description
            date.text = formatTimestamp(item.createdAt)
        }
    }
}

fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
    val currentTimeMillis = System.currentTimeMillis()
    val firestoreTimeMillis = timestamp.toDate().time
    val timeDifferenceMillis = currentTimeMillis - firestoreTimeMillis

    val seconds = timeDifferenceMillis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days >= 1 -> {
            // 12시간 이상인 경우 YYYY-MM-dd 형식으로 표시
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.format(timestamp.toDate())
        }
        hours >= 1 -> "${hours.toInt()}시간 전"
        minutes >= 1 -> "${minutes.toInt()}분 전"
        else -> "방금 전"
    }
}


