package com.example.knocker.controller.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
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
    private var tutorial_ListOfTutorialImages: ArrayList<Drawable> = ArrayList()

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        //region ======================================= FindViewById =======================================

        tutorial_ViewPager = findViewById(R.id.tutorial_view_pager)
        tutorial_Skip = findViewById(R.id.tutorial_skip)
        tutorial_CustomViewPagerAdapter = CustomViewPagerAdapter(supportFragmentManager, tutorial_ListOfTutorialImages)

        //endregion

        tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.tuto_1)!!)
        tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.tuto_2)!!)
        tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.tuto_3)!!)
        tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.tuto_4)!!)
        tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.tuto_5)!!)

        //region ========================================= Adapter ==========================================

        tutorial_ViewPager!!.adapter = tutorial_CustomViewPagerAdapter

        //endregion

        //region ======================================== Listeners =========================================

        tutorial_Skip!!.setOnClickListener {
            val intent = Intent(this@TutorialActivity, MainActivity::class.java)
            intent.putExtra("fromStartActivity", true)
            startActivity(intent)
            finish()
        }

        //endregion
    }
}