package com.synapes.selen_alarm_box

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.synapes.selen_alarm_box.databinding.ActivityMainBinding
import org.pjsip.pjsua2.AccountConfig
import org.pjsip.pjsua2.*

class AccountManager(
    private val context: Context,
    private val viewManager: ViewManager,
    private val binding: ActivityMainBinding
) {
    private val tag = "AccountManager"

    // Reference to MyApp instance
    private var app: MyApp? = MainActivity.app
    private var account: MyAccount? = MainActivity.account
    private var accCfg: AccountConfig? = MainActivity.accCfg
    private var currentCall: MyCall? = MainActivity.currentCall

    fun showAccountChangeDialog() {
        viewManager.showRegistrationDialog(
            currentUsername = Config.USERNAME,
            currentPassword = Config.PASSWORD
        ) { username, password ->
            handleAccountChange(username, password)
        }
    }

    private fun handleAccountChange(newUsername: String, newPassword: String) {
        try {
            // First handle any ongoing calls
            endOngoingCall()

            // Unregister current account if exists
            unregisterCurrentAccount()

            // Update configurations
            updateConfigurations(newUsername, newPassword)

            // Create and register new account
            createNewAccount()

            // Update UI
            updateUI()

            Toast.makeText(context, "Account changed successfully", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e(tag, "Error changing account: $e")
            Toast.makeText(context, "Failed to change account", Toast.LENGTH_SHORT).show()
        }
    }

    private fun endOngoingCall() {
        if (currentCall != null) {
            try {
                val prm = CallOpParam()
                prm.statusCode = pjsip_status_code.PJSIP_SC_DECLINE
                currentCall!!.hangup(prm)
                currentCall = null
            } catch (e: Exception) {
                Log.e(tag, "Error ending ongoing call: $e")
            }
        }
    }

    private fun unregisterCurrentAccount() {
        try {
            if (app?.accList?.size!! > 0 && account?.isRegistrationActive() == true) {
                account?.setRegistration(false)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error unregistering current account: $e")
        }
    }

    private fun updateConfigurations(username: String, password: String) {
        // Update Config object
        Config.USERNAME = username
        Config.PASSWORD = password
        Config.SELF_EXT = username
        Config.ACC_ID_URI = "sip:${username}@${Config.SERVER_ADDRESS}"

        // Save to preferences
        PreferencesManager.setSelfExtension(context, username)
    }

    private fun createNewAccount() {
        try {
            // Create new account config
            accCfg = AccountConfig().apply {
                idUri = Config.ACC_ID_URI
                regConfig.registrarUri = Config.REGISTRA_URI
                sipConfig.authCreds.add(
                    AuthCredInfo(
                        "digest",
                        "*",
                        Config.USERNAME,
                        0,
                        Config.PASSWORD
                    )
                )
                natConfig.iceEnabled = false
                videoConfig.autoTransmitOutgoing = false
                videoConfig.autoShowIncoming = false
            }

            // Add and register new account
            account = app!!.addAcc(accCfg!!)
            MainActivity.account = account
            MainActivity.accCfg = accCfg

        } catch (e: Exception) {
            Log.e(tag, "Error creating new account: $e")
            throw e
        }
    }

    private fun updateUI() {
        // Update caller display
        "${Config.USERNAME} -> ${Config.DESTINATION_EXT}".also {
            binding.callerTextView.text = it
        }

        // Reset call button state
        binding.callButton.text = CallButtonType.RE_REGISTER
    }

    // Helper method to check if account is registered
    fun isAccountRegistered(): Boolean {
        return try {
            account?.isRegistrationActive() ?: false
        } catch (e: Exception) {
            Log.e(tag, "Error checking account registration: $e")
            false
        }
    }

    // Helper method to get current account info
    fun getCurrentAccountInfo(): String {
        return "Username: ${Config.USERNAME}\nServer: ${Config.SERVER_ADDRESS}"
    }
}