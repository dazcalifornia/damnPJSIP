package com.synapes.selen_alarm_box

import android.content.Context

object Config {
    lateinit var appContext: Context

    const val DEBUG_MODE = false

    const val SERVER_ADDRESS = "synapes-pbx-poc-01.online"

    lateinit var SELF_EXT: String
    lateinit var DESTINATION_EXT: String

    fun initialize(context: Context) {

        appContext = context.applicationContext
        SELF_EXT = PreferencesManager.getSelfExtension(context)
        DESTINATION_EXT = PreferencesManager.getDestinationExtension(context)

        // Now update the dependent constants
        ACC_ID_URI = "sip:$SELF_EXT@$SERVER_ADDRESS"
        CALL_DST_URI = "sip:$DESTINATION_EXT@$SERVER_ADDRESS"
        USERNAME = SELF_EXT
        PASSWORD = SELF_EXT
    }

    lateinit var ACC_ID_URI: String
    lateinit var CALL_DST_URI: String
    lateinit var USERNAME: String
    lateinit var PASSWORD: String

    const val REGISTRA_URI = "sip:$SERVER_ADDRESS"

    //  ****************************************************
    const val APP_RUN_MODE = RunMode.UI
//    const val APP_RUN_MODE = RunMode.BACKGROUND // NOT ON ANDROID 7
//    const val APP_RUN_MODE = RunMode.FOREGROUND // NOT ON ANDROID 7

    const val MSG_UPDATE_CALL_INFO = 1

    //     SIP transport listening port
    const val SIP_LISTENING_PORT = 6000

}

object RunMode {
    const val UI = "UI"
    const val FOREGROUND = "FOREGROUND"
    const val BACKGROUND = "BACKGROUND"
}