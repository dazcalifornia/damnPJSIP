package com.synapes.selen_alarm_box

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class RestartBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(" +++ RestartBroadcastReceiver +++", "Restarting app")
//        context.startService(Intent(context, SelenForegroundService::class.java))
        // FiXME: --> start activity --> activity starts service --> if service destroyed, it sends broadcast to start activity
        context.startActivity(Intent(context, MainActivity::class.java))
    }
}