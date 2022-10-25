package com.yellowtwigs.knockin.ui.groups.list

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.net.Uri
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.yellowtwigs.knockin.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yellowtwigs.knockin.databinding.ActivityGroupsListBinding
import com.yellowtwigs.knockin.ui.HelpActivity
import com.yellowtwigs.knockin.ui.settings.ManageMyScreenActivity
import com.yellowtwigs.knockin.ui.first_launch.first_vip_selection.FirstVipSelectionAdapter
import com.yellowtwigs.knockin.ui.contacts.list.ContactsListActivity
import com.yellowtwigs.knockin.ui.groups.manage_group.ManageGroupActivity
import com.yellowtwigs.knockin.ui.groups.list.section.SectionGroupsListAdapter
import com.yellowtwigs.knockin.ui.in_app.PremiumActivity
import com.yellowtwigs.knockin.ui.notifications.history.NotificationsHistoryActivity
import com.yellowtwigs.knockin.ui.notifications.settings.NotificationsSettingsActivity
import com.yellowtwigs.knockin.ui.teleworking.TeleworkingActivity
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GroupsListActivity : AppCompatActivity() {

    private var firstClick: Boolean = true

    //    private var sectionAdapter: SectionGroupAdapter? = null
    private lateinit var firstVipSelectionAdapter: FirstVipSelectionAdapter

    private var settings_left_drawer_ThemeSwitch: SwitchCompat? = null
    private var recyclerLen: Int = 1

    var touchHelper: ItemTouchHelper? = null

    private val groupsListViewModel: GroupsListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkTheme(this)

        val binding = ActivityGroupsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding)
        setupDrawerLayout(binding)
        setupBottomNavigationView(binding)
        setupGroupsList(binding)
        setupFloatingButtons(binding)

        //region ======================================= Listeners ==========================================

//            group_manager_FloatingButtonSend!!.setOnClickListener {
//            val intent = Intent(this@GroupsListActivity, MultiChannelActivity::class.java)
//            val iterator: IntIterator?
//            val listOfIdContactSelected: ArrayList<Int> = ArrayList()
//
////            iterator = (0 until listOfItemSelected.size).iterator()
//
////            for (i in iterator) {
////                listOfIdContactSelected.add(listOfItemSelected[i].getContactId())
////            }
//            intent.putIntegerArrayListExtra("ListContactsSelected", listOfIdContactSelected)
//
//            startActivity(intent)
//            finish()
//        }
//
//        group_manager_FloatingButtonSMS!!.setOnClickListener {
//            val iterator: IntIterator?
//            val listOfPhoneNumberContactSelected: ArrayList<String> = ArrayList()
//
////            iterator = (0 until listOfItemSelected.size).iterator()
//
////            for (i in iterator) {
////                listOfPhoneNumberContactSelected.add(listOfItemSelected[i].getFirstPhoneNumber())
////            }
//            monoChannelSmsClick(listOfPhoneNumberContactSelected)
//        }
//
//        group_manager_FloatingButtonMail!!.setOnClickListener {
//            val iterator: IntIterator?
//            val listOfMailContactSelected: ArrayList<String> = ArrayList()
//
////            iterator = (0 until listOfItemSelected.size).iterator()
//
////            for (i in iterator) {
////                listOfMailContactSelected.add(listOfItemSelected[i].getFirstMail())
////            }
//            monoChannelMailClick(listOfMailContactSelected)
//        }
//
//        touchHelper = ItemTouchHelper(object :
//            ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
//            override fun onMove(
//                p0: RecyclerView,
//                p1: RecyclerView.ViewHolder,
//                p2: RecyclerView.ViewHolder
//            ): Boolean {
//                val sourcePosition = p1.adapterPosition
//                val targetPosition = p2.adapterPosition
//                return true
//            }
//
//            override fun onSwiped(p0: RecyclerView.ViewHolder, p1: Int) {
//            }
//        })
//
//        touchHelper!!.attachToRecyclerView(group_manager_RecyclerView!!)

        //endregion
    }

    private fun setupFloatingButtons(binding: ActivityGroupsListBinding) {
        binding.addNewGroup.setOnClickListener {
            val intent = Intent(this@GroupsListActivity, ManageGroupActivity::class.java)
            startActivity(intent)
        }

    }

    //region =========================================== TOOLBAR ============================================

    private fun setupToolbar(binding: ActivityGroupsListBinding) {
        binding.help.setOnClickListener {
            if (Resources.getSystem().configuration.locale.language == "fr") {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.yellowtwigs.com/aide-en-ligne-groupes")
                )
                startActivity(browserIntent)
            } else {
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://www.yellowtwigs.com/help-groups"))
                startActivity(browserIntent)
            }
        }

