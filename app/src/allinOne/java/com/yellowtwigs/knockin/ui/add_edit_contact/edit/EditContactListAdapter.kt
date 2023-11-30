package com.yellowtwigs.knockin.ui.add_edit_contact.edit

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.databinding.ItemNumberWithSpinnerBinding

class EditContactListAdapter(
    private val context: Context,
    private val onAddedNumberCallback: () -> Unit,
    private val onDeletedNumberCallback: (Int?, String) -> Unit,
) : ListAdapter<PhoneNumberWithSpinner, EditContactListAdapter.ViewHolder>(PhoneNumberWithSpinnerComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemNumberWithSpinnerBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position), onAddedNumberCallback, onDeletedNumberCallback)
    }

    class ViewHolder(private val binding: ItemNumberWithSpinnerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(
            phoneWithSpinner: PhoneNumberWithSpinner, onClickedCallback: () -> Unit, onDeletedNumberCallback: (Int?, String) -> Unit
        ) {
            phoneWithSpinner.flag?.let {
                if (it < 7) binding.spinnerFlag.setSelection(it)
            }
            binding.spinnerFlag.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }

            binding.phoneNumberContent.setText(phoneWithSpinner.phoneNumber)
        }
    }

    object PhoneNumberWithSpinnerComparator : DiffUtil.ItemCallback<PhoneNumberWithSpinner>() {
        override fun areItemsTheSame(oldItem: PhoneNumberWithSpinner, newItem: PhoneNumberWithSpinner): Boolean =
            oldItem.phoneNumber == newItem.phoneNumber

        override fun areContentsTheSame(oldItem: PhoneNumberWithSpinner, newItem: PhoneNumberWithSpinner): Boolean = oldItem == newItem
    }
}