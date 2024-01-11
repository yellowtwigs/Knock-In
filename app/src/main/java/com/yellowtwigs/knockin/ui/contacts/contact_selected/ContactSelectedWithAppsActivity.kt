package com.yellowtwigs.knockin.ui.contacts.contact_selected

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.ActivityContactSelectedWithAppsBinding
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.EditContactActivity
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.EditContactViewModel
import com.yellowtwigs.knockin.ui.add_edit_contact.edit.EditContactViewState
import com.yellowtwigs.knockin.ui.contacts.list.*
import com.yellowtwigs.knockin.utils.ContactGesture.callPhone
import com.yellowtwigs.knockin.utils.ContactGesture.goToSignal
import com.yellowtwigs.knockin.utils.ContactGesture.goToTelegram
import com.yellowtwigs.knockin.utils.ContactGesture.isWhatsappInstalled
import com.yellowtwigs.knockin.utils.ContactGesture.openSms
import com.yellowtwigs.knockin.utils.ContactGesture.openWhatsapp
import com.yellowtwigs.knockin.utils.Converter
import com.yellowtwigs.knockin.utils.Converter.converter06To33
import com.yellowtwigs.knockin.utils.EveryActivityUtils.getAppOnPhone
import com.yellowtwigs.knockin.utils.InitContactsForListAdapter.InitContactAdapter.contactPriorityBorder
import com.yellowtwigs.knockin.utils.RandomDefaultImage.randomDefaultImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactSelectedWithAppsActivity : AppCompatActivity() {

    private val editContactViewModel: EditContactViewModel by viewModels()
    private val contactsListViewModel: ContactsListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityContactSelectedWithAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView(binding)

        val appsSupportPref = getSharedPreferences("Apps_Support_Bought", Context.MODE_PRIVATE)

        editContactViewModel.editContactViewStateLiveData.observe(this) { contact ->
            contact?.let {
                binding.apply {
                    if (it.profilePicture64 == "") {
                        contactImage.setImageResource(randomDefaultImage(contact.profilePicture, this@ContactSelectedWithAppsActivity, "Get"))
                    } else {
                        contactImage.setImageBitmap(Converter.base64ToBitmap(contact.profilePicture64))
                    }

                    contactPriorityBorder(
                        it.priority, contactImage, this@ContactSelectedWithAppsActivity
                    )

                    name.text = "${it.firstName} ${it.lastName}"
                }
                initCircularMenu(binding, contact, appsSupportPref)
            }
        }

        binding.backIcon.setOnClickListener {
            onBackPressed()
            finish()
        }
    }

    private fun initCircularMenu(
        binding: ActivityContactSelectedWithAppsBinding, contact: EditContactViewState, appsSupportPref: SharedPreferences
    ) {
        val listApp = getAppOnPhone(this)

        binding.apply {
            contact.apply {
                smsIcon.visibility = View.GONE
                callIcon1.visibility = View.GONE
                whatsappIcon.visibility = View.GONE

                mailIcon.visibility = View.GONE
                messengerIcon.visibility = View.GONE
                signalIcon.visibility = View.GONE
                editIcon.visibility = View.GONE
                telegramIcon.visibility = View.GONE

                CoroutineScope(Dispatchers.Main).launch {
                    delay(500)
                    checkVisibleMessagingApp(binding.editIcon, true)

                    checkVisibleMessagingApp(binding.smsIcon, contact.firstPhoneNumber.phoneNumber.isNotEmpty())
                    checkVisibleMessagingApp(binding.callIcon1, contact.firstPhoneNumber.phoneNumber.isNotEmpty())

                    checkVisibleMessagingApp(
                        binding.whatsappIcon,
                        contact.firstPhoneNumber.phoneNumber.isNotEmpty() && isWhatsappInstalled(this@ContactSelectedWithAppsActivity) && listOfMessagingApps.contains(
                            "com.whatsapp"
                        )
                    )

                    checkVisibleMessagingApp(binding.mailIcon, listOfMails[0] != "")
                    checkVisibleMessagingApp(binding.messengerIcon, messengerId != "" && listApp.contains("com.facebook.orca"))
                    checkVisibleMessagingApp(
                        binding.signalIcon, listApp.contains("org.thoughtcrime.securesms") && listOfMessagingApps.contains(
                            "org.thoughtcrime.securesms"
                        )
                    )
                    checkVisibleMessagingApp(
                        binding.telegramIcon,
                        listApp.contains("org.telegram.messenger") && listOfMessagingApps.contains("org.telegram.messenger")
                    )
                }

                if (!appsSupportPref.getBoolean("Apps_Support_Bought", false)) {
                    signalIcon.setImageResource(R.drawable.ic_signal_disable)
                    telegramIcon.setImageResource(R.drawable.ic_telegram_disable)
                    messengerIcon.setImageResource(R.drawable.ic_messenger_disable)
                }

                val buttonListener = View.OnClickListener { v: View ->
                    when (v.id) {
                        whatsappIcon.id -> {
                            openWhatsapp(converter06To33(contact.firstPhoneNumber.phoneNumber), this@ContactSelectedWithAppsActivity)
                        }

                        editIcon.id -> {
                            val intent = Intent(
                                this@ContactSelectedWithAppsActivity, EditContactActivity::class.java
                            )
                            intent.putExtra("ContactId", id)
                            startActivity(intent)
                        }

                        callIcon1.id -> {
                            callPhone(contact.firstPhoneNumber.phoneNumber, this@ContactSelectedWithAppsActivity)
                        }

                        smsIcon.id -> {
                            openSms(contact.firstPhoneNumber.phoneNumber, this@ContactSelectedWithAppsActivity)
                        }

                        mailIcon.id -> {
                            val mail = listOfMails.random()
                            val intent = Intent(Intent.ACTION_SENDTO)
                            intent.data = Uri.parse("mailto:")
                            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(mail))
                            intent.putExtra(Intent.EXTRA_SUBJECT, "")
                            intent.putExtra(Intent.EXTRA_TEXT, "")
                            startActivity(intent)
                        }

                        messengerIcon.id -> {
                            if (!appsSupportPref.getBoolean("Apps_Support_Bought", false)) {
                            } else {
                                val intent = Intent(
                                    Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/$messengerId")
                                )
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                            }
                        }

                        signalIcon.id -> {
                            if (!appsSupportPref.getBoolean("Apps_Support_Bought", false)) {
                            } else {
                                goToSignal(this@ContactSelectedWithAppsActivity)
                            }
                        }

                        telegramIcon.id -> {
                            goToTelegram(this@ContactSelectedWithAppsActivity, contact.firstPhoneNumber.phoneNumber)
                        }
                    }
                }

                messengerIcon.setOnClickListener(buttonListener)
                whatsappIcon.setOnClickListener(buttonListener)
                callIcon1.setOnClickListener(buttonListener)
                smsIcon.setOnClickListener(buttonListener)
                editIcon.setOnClickListener(buttonListener)
                mailIcon.setOnClickListener(buttonListener)
                signalIcon.setOnClickListener(buttonListener)
                telegramIcon.setOnClickListener(buttonListener)
            }
        }
    }

    private fun checkVisibleMessagingApp(image: AppCompatImageView, condition: Boolean) {
        val animation = AnimationUtils.loadAnimation(this, R.anim.reapparrition)
        if (condition) {
            image.startAnimation(animation)
            image.isVisible = true
        }
    }

    private fun setupRecyclerView(binding: ActivityContactSelectedWithAppsBinding) {
        val sharedPreferences = getSharedPreferences("Gridview_column", Context.MODE_PRIVATE)
        val nbGrid = sharedPreferences.getInt("gridview", 1)

        val contactsListAdapter = ContactsListAdapter(this, { id, civ, contact -> })

        if (nbGrid == 1) {
            binding.recyclerView.apply {
                contactsListViewModel.contactsListViewStateLiveData.observe(this@ContactSelectedWithAppsActivity) { contacts ->
                    contactsListAdapter.submitList(null)
                    contactsListAdapter.submitList(contacts)
                }
                adapter = contactsListAdapter
                layoutManager = LinearLayoutManager(context)
            }
        } else {
            if (nbGrid == 4) {
                binding.recyclerView.apply {
                    val contactsGridAdapter = ContactsGridFourAdapter(this@ContactSelectedWithAppsActivity) { id, civ, contact ->
                    }
                    contactsListViewModel.contactsListViewStateLiveData.observe(this@ContactSelectedWithAppsActivity) { contacts ->
                        contactsGridAdapter.submitList(null)
                        contactsGridAdapter.submitList(contacts)
                    }
                    adapter = contactsGridAdapter
                    layoutManager = GridLayoutManager(context, nbGrid)
                }
            } else if (nbGrid == 5) {
                binding.recyclerView.apply {
                    val contactsGridAdapter = ContactsGridFiveAdapter(this@ContactSelectedWithAppsActivity) { id, civ, contact ->
                    }
                    contactsListViewModel.contactsListViewStateLiveData.observe(this@ContactSelectedWithAppsActivity) { contacts ->
                        contactsGridAdapter.submitList(null)
                        contactsGridAdapter.submitList(contacts)
                    }
                    adapter = contactsGridAdapter
                    layoutManager = GridLayoutManager(context, nbGrid)
                }
            }
        }
    }


}