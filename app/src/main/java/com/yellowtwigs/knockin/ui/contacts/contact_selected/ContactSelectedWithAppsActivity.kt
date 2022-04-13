package com.yellowtwigs.knockin.ui.contacts.contact_selected

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityContactSelectedWithAppsBinding
import com.yellowtwigs.knockin.model.ContactManager
import com.yellowtwigs.knockin.ui.edit_contact.EditContactDetailsActivity
import com.yellowtwigs.knockin.utils.ContactGesture
import com.yellowtwigs.knockin.utils.ContactGesture.isWhatsappInstalled
import com.yellowtwigs.knockin.utils.Converter
import com.yellowtwigs.knockin.utils.EveryActivityUtils
import com.yellowtwigs.knockin.utils.RandomDefaultImage

class ContactSelectedWithAppsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityContactSelectedWithAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getIntExtra("id", 0)
        val currentContact = ContactManager(this).getContactById(id)

        val listOfAppsInstalledInPhone = EveryActivityUtils.getAppOnPhone(this)
        val listOfApps = arrayListOf<Int>()
        listOfApps.apply {
            add(R.drawable.ic_circular_edit)

            if (currentContact?.getFirstPhoneNumber()?.isNotEmpty() == true) {
                add(R.drawable.ic_sms_selector)
            }

            if (currentContact?.getMessengerID()?.isNotEmpty() == true) {
                add(R.drawable.ic_circular_messenger)
            }
            if (currentContact?.getFirstMail()?.isNotEmpty() == true) {
                add(R.drawable.ic_circular_mail)
            }
            if (currentContact?.getFirstPhoneNumber()?.isNotEmpty() == true) {
                add(R.drawable.ic_google_call)
            }
            if (isWhatsappInstalled(this@ContactSelectedWithAppsActivity)) {
                add(R.drawable.ic_circular_whatsapp)
            }
            if (listOfAppsInstalledInPhone.contains("org.telegram.messenger")) {
                add(R.drawable.ic_telegram)
            }
            if (listOfAppsInstalledInPhone.contains("org.thoughtcrime.securesms")) {
                add(R.drawable.ic_circular_signal)
            }
        }

        binding.apply {
            val appsAdapter = AppsListAdapter { id ->
                Log.i("ContactId", "1 : $id")
                when (id) {
                    R.drawable.ic_circular_whatsapp -> {
                        ContactGesture.openWhatsapp(
                            Converter.converter06To33(
                                currentContact?.getFirstPhoneNumber().toString()
                            ),
                            this@ContactSelectedWithAppsActivity
                        )
                    }
                    R.drawable.ic_circular_edit -> {
                        val intent = Intent(
                            this@ContactSelectedWithAppsActivity,
                            EditContactDetailsActivity::class.java
                        )
                        Log.i("ContactId", "2 : $id")
                        intent.putExtra("ContactId", id)
                        startActivity(intent)
                        finish()
                    }
                    R.drawable.ic_google_call -> {
                        currentContact?.getFirstPhoneNumber()
                            ?.let {
                                ContactGesture.callPhone(
                                    it,
                                    this@ContactSelectedWithAppsActivity
                                )
                            }
                    }
                    R.drawable.ic_sms_selector -> {
                        val phone = currentContact?.getFirstPhoneNumber()
                        val i = Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", phone, null))
                        startActivity(i)
                        finish()
                    }
                    R.drawable.ic_circular_mail -> {
                        val mail = currentContact?.getFirstMail()
                        val intent = Intent(Intent.ACTION_SENDTO)
                        intent.data = Uri.parse("mailto:")
                        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(mail))
                        intent.putExtra(Intent.EXTRA_SUBJECT, "")
                        intent.putExtra(Intent.EXTRA_TEXT, "")
                        startActivity(intent)
                        finish()
                    }
                    R.drawable.ic_circular_messenger -> {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.messenger.com/t/" + currentContact?.getMessengerID())
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                    R.drawable.ic_circular_signal -> {
                        ContactGesture.goToSignal(this@ContactSelectedWithAppsActivity)
                    }
                    R.drawable.ic_telegram -> {
                        goToTelegram()
                    }
                }
            }
            listApps.apply {
                adapter = appsAdapter
                appsAdapter.submitList(listOfApps)
                setHasFixedSize(true)
                layoutManager =
                    LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
            }

            firstName.text = currentContact?.contactDB?.firstName
            lastName.text = currentContact?.contactDB?.lastName

            if (currentContact?.contactDB?.profilePicture64 != "") {
                val bitmap =
                    currentContact?.contactDB?.profilePicture64?.let { Converter.base64ToBitmap(it) }
                image.setImageBitmap(bitmap)
            } else {
                image.setImageResource(
                    RandomDefaultImage.randomDefaultImage(
                        currentContact.contactDB?.profilePicture!!,
                        this@ContactSelectedWithAppsActivity
                    )
                )
            }
        }
    }

    private fun goToTelegram() {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve"))
        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://web.telegram.org/")
                )
            )
        }
    }
}