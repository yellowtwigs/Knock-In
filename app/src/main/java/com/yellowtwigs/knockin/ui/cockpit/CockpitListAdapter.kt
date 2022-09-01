package com.yellowtwigs.knockin.ui.cockpit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.databinding.ItemAppBinding
import com.yellowtwigs.knockin.databinding.ItemCockpitAppBinding

class CockpitListAdapter : ListAdapter<Int, CockpitListAdapter.ViewHolder>(IntComparator()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CockpitListAdapter.ViewHolder {
        return ViewHolder(
            ItemCockpitAppBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CockpitListAdapter.ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemCockpitAppBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(imageRes: Int) {
            binding.appIcon.setImageResource(imageRes)
        }
    }

    class IntComparator : DiffUtil.ItemCallback<Int>() {
        override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem == newItem

        }

        override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem == newItem
        }

    }
}