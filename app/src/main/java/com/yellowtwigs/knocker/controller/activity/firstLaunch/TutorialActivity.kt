package com.yellowtwigs.knocker.controller.activity.firstLaunch

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yellowtwigs.knocker.R
import com.yellowtwigs.knocker.controller.TutorialViewPagerAdapter
import com.yellowtwigs.knocker.controller.activity.MainActivity
import com.yellowtwigs.knocker.controller.activity.NotificationHistoryActivity
import com.yellowtwigs.knocker.controller.activity.CockpitActivity
import com.yellowtwigs.knocker.controller.activity.group.GroupManagerActivity
import java.util.*

class TutorialActivity : AppCompatActivity() {

    //region ========================================== Var or Val ==========================================

    private var tutorial_ViewPager: ViewPager? = null
    private var tutorial_Skip: AppCompatButton? = null
    private var tutorial_CustomViewPagerAdapter: TutorialViewPagerAdapter? = null
    private var tutorial_ListOfTutorialImages: ArrayList<Drawable> = ArrayList()

    private var tutorial_NoSelected_1: AppCompatImageView? = null
    private var tutorial_Selected_1: AppCompatImageView? = null

    private var tutorial_NoSelected_2: AppCompatImageView? = null
    private var tutorial_Selected_2: AppCompatImageView? = null

    private var tutorial_NoSelected_3: AppCompatImageView? = null
    private var tutorial_Selected_3: AppCompatImageView? = null

    private var tutorial_NoSelected_4: AppCompatImageView? = null
    private var tutorial_Selected_4: AppCompatImageView? = null

    private var tutorial_NoSelected_5: AppCompatImageView? = null
    private var tutorial_Selected_5: AppCompatImageView? = null

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        //region ======================================= FindViewById =======================================

        tutorial_ViewPager = findViewById(R.id.tutorial_view_pager)
        tutorial_Skip = this.findViewById(R.id.tutorial_skip)

        tutorial_NoSelected_1 = findViewById(R.id.tutorial_no_selected_1)
        tutorial_Selected_1 = findViewById(R.id.tutorial_selected_1)
        tutorial_NoSelected_2 = findViewById(R.id.tutorial_no_selected_2)
        tutorial_Selected_2 = findViewById(R.id.tutorial_selected_2)
        tutorial_NoSelected_3 = findViewById(R.id.tutorial_no_selected_3)
        tutorial_Selected_3 = findViewById(R.id.tutorial_selected_3)
        tutorial_NoSelected_4 = findViewById(R.id.tutorial_no_selected_4)
        tutorial_Selected_4 = findViewById(R.id.tutorial_selected_4)
        tutorial_NoSelected_5 = findViewById(R.id.tutorial_no_selected_5)
        tutorial_Selected_5 = findViewById(R.id.tutorial_selected_5)

        //endregion

        //region ========================================= Adapter ==========================================

        val intent = intent
        val fromImportContact = intent.getBooleanExtra("fromImportContact", false)
        val fromStartActivity = intent.getBooleanExtra("fromStartActivity", false)
        val fromMainActivity = intent.getBooleanExtra("fromMainActivity", false)
        val fromNotificationHistoryActivity = intent.getBooleanExtra("fromNotificationHistoryActivity", false)
        val fromGroupManagerActivity = intent.getBooleanExtra("fromGroupManagerActivity", false)
        val fromPhoneLogActivity = intent.getBooleanExtra("fromPhoneLogActivity", false)

