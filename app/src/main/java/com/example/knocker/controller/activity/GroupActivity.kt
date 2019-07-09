package com.example.knocker.controller.activity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import com.example.knocker.R
import com.example.knocker.controller.GroupListViewAdapter
import com.example.knocker.model.ContactsRoomDatabase
import com.example.knocker.model.DbWorkerThread
import com.example.knocker.model.ModelDB.GroupWithContact

class GroupActivity : AppCompatActivity() {
    private var group_ContactsDatabase: ContactsRoomDatabase? = null
    private lateinit var group_mDbWorkerThread: DbWorkerThread
    var recycler: ListView?=null ;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)
        recycler=findViewById(R.id.group_list_view_id)

        group_ContactsDatabase=ContactsRoomDatabase.getDatabase(this)

        group_mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        group_mDbWorkerThread.start()
        val group:ArrayList<GroupWithContact> = ArrayList()
        group.addAll(group_ContactsDatabase!!.GroupsDao().getAllGroupsByNameAZ())
        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val len = sharedPreferences.getInt("gridview", 4)

        val adapter= GroupListViewAdapter(group,this,len)
        recycler!!.adapter=adapter
    }

}
