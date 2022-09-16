package com.yellowtwigs.knockin.ui.groups.list.section

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.databinding.GroupSectionItemBinding
import com.yellowtwigs.knockin.ui.groups.list.GroupsListAdapter
import com.yellowtwigs.knockin.ui.groups.list.GroupsListViewState

class SectionGroupsListAdapter(private val cxt: Context) :
    ListAdapter<SectionViewState, SectionGroupsListAdapter.ViewHolder>(
        SectionViewStateComparator()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            GroupSectionItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class ViewHolder(private val binding: GroupSectionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(section: SectionViewState) {
            val groupsListAdapter = GroupsListAdapter(cxt) { id ->
            }

            binding.apply {
                groupName.text = section.title

                sectionRecyclerView.apply {
                    groupsListAdapter.submitList(section.items)
                    adapter = groupsListAdapter
                    layoutManager = LinearLayoutManager(context)
                }
            }

        }
    }

    class SectionViewStateComparator : DiffUtil.ItemCallback<SectionViewState>() {
        override fun areItemsTheSame(
            oldItem: SectionViewState,
            newItem: SectionViewState
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: SectionViewState,
            newItem: SectionViewState
        ): Boolean {
            return oldItem.title == newItem.title &&
                    oldItem.items == newItem.items
        }
    }
}