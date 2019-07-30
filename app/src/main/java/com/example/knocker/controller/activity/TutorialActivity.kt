package com.example.knocker.controller.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.viewpager.widget.ViewPager
import com.example.knocker.R
import com.example.knocker.controller.CustomViewPagerAdapter
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubePlayerView
import java.util.*
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer



class TutorialActivity : YouTubeBaseActivity() {

    //region ========================================== Var or Val ==========================================

    private var tutorial_ViewPager: ViewPager? = null
    private var tutorial_CustomViewPagerAdapter: CustomViewPagerAdapter? = null
    private var tutorial_ListOfTutorialVideo: ArrayList<Uri> = ArrayList()
    private var tutorial_ListOfVideoId: ArrayList<String> = ArrayList()
    private var tutorial_ListOfTitle: ArrayList<String> = ArrayList()

    private var alert_dialog_tutorial_notification_close: AppCompatImageView? = null
    private var alert_dialog_tutorial_YoutubeView: YouTubePlayerView? = null
    private var alert_dialog_tutorial_ButtonPlay: AppCompatButton? = null

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

//        tutorial_ListOfTitle.add("Monochannel")
//        tutorial_ListOfTitle.add("Multichannel")
//        tutorial_ListOfTitle.add("Notification Priorité 2")

        //region =========================================== Uri ============================================

//        tutorial_ListOfTutorialVideo.add(Uri.parse("android.resource://" + (packageName + "/" + R.raw.monochannel_sms)))
//        tutorial_ListOfTutorialVideo.add(Uri.parse("android.resource://" + (packageName + "/" + R.raw.multichannel_sms)))
//        tutorial_ListOfTutorialVideo.add(Uri.parse("android.resource://" + (packageName + "/" + R.raw.tutorial_priority)))

        //endregion

        //region ====================================== ListOfVideoId =======================================

        tutorial_ListOfVideoId.add("3qKeXl3WabM")

        //endregion


        //region ======================================= FindViewById =======================================

//        tutorial_ViewPager = findViewById(R.id.tutorial_view_pager)
//        tutorial_CustomViewPagerAdapter = CustomViewPagerAdapter(supportFragmentManager, tutorial_ListOfTutorialVideo, tutorial_ListOfTitle)
        alert_dialog_tutorial_notification_close = findViewById(R.id.tutorial_notification_close)
        alert_dialog_tutorial_YoutubeView = findViewById(R.id.youtubePlayerView)
        alert_dialog_tutorial_ButtonPlay = findViewById(R.id.buttonPlay)

        //endregion

        //region ========================================= Adapter ==========================================

//        tutorial_ViewPager!!.adapter = tutorial_CustomViewPagerAdapter

        //endregion

        //region ======================================== Listeners =========================================

        alert_dialog_tutorial_notification_close!!.setOnClickListener {
            startActivity(Intent(this@TutorialActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
        }

        alert_dialog_tutorial_ButtonPlay!!.setOnClickListener {
            val videoId = tutorial_ListOfVideoId[0]
            playVideo(videoId, alert_dialog_tutorial_YoutubeView!!)
        }

        //endregion
    }

    private fun playVideo(videoId: String, youTubePlayerView: YouTubePlayerView) {
        //initialize youtube player view
        youTubePlayerView.initialize("YOUR API KEY HERE",
                object : YouTubePlayer.OnInitializedListener {
                    override fun onInitializationSuccess(provider: YouTubePlayer.Provider,
                                                         youTubePlayer: YouTubePlayer, b: Boolean) {
                        youTubePlayer.cueVideo(videoId)
                    }

                    override fun onInitializationFailure(provider: YouTubePlayer.Provider,
                                                         youTubeInitializationResult: YouTubeInitializationResult) {

                    }
                })
    }
}