        when {
            fromImportContact -> {

                if (Locale.getDefault().displayLanguage == "français") {

                    tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.presentation_1)!!)
                    tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.presentation_2)!!)
                    tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.presentation_4)!!)
                    tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.presentation_3)!!)
                    tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.presentation_5)!!)

                } else if (Locale.getDefault().displayLanguage == "English") {

                    tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.presentation_1_en)!!)
                    tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.presentation_2_en)!!)
                    tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.presentation_4_en)!!)
                    tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.presentation_3_en)!!)
                    tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.presentation_5_en)!!)

                }
            }

            fromStartActivity -> {
                if (Locale.getDefault().displayLanguage == "français") {

                    tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.presentation_1)!!)
                    tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.presentation_2)!!)
                    tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.presentation_4)!!)
                    tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.presentation_3)!!)
                    tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.presentation_5)!!)

                } else if (Locale.getDefault().displayLanguage == "English") {

                    tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.presentation_1_en)!!)
                    tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.presentation_2_en)!!)
                    tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.presentation_4_en)!!)
                    tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.presentation_3_en)!!)
                    tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.presentation_5_en)!!)

                }
            }

            fromMainActivity -> {
                tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.tuto_multi_gif)!!)

                tutorial_Skip!!.setText(R.string.tutorial_activity_back)
            }

            fromNotificationHistoryActivity -> {
                tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.presentation_4)!!)
                tutorial_Skip!!.setText(R.string.tutorial_activity_back)
            }

            fromGroupManagerActivity -> {
                tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.presentation_3)!!)
                tutorial_Skip!!.setText(R.string.tutorial_activity_back)
            }

            fromPhoneLogActivity -> {
                tutorial_ListOfTutorialImages.add(getDrawable(R.drawable.presentation_5)!!)
                tutorial_Skip!!.setText(R.string.tutorial_activity_back)
            }
        }

        tutorial_CustomViewPagerAdapter = TutorialViewPagerAdapter(supportFragmentManager, tutorial_ListOfTutorialImages, fromMainActivity)

        //endregion

        //region ========================================= Adapter ==========================================

        tutorial_ViewPager!!.adapter = tutorial_CustomViewPagerAdapter

        if (tutorial_CustomViewPagerAdapter!!.count < 2) {

            tutorial_NoSelected_1!!.visibility = View.GONE
            tutorial_Selected_1!!.visibility = View.GONE
            tutorial_NoSelected_2!!.visibility = View.GONE
            tutorial_Selected_2!!.visibility = View.GONE
            tutorial_NoSelected_3!!.visibility = View.GONE
            tutorial_Selected_3!!.visibility = View.GONE
            tutorial_NoSelected_4!!.visibility = View.GONE
            tutorial_Selected_4!!.visibility = View.GONE
            tutorial_NoSelected_5!!.visibility = View.GONE
            tutorial_Selected_5!!.visibility = View.GONE

        }

        tutorial_ViewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                when (position) {
                    0 -> {
                        tutorial_NoSelected_1!!.visibility = View.INVISIBLE
                        tutorial_Selected_1!!.visibility = View.VISIBLE
                        tutorial_NoSelected_2!!.visibility = View.VISIBLE
                        tutorial_Selected_2!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_3!!.visibility = View.VISIBLE
                        tutorial_Selected_3!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_4!!.visibility = View.VISIBLE
                        tutorial_Selected_4!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_5!!.visibility = View.VISIBLE
                        tutorial_Selected_5!!.visibility = View.INVISIBLE
                    }
                    1 -> {
                        tutorial_NoSelected_1!!.visibility = View.VISIBLE
                        tutorial_Selected_1!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_2!!.visibility = View.INVISIBLE
                        tutorial_Selected_2!!.visibility = View.VISIBLE
                        tutorial_NoSelected_3!!.visibility = View.VISIBLE
                        tutorial_Selected_3!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_4!!.visibility = View.VISIBLE
                        tutorial_Selected_4!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_5!!.visibility = View.VISIBLE
                        tutorial_Selected_5!!.visibility = View.INVISIBLE
                    }
                    2 -> {
                        tutorial_NoSelected_1!!.visibility = View.VISIBLE
                        tutorial_Selected_1!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_2!!.visibility = View.VISIBLE
                        tutorial_Selected_2!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_3!!.visibility = View.INVISIBLE
                        tutorial_Selected_3!!.visibility = View.VISIBLE
                        tutorial_NoSelected_4!!.visibility = View.VISIBLE
                        tutorial_Selected_4!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_5!!.visibility = View.VISIBLE
                        tutorial_Selected_5!!.visibility = View.INVISIBLE
                    }
                    3 -> {
                        tutorial_NoSelected_1!!.visibility = View.VISIBLE
                        tutorial_Selected_1!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_2!!.visibility = View.VISIBLE
                        tutorial_Selected_2!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_3!!.visibility = View.VISIBLE
                        tutorial_Selected_3!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_4!!.visibility = View.INVISIBLE
                        tutorial_Selected_4!!.visibility = View.VISIBLE
                        tutorial_NoSelected_5!!.visibility = View.VISIBLE
                        tutorial_Selected_5!!.visibility = View.INVISIBLE
                    }
                    4 -> {
                        tutorial_NoSelected_1!!.visibility = View.VISIBLE
                        tutorial_Selected_1!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_2!!.visibility = View.VISIBLE
                        tutorial_Selected_2!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_3!!.visibility = View.VISIBLE
                        tutorial_Selected_3!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_4!!.visibility = View.VISIBLE
                        tutorial_Selected_4!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_5!!.visibility = View.INVISIBLE
                        tutorial_Selected_5!!.visibility = View.VISIBLE
                    }
                }
            }

            override fun onPageSelected(position: Int) {

                when (position) {
                    0 -> {
                        tutorial_NoSelected_1!!.visibility = View.INVISIBLE
                        tutorial_Selected_1!!.visibility = View.VISIBLE
                        tutorial_NoSelected_2!!.visibility = View.VISIBLE
                        tutorial_Selected_2!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_3!!.visibility = View.VISIBLE
                        tutorial_Selected_3!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_4!!.visibility = View.VISIBLE
                        tutorial_Selected_4!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_5!!.visibility = View.VISIBLE
                        tutorial_Selected_5!!.visibility = View.INVISIBLE
                    }
                    1 -> {
                        tutorial_NoSelected_1!!.visibility = View.VISIBLE
                        tutorial_Selected_1!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_2!!.visibility = View.INVISIBLE
                        tutorial_Selected_2!!.visibility = View.VISIBLE
                        tutorial_NoSelected_3!!.visibility = View.VISIBLE
                        tutorial_Selected_3!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_4!!.visibility = View.VISIBLE
                        tutorial_Selected_4!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_5!!.visibility = View.VISIBLE
                        tutorial_Selected_5!!.visibility = View.INVISIBLE
                    }
                    2 -> {
                        tutorial_NoSelected_1!!.visibility = View.VISIBLE
                        tutorial_Selected_1!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_2!!.visibility = View.VISIBLE
                        tutorial_Selected_2!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_3!!.visibility = View.INVISIBLE
                        tutorial_Selected_3!!.visibility = View.VISIBLE
                        tutorial_NoSelected_4!!.visibility = View.VISIBLE
                        tutorial_Selected_4!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_5!!.visibility = View.VISIBLE
                        tutorial_Selected_5!!.visibility = View.INVISIBLE
                    }
                    3 -> {
                        tutorial_NoSelected_1!!.visibility = View.VISIBLE
                        tutorial_Selected_1!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_2!!.visibility = View.VISIBLE
                        tutorial_Selected_2!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_3!!.visibility = View.VISIBLE
                        tutorial_Selected_3!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_4!!.visibility = View.INVISIBLE
                        tutorial_Selected_4!!.visibility = View.VISIBLE
                        tutorial_NoSelected_5!!.visibility = View.VISIBLE
                        tutorial_Selected_5!!.visibility = View.INVISIBLE
                    }
                    4 -> {
                        tutorial_NoSelected_1!!.visibility = View.VISIBLE
                        tutorial_Selected_1!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_2!!.visibility = View.VISIBLE
                        tutorial_Selected_2!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_3!!.visibility = View.VISIBLE
                        tutorial_Selected_3!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_4!!.visibility = View.VISIBLE
                        tutorial_Selected_4!!.visibility = View.INVISIBLE
                        tutorial_NoSelected_5!!.visibility = View.INVISIBLE
                        tutorial_Selected_5!!.visibility = View.VISIBLE
                    }
                }
            }

        })

        //endregion

        //region ======================================== Listeners =========================================

        tutorial_Skip!!.setOnClickListener {
            when {
                fromImportContact -> goToMainWithIntent()

                fromStartActivity -> goToMainWithIntent()

                fromMainActivity -> {
                    startActivity(Intent(this@TutorialActivity, MainActivity::class.java))
                    finish()
                }

                fromNotificationHistoryActivity -> {
                    startActivity(Intent(this@TutorialActivity, NotificationHistoryActivity::class.java))
                    finish()
                }

                fromGroupManagerActivity -> {
                    startActivity(Intent(this@TutorialActivity, GroupManagerActivity::class.java))
                    finish()
                }

                fromPhoneLogActivity -> {
                    startActivity(Intent(this@TutorialActivity, CockpitActivity::class.java))
                    finish()
                }
            }
        }

        //endregion
    }

    //region ========================================== Functions ===========================================

    fun goToMainWithIntent() {
        val intentToMain = Intent(this@TutorialActivity, MainActivity::class.java)
        intentToMain.putExtra("fromStartActivity", true)
        startActivity(intentToMain)
        finish()
    }

    //endregion
}