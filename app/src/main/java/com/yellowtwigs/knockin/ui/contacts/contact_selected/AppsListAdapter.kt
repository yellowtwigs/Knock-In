package com.yellowtwigs.knockin.ui.contacts.contact_selected

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.databinding.ItemAppBinding

class AppsListAdapter(private val onClicked: (Int) -> Unit) :
    ListAdapter<Int, AppsListAdapter.ViewHolder>(IntegerComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position), onClicked)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class ViewHolder(private val binding: ItemAppBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(imageResource: Int, onClicked: (Int) -> Unit) {
            binding.apply {
                appIcon.setImageResource(imageResource)

                root.setOnClickListener {
                    onClicked(imageResource)
                }
            }
        }
    }

    class IntegerComparator : DiffUtil.ItemCallback<Int>() {
        override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem.dec() == newItem.dec()
        }
    }
}