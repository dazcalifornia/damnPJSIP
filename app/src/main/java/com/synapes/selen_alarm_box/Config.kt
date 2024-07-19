package com.synapes.selen_alarm_box

import android.content.Context

object Config {
    lateinit var appContext: Context

    const val DROID_SERVER_CHECK_URL = "https://droid-update.selen.click/check"
    const val DROID_SERVER_VERSION_UPDATE_URL = "https://droid-update.selen.click/update"
    const val DEBUG_MODE = false

    //  ****************************************************
    const val SERVER_ADDRESS = "synapes-pbx-poc-01.online" // --> VPN

//    const val SELF_EXT = "5002" // --> Device (Korat Box no with aum)
//    const val SELF_EXT = "5004" // --> Device (New Pui)
//    const val SELF_EXT = "9595" // --> Coding
//    const val SELF_EXT = "5998" // --> Coding - whitebox
//    const val SELF_EXT = "5999" // --> Coding - simulator

    /*
    const val SELF_EXT = "5555" // --> Coding - tablet
    const val DESTINATION_EXT = "1000" // --> HQ Firefox Browser
     */

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

    // ... (rest of your constants)

    // Make these lateinit so we can initialize them after getting SELF_EXT and DESTINATION_EXT
    lateinit var ACC_ID_URI: String
    lateinit var CALL_DST_URI: String
    lateinit var USERNAME: String
    lateinit var PASSWORD: String



    //    const val DESTINATION_EXT = "9999"

    /*
    const val ACC_ID_URI = "sip:$SELF_EXT@$SERVER_ADDRESS"
    const val CALL_DST_URI = "sip:$DESTINATION_EXT@$SERVER_ADDRESS"
    const val REGISTRA_URI = "sip:$SERVER_ADDRESS"
    const val USERNAME = SELF_EXT
    const val PASSWORD = SELF_EXT
     */
    const val REGISTRA_URI = "sip:$SERVER_ADDRESS"

//  ****************************************************


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