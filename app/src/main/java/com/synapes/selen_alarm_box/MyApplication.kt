package com.synapes.selen_alarm_box

import android.app.Application
import android.content.Intent

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Config.initialize(this)
        PreferencesManager.dumpAllPreferences(this)

        // Send broadcast when the app is starting up
        val intent = Intent(BroadcastAction.SELEN_VOIP_APP_START)
        intent.putExtra("message", BroadcastEventMessage.VOIP_APP_IS_STARTING)
        sendBroadcast(intent)
    }
}