package com.yellowtwigs.knockin.controller.activity.bubbles

import android.app.Notification
import android.app.PendingIntent
import android.app.Person
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.yellowtwigs.knockin.R

class BubbleActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bubble)

        // Create bubble intent
        val bubbleIntent = PendingIntent.getActivity(this, 0, Intent(this, BubbleActivity::class.java), 0 /* flags */)

        // Create bubble metadata
        val bubbleData = Notification.BubbleMetadata.Builder()
                .setDesiredHeight(600)
                .setIcon(Icon.createWithResource(this, R.drawable.ic_app_image))
                .setIntent(bubbleIntent)
                .build()

        // Create notification
        val chatBot = Person.Builder()
                .setBot(true)
                .setName("BubbleBot")
                .setImportant(true)
                .build()

        val builder = Notification.Builder(this, "CHANNEL_ID")
                .setContentIntent(bubbleIntent)
                .setSmallIcon(R.drawable.ic_app_image)
                .setBubbleMetadata(bubbleData)
                .addPerson(chatBot)
    }
}
