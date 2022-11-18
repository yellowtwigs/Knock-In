package com.yellowtwigs.knockin.ui.groups.list.section

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.*
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.GroupSectionItemBinding
import com.yellowtwigs.knockin.ui.groups.list.GroupsGridAdapter
import com.yellowtwigs.knockin.ui.groups.list.GroupsListAdapter
import com.yellowtwigs.knockin.ui.groups.manage_group.ManageGroupActivity
import java.net.URLEncoder

class SectionGroupsListAdapter(
    private val cxt: Context,
    private val packageManager: PackageManager,
    private val onClickedCallback: (Int) -> Unit
) :
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
            val sharedPreferences =
                cxt.getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
            val nbGrid = sharedPreferences.getInt("gridview", 1)

            binding.apply {
                groupName.text = section.title

                when (section.sectionColor) {
                    R.color.red_tag_group -> {
                        groupNameSection.background = ResourcesCompat.getDrawable(
                            cxt.resources,
                            R.drawable.rounded_button_color_red,
                            null
                        )
                    }
                    R.color.green_tag_group -> {
                        groupNameSection.background = ResourcesCompat.getDrawable(
                            cxt.resources,
                            R.drawable.rounded_button_color_green,
                            null
                        )
                    }
                    R.color.blue_tag_group -> {
                        groupNameSection.background = ResourcesCompat.getDrawable(
                            cxt.resources,
                            R.drawable.rounded_button_color_blue,
                            null
                        )
                    }
                    R.color.purple_tag_group -> {
                        groupNameSection.background = ResourcesCompat.getDrawable(
                            cxt.resources,
                            R.drawable.rounded_button_color_purple,
                            null
                        )
                    }
                    R.color.yellow_tag_group -> {
                        groupNameSection.background = ResourcesCompat.getDrawable(
                            cxt.resources,
                            R.drawable.rounded_button_color_yellow,
                            null
                        )
                    }
                    R.color.orange_tag_group -> {
                        groupNameSection.background = ResourcesCompat.getDrawable(
                            cxt.resources,
                            R.drawable.rounded_button_color_orange,
                            null
                        )
                    }
                    else -> {
                        groupNameSection.background = ResourcesCompat.getDrawable(
                            cxt.resources,
                            R.drawable.rounded_button_color_red,
                            null
                        )
                    }
                }

                sectionRecyclerView.apply {
                    if (nbGrid == 1) {
                        val groupsListAdapter = GroupsListAdapter(cxt) { id ->
                        }
                        groupsListAdapter.submitList(section.items)
                        adapter = groupsListAdapter
                        layoutManager = LinearLayoutManager(context)
                    } else {
                        val groupsGridAdapter = GroupsGridAdapter(cxt) { id ->
                        }
                        groupsGridAdapter.submitList(section.items)
                        adapter = groupsGridAdapter
                        layoutManager = GridLayoutManager(context, nbGrid)
                    }
                }

                sectionMore.setOnClickListener {
                    val popupMenu = PopupMenu(cxt, it)
                    popupMenu.inflate(R.menu.section_menu_group_manager)
                    popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                        when (item.itemId) {
                            R.id.menu_group_edit_group -> {
                                cxt.startActivity(
                                    Intent(
                                        cxt,
                                        ManageGroupActivity::class.java
                                    ).putExtra("GroupId", section.id)
                                )
                            }
                            R.id.menu_group_delete_group -> {
                                onClickedCallback(section.id)
                            }
                        }
                        return@setOnMenuItemClickListener true
                    }
                    popupMenu.show()
                }

                sectionSms.setOnClickListener {
                    monoChannelSmsClick(section.phoneNumbers)
                }

                sectionGmail.isVisible = section.emails.isNotEmpty()
                sectionGmail.setOnClickListener {
                    monoChannelMailClick(section.emails)
                }
                sectionWhatsapp.setOnClickListener {
                    monoChannelWhatsapp()
                }
            }
        }

        private fun monoChannelSmsClick(listOfPhoneNumber: ArrayList<String>) {
            var message = "smsto:" + listOfPhoneNumber[0]
            for (element in listOfPhoneNumber) {
                message += ";$element"
            }
            cxt.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(message)))
        }

        private fun monoChannelMailClick(listOfMail: ArrayList<String>) {
            val contact = listOfMail.toArray(arrayOfNulls<String>(listOfMail.size))
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_EMAIL, contact)
            intent.data = Uri.parse("mailto:")
            intent.type = "message/rfc822"
            intent.putExtra(Intent.EXTRA_SUBJECT, "")
            intent.putExtra(Intent.EXTRA_TEXT, "")
            cxt.startActivity(intent)
        }

        private fun monoChannelWhatsapp() {
            val i = Intent(Intent.ACTION_VIEW)

            try {
                val url = "https://api.whatsapp.com/send?text=" + URLEncoder.encode(".", "UTF-8")
                i.setPackage("com.whatsapp")
                i.data = Uri.parse(url)
                if (i.resolveActivity(packageManager) != null) {
                    cxt.startActivity(i)
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
            return oldItem.id == newItem.id &&
                    oldItem.title == newItem.title &&
                    oldItem.sectionColor == newItem.sectionColor &&
                    oldItem.items == newItem.items &&
                    oldItem.phoneNumbers == newItem.phoneNumbers &&
                    oldItem.emails == newItem.emails
        }
    }
}