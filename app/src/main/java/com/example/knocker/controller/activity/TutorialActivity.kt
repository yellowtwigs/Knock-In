package com.example.knocker.controller.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.viewpager.widget.ViewPager
import com.example.knocker.R
import com.example.knocker.controller.CustomViewPagerAdapter
import java.util.*

class TutorialActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private var tutorial_ViewPager: ViewPager? = null
    private var tutorial_Skip: AppCompatButton? = null
    private var tutorial_CustomViewPagerAdapter: CustomViewPagerAdapter? = null
    private var tutorial_ListOfTutorialVideo: ArrayList<Uri> = ArrayList()
    private var tutorial_ListOfTitle: ArrayList<String> = ArrayList()
    private var alert_dialog_tutorial_notification_close: AppCompatImageView? = null

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        tutorial_ListOfTitle.add("Monochannel")
        tutorial_ListOfTitle.add("Multichannel")
        tutorial_ListOfTitle.add("Notification Priorité 2")

        //region =========================================== Uri ============================================

        tutorial_ListOfTutorialVideo.add(Uri.parse("android.resource://" + (packageName + "/" + R.raw.monochannel_sms)))
        tutorial_ListOfTutorialVideo.add(Uri.parse("android.resource://" + (packageName + "/" + R.raw.multichannel_sms)))
        tutorial_ListOfTutorialVideo.add(Uri.parse("android.resource://" + (packageName + "/" + R.raw.tutorial_priority)))

        //endregion

        //region ======================================= FindViewById =======================================

        tutorial_ViewPager = findViewById(R.id.tutorial_view_pager)
        tutorial_CustomViewPagerAdapter = CustomViewPagerAdapter(supportFragmentManager, tutorial_ListOfTutorialVideo, tutorial_ListOfTitle)
        alert_dialog_tutorial_notification_close = findViewById<AppCompatImageView>(R.id.tutorial_notification_close)

        //endregion

        //region ========================================= Adapter ==========================================

        tutorial_ViewPager!!.adapter = tutorial_CustomViewPagerAdapter

        //endregion

        //region ======================================== Listeners =========================================

        alert_dialog_tutorial_notification_close!!.setOnClickListener {
            startActivity(Intent(this@TutorialActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
        }

        //endregion
    }
}