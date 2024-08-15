package com.synapes.selen_alarm_box

import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Config.initialize(this)
        PreferencesManager.dumpAllPreferences(this)
    }
}