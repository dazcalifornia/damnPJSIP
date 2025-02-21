package com.synapes.selen_alarm_box

import android.content.Context
import android.widget.Toast
import org.pjsip.pjsua2.*

class CallStateManager(
    private val context: Context,
    private val viewManager: ViewManager
) {
    private var currentState: CallState = CallState.Idle
    private var currentCall: MyCall? = null

    fun handleEvent(event: CallEvent) {
        val newState = when (val state = currentState) {
            is CallState.Idle -> handleIdleState(event)
            is CallState.Dialing -> handleDialingState(event)
            is CallState.Ringing -> handleRingingState(event)
            is CallState.Connecting -> handleConnectingState(event)
            is CallState.Active -> handleActiveState(event)
            is CallState.Disconnecting -> handleDisconnectingState(event)
            is CallState.Disconnected -> handleDisconnectedState(event)
            is CallState.Error -> handleErrorState(event)
        }

        if (newState != currentState) {
            currentState = newState
            updateUI(newState)
        }
    }

    private fun handleIdleState(event: CallEvent): CallState {
        return when (event) {
            is CallEvent.MakeCall -> {
                try {
                    val call = MyCall(MainActivity.account, -1)
                    val prm = CallOpParam(true)
                    call.makeCall("sip:${event.number}@${Config.SERVER_ADDRESS}", prm)
                    currentCall = call
                    CallState.Dialing
                } catch (e: Exception) {
                    CallState.Error("Failed to make call: ${e.message}")
                }
            }
            is CallEvent.IncomingCall -> {
                viewManager.showIncomingCallScreen(
                    remoteUri = event.number,
                    onAnswer = { handleEvent(CallEvent.AnswerCall) },
                    onDecline = { handleEvent(CallEvent.DeclineCall) }
                )
                CallState.Ringing
            }
            else -> CallState.Idle
        }
    }

    private fun handleDialingState(event: CallEvent): CallState {
        return when (event) {
            is CallEvent.CallConnected -> CallState.Active
            is CallEvent.CallDisconnected -> CallState.Disconnected
            is CallEvent.EndCall -> {
                try {
                    val prm = CallOpParam()
                    prm.statusCode = pjsip_status_code.PJSIP_SC_DECLINE
                    currentCall?.hangup(prm)
                    CallState.Disconnecting
                } catch (e: Exception) {
                    CallState.Error("Failed to end call: ${e.message}")
                }
            }
            is CallEvent.CallError -> CallState.Error(event.error)
            else -> currentState
        }
    }

    private fun handleRingingState(event: CallEvent): CallState {
        return when (event) {
            is CallEvent.AnswerCall -> {
                try {
                    val prm = CallOpParam()
                    prm.statusCode = pjsip_status_code.PJSIP_SC_OK
                    currentCall?.answer(prm)
                    CallState.Connecting
                } catch (e: Exception) {
                    CallState.Error("Failed to answer call: ${e.message}")
                }
            }
            is CallEvent.DeclineCall -> {
                try {
                    val prm = CallOpParam()
                    prm.statusCode = pjsip_status_code.PJSIP_SC_DECLINE
                    currentCall?.hangup(prm)
                    CallState.Disconnecting
                } catch (e: Exception) {
                    CallState.Error("Failed to decline call: ${e.message}")
                }
            }
            else -> currentState
        }
    }

    private fun handleConnectingState(event: CallEvent): CallState {
        return when (event) {
            is CallEvent.CallConnected -> CallState.Active
            is CallEvent.CallDisconnected -> CallState.Disconnected
            is CallEvent.CallError -> CallState.Error(event.error)
            else -> currentState
        }
    }

    private fun handleActiveState(event: CallEvent): CallState {
        return when (event) {
            is CallEvent.EndCall -> {
                try {
                    val prm = CallOpParam()
                    prm.statusCode = pjsip_status_code.PJSIP_SC_DECLINE
                    currentCall?.hangup(prm)
                    currentCall = null  // Clear call reference here too
                    CallState.Disconnecting
                } catch (e: Exception) {
                    CallState.Error("Failed to end call: ${e.message}")
                }
            }
            is CallEvent.CallDisconnected -> {
                currentCall = null
                CallState.Idle  // Go directly to Idle instead of Disconnected
            }
            else -> currentState
        }
    }

    private fun handleDisconnectingState(event: CallEvent): CallState {
        return when (event) {
            is CallEvent.CallDisconnected -> {
                currentCall = null  // Clear the current call reference
                CallState.Idle      // Return to Idle state instead of Disconnected
            }
            else -> currentState
        }
    }

    private fun handleDisconnectedState(event: CallEvent): CallState {
        // Reset call reference and return to Idle state
        currentCall = null
        return CallState.Idle
    }

    private fun handleErrorState(event: CallEvent): CallState {
        return when (event) {
            is CallEvent.MakeCall -> handleIdleState(event)
            else -> currentState
        }
    }

    private fun updateUI(state: CallState) {
        when (state) {
            is CallState.Idle -> {
                viewManager.dismissCallScreens()
                // Reset UI elements to enable new calls
                MainActivity.currentCall = null
            }
            is CallState.Dialing -> {
                viewManager.showCallingScreen(
                    number = currentCall?.info?.remoteUri ?: "Unknown",
                    onEndCall = { handleEvent(CallEvent.EndCall) }
                )
                viewManager.updateCallStatus("Dialing...")
            }
            is CallState.Active -> {
                viewManager.updateCallStatus("Connected")
            }
            is CallState.Disconnected -> {
                viewManager.dismissCallScreens()
            }
            is CallState.Error -> {
                viewManager.dismissCallScreens()
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }
    // Add a function to get current state
    fun getCurrentState(): CallState = currentState

    // Add a function to check if we can make a call
    fun canMakeCall(): Boolean {
        return currentState == CallState.Idle
    }
}