package com.synapes.selen_alarm_box

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class IntercommunicationBroadcast: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val message = intent.getStringExtra("message")
        Log.d("IntercommunicationBroadcast", "Action: $action, Message: $message")

        if (intent.action == "com.synapes.selen_alarm_box.SELEN_ALARM_BOX_CALL_HQ") {
            val deviceId = intent.getStringExtra("device_id")
            val destinationNumber = intent.getStringExtra("destination_number")
            Log.d("IntercommunicationBroadcast", "Received broadcast from SelenAlarmBox: $message")
            Log.d("IntercommunicationBroadcast", "device_id: $deviceId, destination_number: $destinationNumber")

            // Call the MainActivity's handleButtonState method
//            val mainActivity = context as MainActivity
//            mainActivity.handleButtonState(0, destinationNumber)
//            mainActivity.handleButtonState(0)
            // Send a local broadcast to MainActivity
            val localIntent = Intent("com.synapes.selen_alarm_box.LOCAL_CALL_HQ")
            localIntent.putExtra("destination_number", destinationNumber)
            LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)
        }
    }
}