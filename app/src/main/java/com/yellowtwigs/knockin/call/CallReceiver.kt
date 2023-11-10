package com.yellowtwigs.knockin.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.yellowtwigs.knockin.domain.notifications.NotificationsListenerUseCases
import com.yellowtwigs.knockin.ui.call.CallLogEntry
import com.yellowtwigs.knockin.utils.Converter.converter33To06
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

//@AndroidEntryPoint
//class CallReceiver : BroadcastReceiver() {
//
//    private val callLogRepository = CallLogRepository()
//
//    @Inject
//    lateinit var notificationsListenerUseCases: NotificationsListenerUseCases
//
//    override fun onReceive(context: Context?, intent: Intent?) {
//        if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
//            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
//            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
//            val voiceMailNumber = intent.getStringExtra(TelephonyManager.EXTRA_VOICEMAIL_NUMBER)
//
//            Log.i("PhoneCall", "incomingNumber : ${incomingNumber}")
//            Log.i("PhoneCall", "voiceMailNumber : ${voiceMailNumber}")
//
//            incomingNumber?.let { phoneNumber ->
//                try {
//                    val contact = notificationsListenerUseCases.getContactByPhoneNumber(converter33To06(phoneNumber))
//
//                    when (state) {
//                        TelephonyManager.EXTRA_STATE_RINGING -> {
//                            val callLogEntry = CallLogEntry("${contact?.firstName} ${contact?.lastName}", phoneNumber)
//                            callLogRepository.addCallLogEntry(callLogEntry)
//
//                            val telephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//                            val telephonyServiceField = telephonyManager.javaClass.getDeclaredField("mITelephony")
//                            telephonyServiceField.isAccessible = true
//                            val telephonyService = telephonyServiceField.get(telephonyManager)
//
//                            val endCallMethod = telephonyService.javaClass.getMethod("endCall")
//                            endCallMethod.invoke(telephonyService)
//                        }
//
//                        TelephonyManager.EXTRA_STATE_OFFHOOK -> {
//                        }
//
//                        TelephonyManager.EXTRA_STATE_IDLE -> {
//                        }
//
//                        else -> {}
//                    }
//                } catch (e: Exception) {
//                    Log.i("PhoneCall", "e : $e")
//                }
//            }
//        }
//    }
//}