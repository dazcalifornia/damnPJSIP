package com.synapes.selen_alarm_box

object CallMessageType {
    const val INCOMING_CALL = 1
    const val CALL_STATE = 2
    const val REG_STATE = 3
    const val BUDDY_STATE = 4
    const val CALL_MEDIA_STATE = 5
    const val CHANGE_NETWORK = 6
}

object CallButtonType {
    const val RE_REGISTER = "Re-Register"
    const val CALL_HQ = "CALL HQ"
    const val CALLING = "Calling..."
    const val HANGUP = "Hangup"
}

object SoundType {
    const val NO_VPN = "no_vpn"
    const val AIRPLANE_MODE = "airplane_mode"
    const val NO_INTERNET = "no_internet"
    const val RETRY_REGISTER = "retry_register"
}

object RunMode {
    const val UI = "UI"
    const val FOREGROUND = "FOREGROUND"
    const val BACKGROUND = "BACKGROUND"
}

object BroadcastEventMessage {
    const val VOIP_APP_IS_STARTING = "VOIP App is starting up"
    const val VOIP_APP_IS_MAKING_CALL = "VOIP App is making a call"
    const val VOIP_APP_FAILED_TO_MAKE_CALL = "VOIP App failed to make a call"
    const val VOIP_APP_HUNG_UP = "VOIP App hung up"
    const val VOIP_APP_FAILED_TO_HANG_UP = "VOIP App failed to hang up"
    const val VOIP_APP_REGISTRATION_SUCCESSFUL = "VOIP App registration successful"
    const val VOIP_APP_REGISTRATION_FAILED = "VOIP App registration failed"
    const val VOIP_APP_CALL_ANSWERED = "VOIP App call answered"
    const val VOIP_APP_CALL_DISCONNECTED = "VOIP App call disconnected"
    const val VOIP_APP_FAILED_TO_ANSWER_CALL = "VOIP App failed to answer call"
}