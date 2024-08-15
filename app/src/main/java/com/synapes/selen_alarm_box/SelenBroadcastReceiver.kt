package com.synapes.selen_alarm_box;

import android.app.ActivityManager
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build
import android.util.Log

class SelenBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
//        Log.d(" +++ BOOT RECEIVED +++ ", " --- onReceive: ACTION_BOOT_COMPLETED")
        // FIXME         activityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK // Add this line --> CHECK IF RESTARTED AFTER HIT RESTART BUTTON CORRECTLY
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (MainActivity.isActivityRunning) {
//                Log.d(" +++ BOOT RECEIVED +++ ", " --- onReceive: Activity is running")
                return
            } else {
//                Log.d(" +++ BOOT RECEIVED +++ ", " --- onReceive: Start Activity")
                context.startActivity(Intent(context, MainActivity::class.java))
            }
        }
    }
}
