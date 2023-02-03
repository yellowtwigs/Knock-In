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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityManageGroupBinding
import com.yellowtwigs.knockin.model.database.data.GroupDB
import com.yellowtwigs.knockin.ui.groups.list.GroupsListActivity
import com.yellowtwigs.knockin.ui.groups.manage_group.data.ManageGroupViewState
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkTheme
import com.yellowtwigs.knockin.utils.EveryActivityUtils.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManageGroupActivity : AppCompatActivity() {

    private val viewModel: ManageGroupViewModel by viewModels()
    private lateinit var binding: ActivityManageGroupBinding
    private val listOfItemSelected = arrayListOf<String>()

    private var groupId = -1
    private lateinit var currentGroup: ManageGroupViewState
    private var currentColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkTheme(this)

        binding = ActivityManageGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        groupId = intent.getIntExtra("GroupId", -1)

        Log.i("MultiSelectGroup", "${intent.getIntegerArrayListExtra("contacts")}")
        intent.getIntegerArrayListExtra("contacts")?.forEach { id ->
            id?.let {
                listOfItemSelected.add(it.toString())
            }
        }

        if (groupId != -1) {
            viewModel.setGroupById(groupId)
        }

        setupToolbar()
        setupRecyclerView()
    }

    //region =========================================== SETUP UI ===========================================

    private fun setupToolbar() {
        binding.toolbar.apply {
            setSupportActionBar(this)
            val actionbar = supportActionBar
            actionbar?.let {
                it.setDisplayHomeAsUpEnabled(true)
                it.setHomeAsUpIndicator(R.drawable.ic_close)
                it.title = ""
            }
        }

    }

    private fun setupRecyclerView() {
        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val nbGrid = sharedPreferences.getInt("gridview", 1)

        if (nbGrid == 1) {
            binding.contacts.apply {
                viewModel.getManageGroupViewState().observe(this@ManageGroupActivity) { manageGroupViewState ->
                    currentGroup = manageGroupViewState
                    currentColor = manageGroupViewState.section_color

                    setupSectionColorSelection()

                    binding.groupName.setText(manageGroupViewState.groupName)
                    listOfItemSelected.addAll(manageGroupViewState.listOfIds)

                    val manageGroupListAdapter = ContactManageGroupListAdapter(
                        this@ManageGroupActivity, listOfItemSelected
                    ) { id ->
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
                viewModel.getManageGroupViewState().observe(this@ManageGroupActivity) { manageGroupViewState ->
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

//    fun gridMultiSelectItemClick(position: Int) { ///// duplicata à changer vite
//        if (listOfItemSelected.contains(gestionnaireContacts!!.contactList[position])) {
//            listOfItemSelected.remove(gestionnaireContacts!!.contactList[position])
//            selectContact!!.remove(gestionnaireContacts!!.contactList[position].contactDB!!)
//        } else {
//            listtest++
//            listOfItemSelected.add(gestionnaireContacts!!.contactList[position])
//            //
//            var contact = gestionnaireContacts!!.contactList[position].contactDB!!
//            selectContact!!.add(gestionnaireContacts!!.contactList[position].contactDB!!)
//        }
//    }

//    fun listMultiSelectItemClick(position: Int) {
//        if (listOfItemSelected.contains(gestionnaireContacts!!.contactList[position])) {
//            listOfItemSelected.remove(gestionnaireContacts!!.contactList[position])
//
//            selectContact!!.remove(gestionnaireContacts!!.contactList[position].contactDB!!)
//        } else {
//            test++
//            listOfItemSelected.add(gestionnaireContacts!!.contactList[position])
//
//            var contact = gestionnaireContacts!!.contactList[position].contactDB!!
//            selectContact!!.add(gestionnaireContacts!!.contactList[position].contactDB!!)
//        }
//    }

//    private fun addToGroup(listContact: List<ContactDB>?, name: String) {
//        val group = GroupDB(null, name, "", -500138) // création de l'objet groupe
//        var counter = 0
//        var alreadyExist = false
//
//        while (counter < contactsDatabase?.GroupsDao()!!
//                .getAllGroupsByNameAZ().size
//        ) { //Nous vérifions que le nom de groupe ne correspond à aucun autre groupe
//            if (name == contactsDatabase?.GroupsDao()!!
//                    .getAllGroupsByNameAZ()[counter].groupDB!!.name
//            ) {
//                alreadyExist = true
//                break
//            }
//            counter++
//        }
//
//        if (alreadyExist) {//Si il existe Nous prévenons l'utilisateur sinon nous créons le groupe
//            Toast.makeText(this, "Ce groupe existe déjà", Toast.LENGTH_LONG).show()
//        } else {
//            val groupId = contactsDatabase!!.GroupsDao().insert(group)
//            listContact!!.forEach {
//                val link = LinkContactGroup(groupId!!.toInt(), it.id)
////                println("contact db id" + contactsDatabase!!.LinkContactGroupDao().insert(link))
//            }
//            println(contactsDatabase!!.GroupsDao().getAllGroupsByNameAZ())
//            refreshActivity()
//        }
//    }

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