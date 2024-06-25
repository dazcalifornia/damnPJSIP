package com.synapes.selen_alarm_box

import android.content.Context
import android.util.Log
import com.google.auto.service.AutoService
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.acra.sender.ReportSender
import org.acra.sender.ReportSenderFactory

class MySender : ReportSender {
    override fun send(context: Context, errorContent: CrashReportData) {
        Log.d("[ACRA-VOIP]", "Report Sent!")
    }
}

@AutoService(ReportSenderFactory::class)
class MySenderFactory : ReportSenderFactory {
    override fun create(context: Context, config: CoreConfiguration): ReportSender {
        return MySender()
    }
}