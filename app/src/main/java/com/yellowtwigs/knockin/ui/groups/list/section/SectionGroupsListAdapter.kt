package com.yellowtwigs.knockin.ui.groups.list.section

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.GroupSectionItemBinding
import com.yellowtwigs.knockin.ui.CircularImageView
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.PhoneNumberWithSpinner
import com.yellowtwigs.knockin.ui.groups.list.*
import com.yellowtwigs.knockin.ui.groups.manage_group.ManageGroupActivity
import com.yellowtwigs.knockin.utils.ContactGesture
import com.yellowtwigs.knockin.utils.Converter
import com.yellowtwigs.knockin.utils.RandomDefaultImage
import kotlin.collections.ArrayList

class SectionGroupsListAdapter(
    private val cxt: Context, private val onClickedCallback: (Int) -> Unit, private val onSectionClickedCallback: (Int) -> Unit
) : ListAdapter<SectionViewState, SectionGroupsListAdapter.ViewHolder>(
    SectionViewStateComparator()
) {

    companion object {
        var listOfItemSelected = ArrayList<ContactInGroupViewState>()

        var listOfIds = arrayListOf<Int>()
        var listOfHasSms = arrayListOf<Boolean>()
        var listOfPhoneNumbers = arrayListOf<PhoneNumberWithSpinner>()

        var listOfHasEmail = arrayListOf<Boolean>()
        var listOfEmails = arrayListOf<String>()

        var listOfHasWhatsapp = arrayListOf<Boolean>()

        var isSectionClicked = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(GroupSectionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class ViewHolder(private val binding: GroupSectionItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(section: SectionViewState) {
            val sharedPreferences = cxt.getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
            val nbGrid = sharedPreferences.getInt("gridview", 1)

            binding.apply {
                groupName.text = section.title

                when (section.sectionColor) {
                    R.color.red_tag_group -> {
                        groupNameSection.background = ResourcesCompat.getDrawable(
                            cxt.resources, R.drawable.rounded_button_color_red, null
                        )
                    }
                    R.color.green_tag_group -> {
                        groupNameSection.background = ResourcesCompat.getDrawable(
                            cxt.resources, R.drawable.rounded_button_color_green, null
                        )
                    }
                    R.color.blue_tag_group -> {
                        groupNameSection.background = ResourcesCompat.getDrawable(
                            cxt.resources, R.drawable.rounded_button_color_blue, null
                        )
                    }
                    R.color.purple_tag_group -> {
                        groupNameSection.background = ResourcesCompat.getDrawable(
                            cxt.resources, R.drawable.rounded_button_color_purple, null
                        )
                    }
                    R.color.yellow_tag_group -> {
                        groupNameSection.background = ResourcesCompat.getDrawable(
                            cxt.resources, R.drawable.rounded_button_color_yellow, null
                        )
                    }
                    R.color.orange_tag_group -> {
                        groupNameSection.background = ResourcesCompat.getDrawable(
                            cxt.resources, R.drawable.rounded_button_color_orange, null
                        )
                    }
                    else -> {
                        groupNameSection.background = ResourcesCompat.getDrawable(
                            cxt.resources, R.drawable.rounded_button_color_red, null
                        )
                    }
                }

                var groupsListAdapter = GroupsListAdapter(cxt, {}, { id, civ, contact ->
                    itemSelected(civ, contact)
                    (cxt as GroupsListActivity).modeMultiSelect = listOfItemSelected.isNotEmpty()
                    isSectionClicked = listOfItemSelected.isNotEmpty()
                    onSectionClickedCallback(0)
                })

                sectionRecyclerView.apply {
                    if (nbGrid == 1) {
                        groupsListAdapter.submitList(section.items)
                        adapter = groupsListAdapter
                        layoutManager = LinearLayoutManager(context)
                    } else {
                        val groupsGridAdapter = if (nbGrid == 4) {
                            GroupsGridFourAdapter(cxt, {}, { id, civ, contact ->
                                itemSelected(civ, contact)
                                (cxt as GroupsListActivity).modeMultiSelect = listOfItemSelected.isNotEmpty()
                                isSectionClicked = listOfItemSelected.isNotEmpty()
                                onSectionClickedCallback(0)
                            })
                        } else {
                            GroupsGridFiveAdapter(cxt, {}, { id, civ, contact ->
                                itemSelected(civ, contact)
                                (cxt as GroupsListActivity).modeMultiSelect = listOfItemSelected.isNotEmpty()
                                isSectionClicked = listOfItemSelected.isNotEmpty()
                                onSectionClickedCallback(1)
                            })
                        }
                        groupsGridAdapter.submitList(section.items)
                        adapter = groupsGridAdapter
                        layoutManager = GridLayoutManager(context, nbGrid)
                    }
                }

                groupNameSection.setOnClickListener {
                    if (GroupsListActivity.listOfSectionsSelected.contains(section.id)) {
                        GroupsListActivity.listOfSectionsSelected.remove(section.id)
                        isSectionClicked = false

                        section.items.map {
                            if (listOfItemSelected.contains(it)) {
                                listOfItemSelected.remove(it)
                            }
                            if (listOfIds.contains(it.id)) {
                                listOfIds.remove(it.id)
                            }

                            listOfHasSms.remove(it.firstPhoneNumber.flag != null)
                            listOfHasEmail.remove(!it.listOfMails.contains(""))
                            listOfHasWhatsapp.remove(!it.hasWhatsapp)

                            if (listOfPhoneNumbers.contains(it.firstPhoneNumber)) {
                                listOfPhoneNumbers.remove(it.firstPhoneNumber)
                            }
                            if (listOfPhoneNumbers.contains(it.secondPhoneNumber)) {
                                listOfPhoneNumbers.remove(it.secondPhoneNumber)
                            }

                            listOfEmails.remove(
                                if (!it.listOfMails.contains("")) {
                                    it.listOfMails.random()
                                } else {
                                    ""
                                }
                            )
                        }

                        sectionRecyclerView.apply {
                            if (nbGrid == 1) {
                                groupsListAdapter = GroupsListAdapter(cxt, {}, { id, civ, contact ->
                                    itemSelected(civ, contact)
                                    (cxt as GroupsListActivity).modeMultiSelect = listOfItemSelected.isNotEmpty()
                                    isSectionClicked = listOfItemSelected.isNotEmpty()
                                    onSectionClickedCallback(0)
                                })
                                groupsListAdapter.submitList(section.items)
                                adapter = groupsListAdapter
                                layoutManager = LinearLayoutManager(context)
                            } else {
                                val groupsGridAdapter = if (nbGrid == 4) {
                                    GroupsGridFourAdapter(cxt, {}, { id, civ, contact ->
                                        itemSelected(civ, contact)
                                        (cxt as GroupsListActivity).modeMultiSelect = listOfItemSelected.isNotEmpty()
                                        isSectionClicked = listOfItemSelected.isNotEmpty()
                                        onSectionClickedCallback(1)
                                    })
                                } else {
                                    GroupsGridFiveAdapter(cxt, {}, { id, civ, contact ->
                                        itemSelected(civ, contact)
                                        (cxt as GroupsListActivity).modeMultiSelect = listOfItemSelected.isNotEmpty()
                                        isSectionClicked = listOfItemSelected.isNotEmpty()
                                        onSectionClickedCallback(1)
                                    })
                                }
                                groupsGridAdapter.submitList(section.items)
                                adapter = groupsGridAdapter
                                layoutManager = GridLayoutManager(context, nbGrid)
                            }
                        }
                    } else {
                        GroupsListActivity.listOfSectionsSelected.add(section.id)
                        isSectionClicked = true

                        section.items.map {
                            if (!listOfItemSelected.contains(it)) {
                                listOfItemSelected.add(it)
                            }
                        }

                        section.items.map {
                            if (!listOfIds.contains(it.id)) {
                                listOfIds.add(it.id)
                            }
                        }

                        listOfHasSms.addAll(section.items.map {
                            it.firstPhoneNumber.flag != null
                        })

                        listOfHasEmail.addAll(section.items.map {
                            !it.listOfMails.contains("")
                        })

                        section.items.map { contact ->
                            if (contact.firstPhoneNumber.flag == 2) {
                                listOfPhoneNumbers.add(contact.firstPhoneNumber)
                            } else if (contact.firstPhoneNumber.flag != 2 && contact.secondPhoneNumber.flag == 2) {
                                if (contact.secondPhoneNumber.phoneNumber.isNotEmpty()) {
                                    listOfPhoneNumbers.add(contact.secondPhoneNumber)
                                } else {
                                    listOfPhoneNumbers.add(contact.firstPhoneNumber)
                                }
                            } else if (contact.firstPhoneNumber.flag == 2 && contact.secondPhoneNumber.flag == 2) {
                                listOfPhoneNumbers.add(contact.firstPhoneNumber)
                            } else {
                                listOfPhoneNumbers.add(contact.firstPhoneNumber)
                            }
                        }

                        listOfEmails.addAll(section.items.map { contact ->
                            if (!contact.listOfMails.contains("")) {
                                contact.listOfMails.random()
                            } else {
                                ""
                            }
                        })

                        listOfHasWhatsapp.addAll(section.items.map {
                            it.hasWhatsapp
                        })

                        sectionRecyclerView.apply {
                            if (nbGrid == 1) {
                                groupsListAdapter = GroupsListAdapter(cxt, {}, { id, civ, contact ->
                                    itemSelected(civ, contact)
                                    (cxt as GroupsListActivity).modeMultiSelect = listOfItemSelected.isNotEmpty()
                                    isSectionClicked = listOfItemSelected.isNotEmpty()
                                    onSectionClickedCallback(0)
                                })
                                groupsListAdapter.submitList(section.items)
                                adapter = groupsListAdapter
                                layoutManager = LinearLayoutManager(context)
                            } else {
                                val groupsGridAdapter = if (nbGrid == 4) {
                                    GroupsGridFourAdapter(cxt, {}, { id, civ, contact ->
                                        itemSelected(civ, contact)
                                        (cxt as GroupsListActivity).modeMultiSelect = listOfItemSelected.isNotEmpty()
//
//                                setupMultiSelectToolbar(binding, modeMultiSelect)
                                    })
                                } else {
                                    GroupsGridFiveAdapter(cxt, {}, { id, civ, contact ->
                                        itemSelected(civ, contact)
                                        (cxt as GroupsListActivity).modeMultiSelect = listOfItemSelected.isNotEmpty()
//
//                                setupMultiSelectToolbar(binding, modeMultiSelect)
                                    })
                                }
                                groupsGridAdapter.submitList(section.items)
                                adapter = groupsGridAdapter
                                layoutManager = GridLayoutManager(context, nbGrid)
                            }
                        }
                    }


//                    (cxt as GroupsListActivity).modeMultiSelect = !isSectionClicked
//                    isSectionClicked = !isSectionClicked
//                    if (isSectionClicked) {
//                    } else {
//                    }
//
//                    onClickedCallbackMultiSelect(1)

//                    (cxt as GroupsListActivity).modeMultiSelect = listOfItemSelected.isNotEmpty()
//                    isSectionClicked = listOfItemSelected.isNotEmpty()
                    onSectionClickedCallback(0)
                }

                sectionMore.setOnClickListener {
                    val popupMenu = PopupMenu(cxt, it)
                    popupMenu.inflate(R.menu.section_menu_group_manager)
                    popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                        when (item.itemId) {
                            R.id.menu_group_edit_group -> {
                                cxt.startActivity(
                                    Intent(
                                        cxt, ManageGroupActivity::class.java
                                    ).putExtra("GroupId", section.id)
                                )
                            }
                            R.id.menu_group_delete_group -> {
                                deleteGroupAlertDialog(section.title, section.id)
                            }
                        }
                        return@setOnMenuItemClickListener true
                    }
                    popupMenu.show()
                }
            }
        }

        private fun deleteGroupAlertDialog(groupName: String, id: Int) {
            MaterialAlertDialogBuilder(cxt, R.style.AlertDialog).setTitle(R.string.section_alert_delete_group_title).setMessage(
                String.format(groupName, R.string.section_alert_delete_group_message)
            ).setPositiveButton(
                android.R.string.yes
            ) { _: DialogInterface?, _: Int ->
                onClickedCallback(id)
            }.setNegativeButton(android.R.string.no, null).show()
        }

        private fun itemSelected(image: CircularImageView, contact: ContactInGroupViewState) {
            if (listOfItemSelected.contains(contact)) {
                listOfItemSelected.remove(contact)
                listOfIds.remove(contact.id)
                listOfHasSms.remove(contact.firstPhoneNumber.flag != null)

                if (listOfPhoneNumbers.contains(contact.firstPhoneNumber)) {
                    listOfPhoneNumbers.remove(contact.firstPhoneNumber)
                } else if (listOfPhoneNumbers.contains(contact.secondPhoneNumber)) {
                    listOfPhoneNumbers.remove(contact.secondPhoneNumber)
                }

                listOfHasEmail.remove(!contact.listOfMails.contains(""))
                if (!contact.listOfMails.contains("")) {
                    listOfEmails.remove(contact.listOfMails.random())
                }

                listOfHasWhatsapp.remove(contact.hasWhatsapp)

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
            } else {
                listOfItemSelected.add(contact)
                listOfIds.add(contact.id)

                listOfHasSms.add(contact.firstPhoneNumber.flag != null)

                if (contact.firstPhoneNumber.flag == 2 && contact.secondPhoneNumber.flag == 2) {
                    MaterialAlertDialogBuilder(
                        cxt, R.style.AlertDialog
                    ).setBackground(cxt.getDrawable(R.color.backgroundColor)).setTitle("")
                        .setMessage(cxt.getString(R.string.two_numbers_dialog_message))
                        .setPositiveButton("${ContactGesture.transformPhoneNumberWithSpinnerToFlag(contact.firstPhoneNumber)} : ${contact.firstPhoneNumber.phoneNumber}") { _, _ ->
                            listOfPhoneNumbers.add(contact.firstPhoneNumber)
                        }
                        .setNegativeButton("${ContactGesture.transformPhoneNumberWithSpinnerToFlag(contact.secondPhoneNumber)} : ${contact.secondPhoneNumber.phoneNumber}") { dialog, _ ->
                            listOfPhoneNumbers.add(contact.secondPhoneNumber)

                            dialog.cancel()
                            dialog.dismiss()
                        }.show()
                } else if (contact.firstPhoneNumber.flag == 2 && contact.secondPhoneNumber.flag == null) {
                    listOfPhoneNumbers.add(contact.firstPhoneNumber)
                } else if (contact.firstPhoneNumber.flag == null && contact.secondPhoneNumber.flag == 2) {
                    contact.secondPhoneNumber.let {
                        listOfPhoneNumbers.add(it)
                    }
                } else if (contact.firstPhoneNumber.flag != 2 && contact.secondPhoneNumber.flag == null) {
                    MaterialAlertDialogBuilder(
                        cxt, R.style.AlertDialog
                    ).setBackground(cxt.getDrawable(R.color.backgroundColor)).setTitle(cxt.getString(R.string.not_mobile_flag_title))
                        .setMessage(cxt.getString(R.string.multi_channel_not_mobile_flag))
                        .setPositiveButton(cxt.getString(R.string.alert_dialog_yes)) { _, _ ->
                            listOfPhoneNumbers.add(contact.firstPhoneNumber)
                        }.setNegativeButton(cxt.getString(R.string.alert_dialog_no)) { dialog, _ ->
                            dialog.cancel()
                            dialog.dismiss()
                        }.show()
                } else if (contact.firstPhoneNumber.flag == null && contact.secondPhoneNumber.flag != 2) {
                    MaterialAlertDialogBuilder(
                        cxt, R.style.AlertDialog
                    ).setBackground(cxt.getDrawable(R.color.backgroundColor)).setTitle(cxt.getString(R.string.not_mobile_flag_title))
                        .setMessage(cxt.getString(R.string.multi_channel_not_mobile_flag))
                        .setPositiveButton(cxt.getString(R.string.alert_dialog_yes)) { _, _ ->
                            contact.secondPhoneNumber.let {
                                listOfPhoneNumbers.add(it)
                            }
                        }.setNegativeButton(cxt.getString(R.string.alert_dialog_no)) { dialog, _ ->
                            dialog.cancel()
                            dialog.dismiss()
                        }.show()
                } else if (contact.firstPhoneNumber.flag != 2 && contact.secondPhoneNumber.flag != 2) {
                    MaterialAlertDialogBuilder(
                        cxt, R.style.AlertDialog
                    ).setBackground(cxt.getDrawable(R.color.backgroundColor)).setTitle("")
                        .setMessage(cxt.getString(R.string.two_numbers_dialog_message))
                        .setPositiveButton("${ContactGesture.transformPhoneNumberWithSpinnerToFlag(contact.firstPhoneNumber)} : ${contact.firstPhoneNumber.phoneNumber}") { _, _ ->
                            listOfPhoneNumbers.add(contact.firstPhoneNumber)
                        }
                        .setNegativeButton("${ContactGesture.transformPhoneNumberWithSpinnerToFlag(contact.secondPhoneNumber)} : ${contact.secondPhoneNumber.phoneNumber}") { dialog, _ ->
                            listOfPhoneNumbers.add(contact.secondPhoneNumber)

                            dialog.cancel()
                            dialog.dismiss()
                        }.show()
                } else if (contact.firstPhoneNumber.flag == 2 && contact.secondPhoneNumber.flag != 2) {
                    MaterialAlertDialogBuilder(
                        cxt, R.style.AlertDialog
                    ).setBackground(cxt.getDrawable(R.color.backgroundColor)).setTitle("")
                        .setMessage(cxt.getString(R.string.two_numbers_dialog_message))
                        .setPositiveButton("${ContactGesture.transformPhoneNumberWithSpinnerToFlag(contact.firstPhoneNumber)} : ${contact.firstPhoneNumber.phoneNumber}") { _, _ ->
                            listOfPhoneNumbers.add(contact.firstPhoneNumber)
                        }
                        .setNegativeButton("${ContactGesture.transformPhoneNumberWithSpinnerToFlag(contact.secondPhoneNumber)} : ${contact.secondPhoneNumber.phoneNumber}") { dialog, _ ->
                            contact.secondPhoneNumber.let {
                                listOfPhoneNumbers.add(it)
                            }
                            dialog.cancel()
                            dialog.dismiss()
                        }.show()
                } else if (contact.firstPhoneNumber.flag != 2 && contact.secondPhoneNumber.flag == 2) {
                    MaterialAlertDialogBuilder(
                        cxt, R.style.AlertDialog
                    ).setBackground(cxt.getDrawable(R.color.backgroundColor)).setTitle("")
                        .setMessage(cxt.getString(R.string.two_numbers_dialog_message))
                        .setPositiveButton("${ContactGesture.transformPhoneNumberWithSpinnerToFlag(contact.firstPhoneNumber)} : ${contact.firstPhoneNumber.phoneNumber}") { _, _ ->
                            listOfPhoneNumbers.add(contact.firstPhoneNumber)
                        }
                        .setNegativeButton("${ContactGesture.transformPhoneNumberWithSpinnerToFlag(contact.secondPhoneNumber)} : ${contact.secondPhoneNumber.phoneNumber}") { dialog, _ ->
                            contact.secondPhoneNumber.let {
                                listOfPhoneNumbers.add(it)
                            }
                            dialog.cancel()
                            dialog.dismiss()
                        }.show()
                } else {

                }

                listOfHasEmail.add(!contact.listOfMails.contains(""))
                if (!contact.listOfMails.contains("")) {
                    listOfEmails.add(contact.listOfMails.random())
                }

                listOfHasWhatsapp.add(contact.hasWhatsapp)
                image.setImageResource(contact.profilePictureSelected)
            }
        }
    }

    class SectionViewStateComparator : DiffUtil.ItemCallback<SectionViewState>() {
        override fun areItemsTheSame(
            oldItem: SectionViewState, newItem: SectionViewState
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: SectionViewState, newItem: SectionViewState
        ): Boolean {
            return oldItem.id == newItem.id && oldItem.title == newItem.title && oldItem.sectionColor == newItem.sectionColor && oldItem.items == newItem.items && oldItem.phoneNumbers == newItem.phoneNumbers && oldItem.emails == newItem.emails
        }
    }
}