package com.yellowtwigs.knockin.ui.call

import android.content.Context
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import com.yellowtwigs.knockin.call.CallLogRepository
import com.yellowtwigs.knockin.domain.notifications.NotificationsListenerUseCases
import com.yellowtwigs.knockin.utils.Converter

class CustomPhoneStateListener(private val context: Context) : PhoneStateListener() {

    private val callLogRepository = CallLogRepository()

    private lateinit var notificationsListenerUseCases: NotificationsListenerUseCases

    fun injectDependencies(notificationsListenerUseCases: NotificationsListenerUseCases) {
        this.notificationsListenerUseCases = notificationsListenerUseCases
    }

    override fun onCallStateChanged(state: Int, phoneNumber: String?) {
        super.onCallStateChanged(state, phoneNumber)

        Log.i("PhoneCall", "phoneNumber : ${phoneNumber}")

        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                try {
                    phoneNumber?.let {
                        val contact = notificationsListenerUseCases.getContactByPhoneNumber(Converter.converter33To06(it))

                        val callLogEntry = CallLogEntry("${contact?.firstName} ${contact?.lastName}", phoneNumber)
                        callLogRepository.addCallLogEntry(callLogEntry)

//                        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//                        val telephonyServiceField = telephonyManager.javaClass.getDeclaredField("mITelephony")
//                        telephonyServiceField.isAccessible = true
//                        val telephonyService = telephonyServiceField.get(telephonyManager)
//
//                        val endCallMethod = telephonyService.javaClass.getMethod("endCall")
//                        endCallMethod.invoke(telephonyService)
                    }
                } catch (e: Exception) {
                    Log.i("PhoneCall", "e : $e")
                }
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
            }
            TelephonyManager.CALL_STATE_IDLE -> {
            }
        }
    }
}