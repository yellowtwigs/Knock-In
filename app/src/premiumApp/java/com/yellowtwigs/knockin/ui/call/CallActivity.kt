package com.yellowtwigs.knockin.ui.call

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.CallLog
import android.telephony.TelephonyManager
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.yellowtwigs.knockin.databinding.ActivityCallBinding
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.app.ActivityCompat

@AndroidEntryPoint
class CallActivity : AppCompatActivity() {

    private val callLogViewModel: CallViewModel by viewModels()
    private lateinit var binding: ActivityCallBinding

    var listOfCalls = listOf<String>(
        CallLog.Calls._ID,
        CallLog.Calls.NUMBER,
        CallLog.Calls.TYPE,
        CallLog.Calls.DURATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val permission = Manifest.permission.READ_CALL_LOG
        val requestCode = 1  // Use any unique request code

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        } else {
            callLogViewModel.callLogsLiveData.observe(this) { logs ->
                Log.i("PhoneCall", "logs : ${logs}")

                if (logs.isNotEmpty()) {
                    binding.log.text = logs.first().phoneNumber
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_CALL_LOG_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, query call log data
//                    callLogViewModel.queryCallLog(this)
                } else {
                    // Permission denied, handle it accordingly
                }
            }
        }
    }

    companion object {
        private const val READ_CALL_LOG_PERMISSION_REQUEST = 1001
    }

    private fun displayLog() {
        var from = listOf(CallLog.Calls.NUMBER, CallLog.Calls.DURATION, CallLog.Calls.TYPE).toTypedArray()

        var rs =
            contentResolver.query(CallLog.Calls.CONTENT_URI, listOfCalls.toTypedArray(), null, null, "${CallLog.Calls.LAST_MODIFIED} DESC")
    }
}