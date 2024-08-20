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

object BroadcastAction {
    const val APP_RESTART = "com.synapes.selen_alarm_box.APP_RESTART"
    const val SIP_INIT = "com.synapes.selen_alarm_box.SIP_INIT"
    const val SIP_REGISTER = "com.synapes.selen_alarm_box.SIP_REGISTER"
    const val SIP_UNREGISTER = "com.synapes.selen_alarm_box.SIP_UNREGISTER"
    const val SIP_CALL = "com.synapes.selen_alarm_box.SIP_CALL"
    const val SIP_HANGUP = "com.synapes.selen_alarm_box.SIP_HANGUP"
    const val SIP_INCOMING_CALL = "com.synapes.selen_alarm_box.SIP_INCOMING_CALL"
    const val SIP_INCOMING_CALL_CANCEL = "com.synapes.selen_alarm_box.SIP_INCOMING_CALL_CANCEL"
    const val SIP_CALL_CONNECTED = "com.synapes.selen_alarm_box.SIP_CALL_CONNECTED"
    const val SIP_CALL_DISCONNECTED = "com.synapes.selen_alarm_box.SIP_CALL_DISCONNECTED"
    const val SIP_CALL_ERROR = "com.synapes.selen_alarm_box.SIP_CALL_ERROR"
    const val SIP_CALL_INFO = "com.synapes.selen_alarm_box.SIP_CALL_INFO"
    const val SIP_CALL_INFO_UPDATE = "com.synapes.selen_alarm_box.SIP_CALL_INFO_UPDATE"
    const val SIP_CALL_INFO_CLEAR = "com.synapes.selen_alarm_box.SIP_CALL_INFO_CLEAR"
    const val SIP_CALL_INFO_CLEAR_ALL = "com.synapes.selen_alarm_box.SIP_CALL_INFO_CLEAR_ALL"
    const val SIP_CALL_INFO_CLEAR_LAST = "com.synapes.selen_alarm_box.SIP_CALL_INFO_CLEAR_LAST"
    const val SIP_CALL_INFO_CLEAR_FIRST = "com.synapes.selen_alarm_box.SIP_CALL_INFO_CLEAR_FIRST"
    const val SIP_CALL_INFO_CLEAR_BY_INDEX =
        "com.synapes.selen_alarm_box.SIP_CALL_INFO_CLEAR_BY_INDEX"
    const val SIP_CALL_INFO_CLEAR_BY_CALL_ID =
        "com.synapes.selen_alarm_box.SIP_CALL_INFO_CLEAR_BY_CALL_ID"
    const val SIP_CALL_INFO_CLEAR_BY_EXT = "com.synapes.selen_alarm_box.SIP_CALL_INFO_CLEAR_BY_EXT"
    const val SIP_CALL_INFO_CLEAR_BY_EXT_AND_CALL_ID =
        "com.synapes.selen_alarm_box.SIP_CALL_INFO_CLEAR_BY_EXT_AND_CALL_ID"
    const val SIP_CALL_INFO_CLEAR_BY_EXT_AND_INDEX =
        "com.synapes.selen_alarm_box.SIP_CALL_INFO_CLEAR_BY_EXT_AND_INDEX"
    const val SIP_CALL_INFO_CLEAR_BY_CALL_ID_AND_INDEX =
        "com.synapes.selen_alarm_box.SIP_CALL_INFO_CLEAR_BY_CALL_ID"
}