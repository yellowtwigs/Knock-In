package com.example.knocker.controller.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
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

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        tutorial_ListOfTitle.add("Activation des notifications")
        tutorial_ListOfTitle.add("Multichannel et multiselect")
        tutorial_ListOfTitle.add("Changement de priorité")
        tutorial_ListOfTitle.add("Modification de l'affichage")

        //region =========================================== Uri ============================================

        tutorial_ListOfTutorialVideo.add(Uri.parse("android.resource://" + (packageName + "/" + R.raw.activate_notif)))
        tutorial_ListOfTutorialVideo.add(Uri.parse("android.resource://" + (packageName + "/" + R.raw.multichannel)))

        //endregion

        //region ======================================= FindViewById =======================================

        tutorial_ViewPager = findViewById(R.id.tutorial_view_pager)
        tutorial_Skip = findViewById(R.id.tutorial_skip)
        tutorial_CustomViewPagerAdapter = CustomViewPagerAdapter(supportFragmentManager, tutorial_ListOfTutorialVideo, tutorial_ListOfTitle)

        //endregion

        //region ========================================= Adapter ==========================================

        tutorial_ViewPager!!.adapter = tutorial_CustomViewPagerAdapter

        //endregion

        //region ======================================== Listeners =========================================

        tutorial_Skip!!.setOnClickListener {
            startActivity(Intent(this@TutorialActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
        }

        //endregion
    }
}