//        setSupportActionBar(binding.toolbarMenu)
//        val actionbar = supportActionBar
//        actionbar?.title = ""
//
//        binding.toolbarSearch.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//            }
//
//            override fun onTextChanged(query: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                contactsListViewModel.setSearchTextChanged(query.toString())
//            }
//
//            override fun afterTextChanged(p0: Editable?) {
//            }
//        })
    }

    //endregion

    //region ======================================== DRAWER LAYOUT =========================================

    private fun setupDrawerLayout(binding: ActivityGroupsListBinding) {
        binding.drawerLayout.apply {
            val menu = binding.navView.menu
            val navItem = menu.findItem(R.id.nav_home)
            navItem.isChecked = true
            menu.getItem(0).isChecked = true

            binding.navView.setNavigationItemSelectedListener { menuItem ->
                closeDrawers()

                val itemLayout = findViewById<ConstraintLayout>(R.id.teleworking_item)
                val itemText = findViewById<AppCompatTextView>(R.id.teleworking_item_text)

                itemText.text =
                    "${getString(R.string.teleworking)} ${getString(R.string.left_drawer_settings)}"

                itemLayout.setOnClickListener {
                    startActivity(
                        Intent(
                            this@GroupsListActivity,
                            TeleworkingActivity::class.java
                        )
                    )
                }
                when (menuItem.itemId) {
                    R.id.nav_notifications -> startActivity(
                        Intent(this@GroupsListActivity, NotificationsSettingsActivity::class.java)
                    )
                    R.id.nav_in_app -> startActivity(
                        Intent(this@GroupsListActivity, PremiumActivity::class.java)
                    )
                    R.id.nav_manage_screen -> startActivity(
                        Intent(this@GroupsListActivity, ManageMyScreenActivity::class.java)
                    )
                    R.id.nav_help -> startActivity(
                        Intent(this@GroupsListActivity, HelpActivity::class.java)
                    )
                }

                true
            }

            binding.openDrawer.setOnClickListener {
                if (isOpen) {
                    closeDrawer(GravityCompat.START)
                } else {
                    openDrawer(GravityCompat.START)
                }
            }
        }
    }

    //endregion

    //region =========================================== SETUP UI ===========================================

    private fun setupBottomNavigationView(binding: ActivityGroupsListBinding) {
        binding.navigation.menu.getItem(1).isChecked = true
        binding.navigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_contacts -> {
                    startActivity(
                        Intent(
                            this@GroupsListActivity,
                            ContactsListActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_notifcations -> {
                    startActivity(
                        Intent(
                            this@GroupsListActivity,
                            NotificationsHistoryActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_cockpit -> {
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })
    }

    private fun setupGroupsList(binding: ActivityGroupsListBinding) {
        val sectionGroupsListAdapter = SectionGroupsListAdapter(this) { id ->
            CoroutineScope(Dispatchers.IO).launch {
                groupsListViewModel.deleteGroupById(id)
            }
        }

        binding.recyclerView.apply {
            groupsListViewModel.getAllGroups().observe(this@GroupsListActivity) { groups ->
                sectionGroupsListAdapter.submitList(groups)
            }
            adapter = sectionGroupsListAdapter
            layoutManager = LinearLayoutManager(context)
        }

//        if (len >= 4) {
////            groupAdapter = GroupAdapter(this, gestionnaireContacts!!, len)
//            group_manager_RecyclerView!!.layoutManager = GridLayoutManager(this, len)
//        } else {
////            groupAdapter = GroupAdapter(this, gestionnaireContacts!!, len)
//            group_manager_RecyclerView!!.layoutManager = LinearLayoutManager(this)
//            group_manager_RecyclerView!!.recycledViewPool.setMaxRecycledViews(0, 0)
//        }
//
//        val sectionList = arrayOfNulls<SectionGroupAdapter.Section>(sections.size)
////        sectionAdapter = SectionGroupAdapter(
////            this, R.layout.group_manager_recycler_adapter_section, group_manager_RecyclerView!!,
////            groupAdapter!! as RecyclerView.Adapter<RecyclerView.ViewHolder>, len
////        )
//        sectionAdapter!!.setSections(sections.toArray(sectionList))
//        group_manager_RecyclerView!!.adapter = sectionAdapter
    }

    //endregion

    //region ========================================= Functions ============================================

//    fun recyclerMultiSelectItemClick(position: Int) {
//        if (listOfItemSelected.contains(gestionnaireContacts!!.contactList[position])) {
//            listOfItemSelected.remove(gestionnaireContacts!!.contactList[position])
//            verifiedContactsChannel(listOfItemSelected)
//        } else {
//            listOfItemSelected.add(gestionnaireContacts!!.contactList[position])
//
//            group_manager_FloatingButtonSMS!!.visibility = View.GONE
//            group_manager_FloatingButtonMail!!.visibility = View.GONE
//            group_manager_FloatingButtonSend!!.visibility = View.GONE
//
//            verifiedContactsChannel(listOfItemSelected)
//        }
//
//        val i = listOfItemSelected.size
//
//        if (listOfItemSelected.size == 1 && firstClick) {
//            Toast.makeText(this, R.string.main_toast_multi_select_actived, Toast.LENGTH_SHORT)
//                .show()
//            firstClick = false
//
//        } else if (listOfItemSelected.size == 0) {
//            Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_SHORT)
//                .show()
//
//            group_manager_FloatingButtonSMS!!.visibility = View.GONE
//            group_manager_FloatingButtonMail!!.visibility = View.GONE
//
//            firstClick = true
//        }
//
////        if (listOfItemSelected.size == 1) {
////            main_ToolbarMultiSelectModeTitle!!.text = i.toString() + " " + getString(R.string.main_toast_multi_select_mode_selected)
////        } else if (listOfItemSelected.size > 1) {
////
////            Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_LONG).show()
////            main_ToolbarMultiSelectModeTitle!!.text = i.toString() + " " + getString(R.string.main_toast_multi_select_mode_selected_more_than_one)
////        }
//    }

//    fun recyclerMultiSelectItemClick(
//        position: Int,
//        secondClickLibelle: Boolean,
//        fromLibelleClick: Boolean
//    ) {
//        if (!secondClickLibelle) {
//            if (listOfItemSelected.contains(gestionnaireContacts!!.contactList[position])) {
//                listOfItemSelected.remove(gestionnaireContacts!!.contactList[position])
//
//                if (listOfItemSelected.size == 0) {
//                    group_manager_FloatingButtonSMS!!.visibility = View.GONE
//                    group_manager_FloatingButtonMail!!.visibility = View.GONE
//                    group_manager_FloatingButtonSend!!.visibility = View.GONE
//                }
//
//            } else {
//                listOfItemSelected.add(gestionnaireContacts!!.contactList[position])
//                verifiedContactsChannel(listOfItemSelected)
//            }
//
//            if (fromLibelleClick) {
//                firstClick = true
//            }
//        } else {
//            if (listOfItemSelected.contains(gestionnaireContacts!!.contactList[position])) {
//                listOfItemSelected.remove(gestionnaireContacts!!.contactList[position])
//            }
//        }
//
//
//        if (listOfItemSelected.size == 1 && firstClick) {
//            Toast.makeText(this, R.string.main_toast_multi_select_actived, Toast.LENGTH_LONG).show()
//            firstClick = false
//        } else if (listOfItemSelected.size == 0) {
//            listOfItemSelected.clear()
//            group_manager_FloatingButtonSMS!!.visibility = View.GONE
//            group_manager_FloatingButtonMail!!.visibility = View.GONE
//            group_manager_FloatingButtonSend!!.visibility = View.GONE
//            Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_LONG)
//                .show()
//            firstClick = true
//        }
//    }

//    fun gridLongItemClick(position: Int) {
//        if (listOfItemSelected.contains(
//                gestionnaireContacts!!.contactList.get(position)
//            )
//        ) {
//            listOfItemSelected.remove(
//                gestionnaireContacts!!.contactList.get(position)
//            )
//        } else {
//            listOfItemSelected.add(
//                gestionnaireContacts!!.contactList.get(position)
//            )
//            verifiedContactsChannel(listOfItemSelected)
//        }
//
//        if (listOfItemSelected.size == 1 && firstClick) {
//            Toast.makeText(this, R.string.main_toast_multi_select_actived, Toast.LENGTH_LONG).show()
//            firstClick = false
//        } else if (listOfItemSelected.size == 0) {
//            Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_LONG)
//                .show()
//            group_manager_FloatingButtonSend!!.visibility = View.GONE
//            group_manager_FloatingButtonMail!!.visibility = View.GONE
//            group_manager_FloatingButtonSMS!!.visibility = View.GONE
//            group_manager_FloatingButtonAddNewGroup!!.visibility = View.GONE
//            firstClick = true
//        }
//    }

//    private fun verifiedContactsChannel(listOfItemSelected: ArrayList<ContactWithAllInformation>) {
//        val iterator = (0 until listOfItemSelected.size).iterator()
//        var allContactsHaveMail = true
//        var allContactsHavePhoneNumber = true
//
//        for (i in iterator) {
//            if (listOfItemSelected[i].getFirstMail() == "") {
//                allContactsHaveMail = false
//            }
//
//            if (listOfItemSelected[i].getFirstPhoneNumber() == "") {
//                allContactsHavePhoneNumber = false
//            }
//        }
//        group_manager_FloatingButtonAddNewGroup!!.visibility = View.GONE
//        group_manager_FloatingButtonSend!!.visibility = View.VISIBLE
//        //var i = 2
//        val metrics = DisplayMetrics()
//        this.windowManager.defaultDisplay.getMetrics(metrics)
//        val margin = (0.2
//                * metrics.densityDpi).toInt()
//        println("metric smartphone" + metrics.densityDpi)
//        if (allContactsHavePhoneNumber) {
//            group_manager_FloatingButtonSMS!!.visibility = View.VISIBLE
//            //    i++
//        } else {
//            println("false phoneNumber")
//            group_manager_FloatingButtonSMS!!.visibility = View.GONE
//        }
//        if (allContactsHaveMail) {
//            group_manager_FloatingButtonMail!!.visibility = View.VISIBLE
//            val params: ViewGroup.MarginLayoutParams =
//                group_manager_FloatingButtonMail!!.layoutParams as ViewGroup.MarginLayoutParams
//            params.bottomMargin = margin
//            group_manager_FloatingButtonMail!!.layoutParams = params
//            println("height of floating mail" + group_manager_FloatingButtonMail!!.height)
//        } else {
//            println("false mail")
//            group_manager_FloatingButtonMail!!.visibility = View.GONE
//        }
//    }

//    fun refreshList() {
//        group_manager_ContactsDatabase = ContactsDatabase.getDatabase(this)
//        val group: ArrayList<GroupWithContact> = ArrayList()
//        group.addAll(group_manager_ContactsDatabase!!.GroupsDao().getAllGroupsByNameAZ())
//        val sharedPreferences = getSharedPreferences("group", MODE_PRIVATE)
//        val len = sharedPreferences.getInt("gridview", recyclerLen)
//        val listContactGroup: ArrayList<ContactWithAllInformation> = arrayListOf()
//        val sections = ArrayList<SectionGroupAdapter.Section>()
//        var position = 0
//        for (i in group) {
//            val list = i.getListContact(this)
//            listContactGroup.addAll(list)
//            sections.add(SectionGroupAdapter.Section(position, i.groupDB!!.name, i.groupDB!!.id!!))
//            position += list.size
//        }
//        gestionnaireContacts = ContactManager(this)
//        gestionnaireContacts!!.contactList = listContactGroup
//        if (len >= 3) {
//            groupAdapter = GroupAdapter(this, gestionnaireContacts!!, len)
//            group_manager_RecyclerView!!.layoutManager = GridLayoutManager(this, len)
//        } else {
//            groupAdapter = GroupAdapter(this, gestionnaireContacts!!, 4)
//            group_manager_RecyclerView!!.layoutManager = GridLayoutManager(this, 4)
//        }
//        val sectionList = arrayOfNulls<SectionGroupAdapter.Section>(sections.size)
//        val sectionAdapter = SectionGroupAdapter(
//            this,
//            R.layout.group_manager_recycler_adapter_section,
//            group_manager_RecyclerView!!,
//            groupAdapter!! as RecyclerView.Adapter<RecyclerView.ViewHolder>,
//            len
//        )
//        sectionAdapter.setSections(sections.toArray(sectionList))
//        group_manager_RecyclerView!!.adapter = sectionAdapter
//    }

    fun refreshActivity() {
        startActivity(Intent(this@GroupsListActivity, GroupsListActivity::class.java))
    }

    private fun monoChannelSmsClick(listOfPhoneNumber: ArrayList<String>) {
        var message = "smsto:" + listOfPhoneNumber[0]
        for (i in 0 until listOfPhoneNumber.size) {
            message += ";" + listOfPhoneNumber[i]
        }
        startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(message)))
    }

    private fun monoChannelMailClick(listOfMail: ArrayList<String>) {
        val contact = listOfMail.toArray(arrayOfNulls<String>(listOfMail.size))
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(
            Intent.EXTRA_EMAIL,
            contact
        )/*listOfMail.toArray(new String[listOfMail.size()]*/
        intent.data = Uri.parse("mailto:")
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_SUBJECT, "")
        intent.putExtra(Intent.EXTRA_TEXT, "")
        startActivity(intent)
    }

//    fun gridMultiSelectItemClick(len: Int, position: Int, firstPosVis: Int) {
//        val multiSelectAdapter = MultiSelectAdapter(this, false) { position ->
//            multiSelectAdapter.itemSelected(position)
//        }
//        multiSelectAdapter.submitList(null)
//        multiSelectAdapter.submitList(gestionnaireContacts?.contactList)
//
//        group_manager_RecyclerView?.adapter = multiSelectAdapter
//        group_manager_RecyclerView?.setHasFixedSize(true)
//        group_manager_RecyclerView?.layoutManager =
//            GridLayoutManager(this, 4, RecyclerView.VERTICAL, false)
//        multiSelectAdapter.itemSelected(position)
//        firstClick = true
//
//        if (listOfItemSelected.contains(gestionnaireContacts!!.contactList[position])) {
//            listOfItemSelected.remove(gestionnaireContacts!!.contactList[position])
//        } else {
//            listOfItemSelected.add(gestionnaireContacts!!.contactList[position])
//        }
//
//        verifiedContactsChannel(listOfItemSelected)
//        Toast.makeText(this, R.string.main_toast_multi_select_actived, Toast.LENGTH_SHORT).show()
//    }

//    fun clickGroupGrid(
//        len: Int,
//        positions: List<Int>,
//        firstPosVis: Int,
//        secondClickLibelle: Boolean,
//        fromLibelleClick: Boolean
//    ) {
//        val multiSelectAdapter = MultiSelectAdapter(this, false) { position ->
//            multiSelectAdapter.itemSelected(position)
//        }
//        multiSelectAdapter.submitList(null)
//        multiSelectAdapter.submitList(gestionnaireContacts?.contactList)
//
//        group_manager_RecyclerView?.adapter = multiSelectAdapter
//        group_manager_RecyclerView?.setHasFixedSize(true)
//        group_manager_RecyclerView?.layoutManager =
//            GridLayoutManager(this, 4, RecyclerView.VERTICAL, false)
//        if (!secondClickLibelle) {
//            firstClick = true
//
//            for (position in positions) {
//                multiSelectAdapter.itemSelected(position)
//            }
//
//            verifiedContactsChannel(listOfItemSelected)
//
//            if (fromLibelleClick && firstClick) {
//                group_manager_FloatingButtonAddNewGroup!!.visibility = View.GONE
//            }
//
//            Toast.makeText(this, R.string.main_toast_multi_select_actived, Toast.LENGTH_SHORT)
//                .show()
//        } else {
//            group_manager_FloatingButtonSend!!.visibility = View.GONE
//            group_manager_FloatingButtonSMS!!.visibility = View.GONE
//            group_manager_FloatingButtonMail!!.visibility = View.GONE
//            group_manager_FloatingButtonAddNewGroup!!.visibility = View.GONE
//
////            adapter.itemDeselected()
////            gridViewAdapter = ContactGridViewAdapter(this, gestionnaireContacts, len)
////            group_GridView!!.adapter = gridViewAdapter
//
//            Toast.makeText(this, R.string.main_toast_multi_select_deactived, Toast.LENGTH_SHORT)
//                .show()
//        }
//    }

    //endregion
}