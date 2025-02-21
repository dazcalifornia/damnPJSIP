package com.synapes.selen_alarm_box

// CallEvent.kt
sealed class CallEvent {
    data class MakeCall(val number: String) : CallEvent()
    object AnswerCall : CallEvent()
    object DeclineCall : CallEvent()
    object EndCall : CallEvent()
    object CallConnected : CallEvent()
    object CallDisconnected : CallEvent()
    data class IncomingCall(val number: String) : CallEvent()
    data class CallError(val error: String) : CallEvent()
}