package com.synapes.selen_alarm_box

import android.content.Context
import com.synapes.selen_alarm_box.databinding.ActivityMainBinding

class UIManager(
    private val context: Context,
    private val binding: ActivityMainBinding,
    private val viewManager: ViewManager
) {
    private val tag = "UIManager"

    fun setupUIComponents(
        onMakeCall: (String) -> Unit,
        onEndCall: () -> Unit,
        onRegister: (String, String) -> Unit
    ) {
        setupDebugButton()
        setupCallButton(onMakeCall, onEndCall)
        setupSettingsButton(onRegister)
        updateCallerDisplay()
    }

    private fun setupDebugButton() {
        if (Config.DEBUG_MODE) {
            binding.debugButton.setOnClickListener {
                Utils().simulateCrash()
            }
        } else {
            binding.debugButton.isEnabled = false
        }
    }

    private fun setupCallButton(onMakeCall: (String) -> Unit, onEndCall: () -> Unit) {
        binding.callButton.setOnClickListener {
            if (MainActivity.currentCall == null) {
                viewManager.showCallDialog(
                    currentDestination = Config.DESTINATION_EXT,
                    onMakeCall = onMakeCall
                )
            } else {
                onEndCall()
            }
        }
    }

    private fun setupSettingsButton(onRegister: (String, String) -> Unit) {
        binding.settingsButton.setOnClickListener {
            viewManager.showRegistrationDialog(
                currentUsername = Config.USERNAME,
                currentPassword = Config.PASSWORD,
                onRegister = onRegister
            )
        }
    }

    private fun updateCallerDisplay() {
        "${Config.USERNAME} -> ${Config.DESTINATION_EXT}".also {
            binding.callerTextView.text = it
        }
    }

    fun disableCallButton(reason: String) {
        binding.callButton.isEnabled = false
        binding.callButton.text = reason
    }

    fun updateNetworkStatus(status: String) {
        binding.networkStatusTextView.text = status
    }
}