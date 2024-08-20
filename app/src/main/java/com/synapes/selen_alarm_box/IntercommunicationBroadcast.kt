package com.synapes.selen_alarm_box

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class IntercommunicationBroadcast: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val message = intent.getStringExtra("message")
        Log.d("IntercommunicationBroadcast", "Action: $action, Message: $message")

    }
}