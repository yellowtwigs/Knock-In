package com.yellowtwigs.knockin.ui.groups.manage_group

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ItemContactGridBinding
import com.yellowtwigs.knockin.ui.CircularImageView
import com.yellowtwigs.knockin.ui.groups.manage_group.data.ContactManageGroupViewState
import com.yellowtwigs.knockin.utils.Converter
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactPriorityBorder
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactProfilePicture
import com.yellowtwigs.knockin.utils.RandomDefaultImage

class ContactManageGroupGripAdapter(
    private val cxt: Context,
    private val listOfItemSelected: ArrayList<String>,
    private val onClickedCallback: (String) -> Unit
) :
    ListAdapter<ContactManageGroupViewState, ContactManageGroupGripAdapter.ViewHolder>(
        ContactManageGroupViewStateComparator()
    ) {

    private var imageHeight = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContactGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        imageHeight = binding.civ.layoutParams.height
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemContactGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(contact: ContactManageGroupViewState) {
            binding.apply {
                contactPriorityBorder(contact.priority, civ, cxt)
                contactProfilePicture(contact.profilePicture64, contact.profilePicture, civ, cxt)

                val len = cxt.getSharedPreferences("Gridview_column", Context.MODE_PRIVATE).getInt("gridview", 4)

                InitContactsForListAdapter.InitContactAdapter.initContactNameFromGrid(
                    contact.firstName,
                    contact.lastName,
                    len,
                    firstName,
                    lastName,
                    civ,
                    imageHeight
                )

                itemSelected(contact.id, civ, contact)

                root.setOnClickListener {
                    onClickedCallback(contact.id.toString())
                    itemSelected(contact.id, civ, contact)
                }
            }
        }
    }

    class ContactManageGroupViewStateComparator :
        DiffUtil.ItemCallback<ContactManageGroupViewState>() {
        override fun areItemsTheSame(
            oldItem: ContactManageGroupViewState,
            newItem: ContactManageGroupViewState
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: ContactManageGroupViewState,
            newItem: ContactManageGroupViewState
        ): Boolean {
            return oldItem.firstName == newItem.firstName &&
                    oldItem.lastName == newItem.lastName &&
                    oldItem.profilePicture == newItem.profilePicture &&
                    oldItem.profilePicture64 == newItem.profilePicture64
        }
    }

    private fun itemSelected(
        id: Int,
        image: CircularImageView,
        contact: ContactManageGroupViewState
    ) {
        if (listOfItemSelected.contains(id.toString())) {
            image.setImageResource(R.drawable.ic_item_selected)
        } else {
            if (contact.profilePicture64 != "") {
                val bitmap = Converter.base64ToBitmap(contact.profilePicture64)
                image.setImageBitmap(bitmap)
            } else {
                image.setImageResource(
                    RandomDefaultImage.randomDefaultImage(
                        contact.profilePicture, cxt
                    )
                )
            }
        }
    }

    fun itemDeselected() {
        listOfItemSelected.clear()
    }
}