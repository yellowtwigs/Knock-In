package com.yellowtwigs.knockin.ui.groups.manage_group

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityManageGroupBinding
import com.yellowtwigs.knockin.model.database.data.GroupDB
import com.yellowtwigs.knockin.ui.groups.list.GroupsListActivity
import com.yellowtwigs.knockin.ui.groups.manage_group.data.ManageGroupViewState
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkTheme
import com.yellowtwigs.knockin.utils.EveryActivityUtils.hideKeyboard
import com.yellowtwigs.knockin.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManageGroupActivity : AppCompatActivity() {

    private val binding by viewBinding { ActivityManageGroupBinding.inflate(it) }
    private val viewModel by viewModels<ManageGroupViewModel>()

    private val listOfItemSelected = arrayListOf<String>()

    private var groupId = -1
    private lateinit var currentGroup: ManageGroupViewState
    private var currentColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        checkTheme(this)
        setupToolbar()

        groupId = intent.getIntExtra("SectionId", -1)
        viewModel.setGroupById(groupId)
        setupRecyclerView()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        val actionbar = supportActionBar
        actionbar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_close)
            it.title = ""
        }
    }

    //region =========================================== SETUP UI ===========================================


    private fun setupRecyclerView() {
        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val nbGrid = sharedPreferences.getInt("gridview", 1)

        Log.i("GetContactsFromGroup", "nbGrid : ${nbGrid}")

        if (nbGrid == 1) {
            binding.contacts.apply {
                viewModel.groupViewState.asLiveData().observe(this@ManageGroupActivity) { manageGroupViewState ->
                    currentGroup = manageGroupViewState
                    currentColor = manageGroupViewState.section_color

                    setupSectionColorSelection()

                    binding.groupName.setText(manageGroupViewState.groupName)
                    listOfItemSelected.addAll(manageGroupViewState.listOfIds)

                    val manageGroupListAdapter = ContactManageGroupListAdapter(this@ManageGroupActivity, listOfItemSelected) { id ->
                        hideKeyboard(this@ManageGroupActivity)
                        if (listOfItemSelected.contains(id)) {
                            listOfItemSelected.remove(id)
                        } else {
                            listOfItemSelected.add(id)
                        }
                    }

                    manageGroupListAdapter.submitList(null)
                    manageGroupListAdapter.submitList(manageGroupViewState.listOfContacts)

                    adapter = manageGroupListAdapter
                    layoutManager = LinearLayoutManager(context)
                    setItemViewCacheSize(500)
                }
            }
        } else {
            binding.contacts.apply {
                viewModel.groupViewState.asLiveData().observe(this@ManageGroupActivity) { manageGroupViewState ->
                    currentGroup = manageGroupViewState
                    currentColor = manageGroupViewState.section_color

                    setupSectionColorSelection()

                    binding.groupName.setText(manageGroupViewState.groupName)
                    listOfItemSelected.addAll(manageGroupViewState.listOfIds)

                    val manageGroupGripAdapter = if (nbGrid == 4) {
                        ContactManageGroupGripFourAdapter(
                            this@ManageGroupActivity, listOfItemSelected
                        ) { id ->
                            hideKeyboard(this@ManageGroupActivity)
                            if (listOfItemSelected.contains(id)) {
                                listOfItemSelected.remove(id)
                            } else {
                                listOfItemSelected.add(id)
                            }
                        }
                    } else {
                        ContactManageGroupGripFiveAdapter(
                            this@ManageGroupActivity, listOfItemSelected
                        ) { id ->
                            hideKeyboard(this@ManageGroupActivity)
                            if (listOfItemSelected.contains(id)) {
                                listOfItemSelected.remove(id)
                            } else {
                                listOfItemSelected.add(id)
                            }
                        }
                    }

                    manageGroupGripAdapter.submitList(null)
                    manageGroupGripAdapter.submitList(manageGroupViewState.listOfContacts)

                    adapter = manageGroupGripAdapter
                    layoutManager = GridLayoutManager(context, nbGrid)
                    setItemViewCacheSize(500)
                }
            }
        }
    }

    private fun setupSectionColorSelection() {
        binding.apply {
            selectColorSeparator.setOnClickListener {
                selectColorLayout.isVisible = !selectColorLayout.isVisible
            }
            when (currentColor) {
                R.color.blue_tag_group -> {
                    sectionColorClick(groupColorBlue)
                }
                R.color.green_tag_group -> {
                    sectionColorClick(groupColorGreen)
                }
                R.color.red_tag_group -> {
                    sectionColorClick(groupColorRed)
                }
                R.color.yellow_tag_group -> {
                    sectionColorClick(groupColorYellow)
                }
                R.color.purple_tag_group -> {
                    sectionColorClick(groupColorPurple)
                }
                R.color.orange_tag_group -> {
                    sectionColorClick(groupColorOrange)
                }
            }
            groupColorBlue.setOnClickListener {
                currentColor = R.color.blue_tag_group
                sectionColorClick(groupColorBlue)
            }
            groupColorRed.setOnClickListener {
                currentColor = R.color.red_tag_group
                sectionColorClick(groupColorRed)
            }
            groupColorGreen.setOnClickListener {
                currentColor = R.color.green_tag_group
                sectionColorClick(groupColorGreen)
            }
            groupColorOrange.setOnClickListener {
                currentColor = R.color.orange_tag_group
                sectionColorClick(groupColorOrange)
            }
            groupColorPurple.setOnClickListener {
                currentColor = R.color.purple_tag_group
                sectionColorClick(groupColorPurple)
            }
            groupColorYellow.setOnClickListener {
                currentColor = R.color.yellow_tag_group
                sectionColorClick(groupColorYellow)
            }
        }
    }

    //endregion

    private fun sectionColorClick(imageView: AppCompatImageView) {
        binding.apply {
            groupColorBlue.setImageResource(android.R.color.transparent)
            groupColorRed.setImageResource(android.R.color.transparent)
            groupColorGreen.setImageResource(android.R.color.transparent)
            groupColorOrange.setImageResource(android.R.color.transparent)
            groupColorPurple.setImageResource(android.R.color.transparent)
            groupColorYellow.setImageResource(android.R.color.transparent)

            imageView.setImageResource(R.drawable.border_selected_yellow)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu_validation, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                refreshActivity()
            }
            R.id.nav_validate -> {
                if (binding.groupName.text?.isEmpty() == true) {
                    Toast.makeText(
                        this, getString(R.string.add_new_group_toast_empty_field), Toast.LENGTH_LONG
                    ).show()
                } else {
                    if (listOfItemSelected.isEmpty()) {
                        Toast.makeText(
                            this, getString(R.string.add_new_group_toast_no_contact_selected), Toast.LENGTH_LONG
                        ).show()
                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            if (groupId == -1) {
                                viewModel.createNewGroup(
                                    GroupDB(
                                        0, binding.groupName.text.toString(), "", currentColor, listOfItemSelected, 0
                                    )
                                )
                            } else {
                                Log.i("sectionColor", "currentColor : $currentColor")
                                viewModel.createNewGroup(
                                    GroupDB(
                                        groupId, binding.groupName.text.toString(), "", currentColor, listOfItemSelected, 0
                                    )
                                )
                            }
                        }
                        startActivity(Intent(this, GroupsListActivity::class.java))
                        finish()
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun refreshActivity() {
        startActivity(
            Intent(this@ManageGroupActivity, GroupsListActivity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_NO_ANIMATION
            )
        )
        finish()
    }

    override fun onBackPressed() {
    }
}