package com.yellowtwigs.knockin.ui.first_launch.start

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.yellowtwigs.knockin.databinding.SlideItemContainerBinding

class SliderAdapter(
    private val sliderItems: MutableList<SliderItem>
) : RecyclerView.Adapter<SliderAdapter.SliderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        return (SliderViewHolder(
            SlideItemContainerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        ))
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        holder.onBind(sliderItems[position])
    }

    override fun getItemCount(): Int {
        return sliderItems.size
    }

    inner class SliderViewHolder(private val binding: SlideItemContainerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(sliderItem: SliderItem) {
            Glide.with(binding.gifImageView)
                .load(sliderItem.image)
                .into(binding.gifImageView)
        }
    }
}