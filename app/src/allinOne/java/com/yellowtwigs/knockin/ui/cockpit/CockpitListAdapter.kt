package com.yellowtwigs.knockin.ui.cockpit

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.databinding.ItemCockpitAppBinding
import com.yellowtwigs.knockin.utils.NotificationsGesture.convertPackageNameToGoTo

class CockpitListAdapter(private val cxt: Context) : ListAdapter<CockpitViewState, CockpitListAdapter.ViewHolder>(IntComparator()) {
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

        fun onBind(cockpitViewState: CockpitViewState) {
            binding.appIcon.setImageResource(cockpitViewState.icon)
            binding.appIcon.setOnClickListener {
                convertPackageNameToGoTo(cockpitViewState.packageName, cxt)
            }
        }
    }

    class IntComparator : DiffUtil.ItemCallback<CockpitViewState>() {
        override fun areItemsTheSame(
            oldItem: CockpitViewState,
            newItem: CockpitViewState
        ): Boolean {
            return oldItem == newItem

        }

        override fun areContentsTheSame(
            oldItem: CockpitViewState,
            newItem: CockpitViewState
        ): Boolean {
            return oldItem.icon == newItem.icon &&
                    oldItem.packageName == newItem.packageName
        }

    }
}