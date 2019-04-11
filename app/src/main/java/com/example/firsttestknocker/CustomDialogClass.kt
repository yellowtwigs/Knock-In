package com.example.firsttestknocker

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView

class CustomDialogClass(var activity: Activity) : Dialog(activity), android.view.View.OnClickListener {
    var dialog: Dialog? = null
    private var custom_popup_YesButton: Button? = null
    private var custom_popup_NoButton: Button? = null
    private var custom_popup_LongText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.custom_popup)
        custom_popup_YesButton = findViewById(R.id.custom_popup_yes_btn)
        custom_popup_NoButton = findViewById(R.id.custom_popup_no_btn)
        custom_popup_LongText = findViewById(R.id.custom_popup_long_text_id)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.custom_popup_yes_btn -> activity.finish()
            R.id.custom_popup_no_btn -> dismiss()
            else -> {
            }
        }
        dismiss()
    }

    fun setText(text: String) {
        custom_popup_LongText!!.text = text
    }
}
