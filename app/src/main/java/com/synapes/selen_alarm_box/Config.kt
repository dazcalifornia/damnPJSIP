package com.synapes.selen_alarm_box

object Config {
    //  ****************************************************
    const val SERVER_ADDRESS = "synapes-pbx-poc-01.online" // --> VPN
    const val SELF_EXT = "8888"
    const val DESTINATION_EXT = "9595"
//    const val DESTINATION_EXT = "9999"
    const val ACC_ID_URI = "sip:$SELF_EXT@$SERVER_ADDRESS"
    const val CALL_DST_URI = "sip:$DESTINATION_EXT@$SERVER_ADDRESS"
    const val REGISTRA_URI = "sip:$SERVER_ADDRESS"
    const val USERNAME = SELF_EXT
    const val PASSWORD = SELF_EXT
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