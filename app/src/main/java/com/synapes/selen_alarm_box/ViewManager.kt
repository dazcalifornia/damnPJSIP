package com.synapes.selen_alarm_box

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class ViewManager(private val context: Context) {
    private var registrationDialog: Dialog? = null
    private var callDialog: Dialog? = null
    private var callingScreen: Dialog? = null
    private var incomingCallScreen: Dialog? = null
    private var callDurationHandler: Handler? = null
    private var callStartTime: Long = 0

    // And in ViewManager.kt, update the registration dialog implementation:
    fun showRegistrationDialog(
        currentUsername: String,
        currentPassword: String,
        onRegister: (username: String, password: String) -> Unit
    ) {
        registrationDialog = Dialog(context).apply {
            setContentView(R.layout.registration_dialog)
            setCancelable(true)

            // Find views
            val usernameInput = findViewById<EditText>(R.id.usernameEditText)
            val passwordInput = findViewById<EditText>(R.id.passwordEditText)
            val registerButton = findViewById<Button>(R.id.registerButton)
            val cancelButton = findViewById<Button>(R.id.cancelRegButton)
//            val statusText = findViewById<TextView>(R.id.registrationStatusText)

            // Set current values
            usernameInput.setText(currentUsername)
            passwordInput.setText(currentPassword)

            registerButton.setOnClickListener {
                val newUsername = usernameInput.text.toString()
                val newPassword = passwordInput.text.toString()

                if (newUsername.isBlank() || newPassword.isBlank()) {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Show loading state
                registerButton.isEnabled = false
//                statusText.text = "Changing account..."

                // Trigger account change
                onRegister(newUsername, newPassword)
                dismiss()
            }

            cancelButton.setOnClickListener {
                dismiss()
            }

            show()
        }
    }

//    fun showRegistrationDialog(
//        currentUsername: String,
//        currentPassword: String,
//        onRegister: (username: String, password: String) -> Unit
//    ) {
//        registrationDialog = Dialog(context).apply {
//            setContentView(R.layout.registration_dialog)
//            setCancelable(true)
//
//            val username = findViewById<EditText>(R.id.usernameEditText)
//            val password = findViewById<EditText>(R.id.passwordEditText)
//            val registerButton = findViewById<Button>(R.id.registerButton)
//            val cancelButton = findViewById<Button>(R.id.cancelRegButton)
//
//            username.setText(currentUsername)
//            password.setText(currentPassword)
//
//            registerButton.setOnClickListener {
//                val newUsername = username.text.toString()
//                val newPassword = password.text.toString()
//
//                if (newUsername.isBlank() || newPassword.isBlank()) {
//                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
//                    return@setOnClickListener
//                }
//
//                onRegister(newUsername, newPassword)
//                dismiss()
//                Toast.makeText(context, "Registration updated", Toast.LENGTH_SHORT).show()
//            }
//
//            cancelButton.setOnClickListener {
//                dismiss()
//            }
//
//            show()
//        }
//    }

    fun showCallDialog(
        currentDestination: String,
        onMakeCall: (destination: String) -> Unit
    ) {
        callDialog = Dialog(context).apply {
            setContentView(R.layout.call_dialog)
            setCancelable(true)

            val destination = findViewById<EditText>(R.id.destinationEditText)
            val makeCallButton = findViewById<Button>(R.id.makeCallButton)
            val cancelButton = findViewById<Button>(R.id.cancelCallButton)

            destination.setText(currentDestination)

            makeCallButton.setOnClickListener {
                val destNumber = destination.text.toString()

                if (destNumber.isBlank()) {
                    Toast.makeText(context, "Please enter destination number", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                onMakeCall(destNumber)
                dismiss()
            }

            cancelButton.setOnClickListener {
                dismiss()
            }

            show()
        }
    }

    fun showCallingScreen(
        number: String,
        onEndCall: () -> Unit
    ) {
        callingScreen = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
            setContentView(R.layout.calling_screen)
            setCancelable(false)

            val callNumberText = findViewById<TextView>(R.id.callNumberText)
            val callStatusText = findViewById<TextView>(R.id.callStatusText)
            val callDurationText = findViewById<TextView>(R.id.callDurationText)
            val endCallButton = findViewById<Button>(R.id.endCallButton)

            callNumberText.text = number
            callStatusText.text = "Calling..."

            callStartTime = System.currentTimeMillis()
            callDurationHandler = Handler(Looper.getMainLooper())

            val durationRunnable = object : Runnable {
                override fun run() {
                    val duration = System.currentTimeMillis() - callStartTime
                    val seconds = (duration / 1000) % 60
                    val minutes = (duration / (1000 * 60)) % 60
                    callDurationText.text = String.format("%02d:%02d", minutes, seconds)
                    callDurationHandler?.postDelayed(this, 1000)
                }
            }

            endCallButton.setOnClickListener {
                onEndCall()
                dismiss()
            }

            show()
            callDurationHandler?.postDelayed(durationRunnable, 0)
        }
    }

    fun showIncomingCallScreen(
        remoteUri: String,
        onAnswer: () -> Unit,
        onDecline: () -> Unit
    ) {
        incomingCallScreen = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
            setContentView(R.layout.incoming_screen)
            setCancelable(false)

            val incomingNumberText = findViewById<TextView>(R.id.incomingNumberText)
            val answerButton = findViewById<Button>(R.id.answerButton)
            val declineButton = findViewById<Button>(R.id.declineButton)

            incomingNumberText.text = remoteUri

            answerButton.setOnClickListener {
                onAnswer()
                dismiss()
            }

            declineButton.setOnClickListener {
                onDecline()
                dismiss()
            }

            show()
        }
    }

    fun updateCallStatus(status: String) {
        callingScreen?.findViewById<TextView>(R.id.callStatusText)?.text = status
    }

    fun dismissAll() {
        registrationDialog?.dismiss()
        callDialog?.dismiss()
        callingScreen?.dismiss()
        incomingCallScreen?.dismiss()
        callDurationHandler?.removeCallbacksAndMessages(null)
    }

    fun dismissCallScreens() {
        callingScreen?.dismiss()
        incomingCallScreen?.dismiss()
        callDurationHandler?.removeCallbacksAndMessages(null)
    }


}