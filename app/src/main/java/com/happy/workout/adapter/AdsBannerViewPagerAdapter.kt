package com.happy.workout.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.happy.workout.R
import com.happy.workout.model.AdsBannerItem

class AdsBannerViewPagerAdapter(
    private val context: Context,
    private val items: List<AdsBannerItem>
) : RecyclerView.Adapter<AdsBannerViewPagerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ads_banner_card_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position].imageUrl)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.adImageView)

        fun bind(imageUrl: Int) {
            Glide.with(context).load(imageUrl).into(imageView)
        }
    }
}
