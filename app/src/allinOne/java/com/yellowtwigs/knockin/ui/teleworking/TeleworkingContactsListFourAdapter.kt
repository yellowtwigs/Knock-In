package com.yellowtwigs.knockin.ui.teleworking

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.databinding.ItemContactGrid4Binding
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactPriorityBorder
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactProfilePicture

class TeleworkingContactsListFourAdapter(
    private val cxt: Context,
    private val onClickedCallback: (Int) -> Unit
) :
    ListAdapter<TeleworkingViewState, TeleworkingContactsListFourAdapter.ViewHolder>(
        TeleworkingViewStateComparator()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContactGrid4Binding.inflate(
            LayoutInflater.from(parent.context), parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemContactGrid4Binding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(contact: TeleworkingViewState) {
            binding.apply {
                contactPriorityBorder(2, civ, cxt)
                contactProfilePicture(contact.profilePicture64, contact.profilePicture, civ, cxt)

                firstName.text = contact.firstName
                lastName.text = contact.lastName

                root.setOnClickListener {
                    onClickedCallback(contact.id)
                }
            }
        }
    }

    class TeleworkingViewStateComparator : DiffUtil.ItemCallback<TeleworkingViewState>() {
        override fun areItemsTheSame(
            oldItem: TeleworkingViewState,
            newItem: TeleworkingViewState
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: TeleworkingViewState,
            newItem: TeleworkingViewState
        ): Boolean {
            return oldItem.id == newItem.id &&
                    oldItem.firstName == newItem.firstName &&
                    oldItem.lastName == newItem.lastName &&
                    oldItem.profilePicture == newItem.profilePicture &&
                    oldItem.profilePicture64 == newItem.profilePicture64
        }
    }
}