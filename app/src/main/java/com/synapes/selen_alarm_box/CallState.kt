package com.synapes.selen_alarm_box

sealed class CallState {
    object Idle : CallState()
    object Dialing : CallState()
    object Ringing : CallState()
    object Connecting : CallState()
    object Active : CallState()
    object Disconnecting : CallState()
    object Disconnected : CallState()
    data class Error(val message: String) : CallState()
}