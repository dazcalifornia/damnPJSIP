package com.synapes.selen_alarm_box

import android.app.Application
import android.content.Context
import org.acra.config.httpSender
import org.acra.sender.HttpSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra

class MyApplication : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        initAcra {
            reportFormat = StringFormat.JSON
            alsoReportToAndroidFramework = true
            httpSender {
                uri = "https://droid.selen.click/report"
                basicAuthLogin = "qT7zUFZkI3kcqUxJ"
                basicAuthPassword = "UVClC9LhAF94HlYQ"
                httpMethod = HttpSender.Method.POST
            }
        }
    }
}