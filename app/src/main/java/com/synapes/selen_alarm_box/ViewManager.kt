@file:Suppress("DEPRECATION")

package com.synapes.selen_alarm_box

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ViewManager(private val context: Context) {
    private var registrationDialog: Dialog? = null
    private var callDialog: Dialog? = null
    private var callingScreen: Dialog? = null
    private var incomingCallScreen: Dialog? = null
    private var callDurationHandler: Handler? = null
    private var callStartTime: Long = 0
    private var isMuted = false
    private var isSpeakerOn = false
    private val audioManager = AudioSystemManager(context)

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


    // In ViewManager.kt
    fun showCallDialog(
        currentDestination: String,
        onMakeCall: (destination: String) -> Unit
    ) {
        // Dismiss any existing dialog first
        callDialog?.dismiss()

        callDialog = Dialog(context).apply {
            setContentView(R.layout.call_dialog)
            setCancelable(true)

            val destination = findViewById<EditText>(R.id.destinationEditText)
            val makeCallButton = findViewById<Button>(R.id.makeCallButton)
            val cancelButton = findViewById<Button>(R.id.cancelCallButton)

            // Clear previous text and set new destination
            destination.text.clear()
            destination.setText(currentDestination)

            // Allow editing
            destination.isEnabled = true

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
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )

            // Find views
            val callNumberText = findViewById<TextView>(R.id.callNumberText)
            val callStatusText = findViewById<TextView>(R.id.callStatusText)
            val callDurationText = findViewById<TextView>(R.id.callDurationText)
            val endCallButton = findViewById<FloatingActionButton>(R.id.endCallButton)
            val muteButton = findViewById<FloatingActionButton>(R.id.muteButton)
            val speakerButton = findViewById<FloatingActionButton>(R.id.speakerButton)

            // Set initial values
            callNumberText.text = number
            callStatusText.text = "Calling..."

            // Setup duration timer
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

            // Set button click listeners
            endCallButton.setOnClickListener {
                onEndCall()
                dismiss()
            }

            muteButton.setOnClickListener {
                isMuted = !isMuted
                muteButton.apply {
                    setImageResource(
                        if (isMuted) android.R.drawable.ic_lock_silent_mode
                        else android.R.drawable.ic_lock_silent_mode_off
                    )
                    backgroundTintList = ColorStateList.valueOf(
                        if (isMuted) context.getColor(R.color.red)
                        else context.getColor(R.color.white)
                    )
                }
                audioManager.toggleMute(isMuted)
            }

            speakerButton.setOnClickListener {
                isSpeakerOn = !isSpeakerOn
                speakerButton.apply {
                    setImageResource(
                        if (isSpeakerOn) android.R.drawable.ic_lock_silent_mode_off
                        else android.R.drawable.ic_lock_silent_mode
                    )
                    backgroundTintList = ColorStateList.valueOf(
                        if (isSpeakerOn) context.getColor(R.color.red)
                        else context.getColor(R.color.white)
                    )
                }
                audioManager.toggleSpeaker(isSpeakerOn)
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
        incomingCallScreen = Dialog(context, R.style.FullScreenDialog).apply {
            setContentView(R.layout.incoming_screen)
            setCancelable(false)
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )

            val incomingNumberText = findViewById<TextView>(R.id.incomingNumberText)
            val answerButton = findViewById<FloatingActionButton>(R.id.answerButton)
            val declineButton = findViewById<FloatingActionButton>(R.id.declineButton)

            incomingNumberText.text = remoteUri

            answerButton.setOnClickListener {
                onAnswer()
                dismiss()
                // Show calling screen when call is answered
                showCallingScreen(remoteUri) { onDecline() }
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

    fun dismissCallScreens() {
        callingScreen?.let {
            // Reset audio states
            if (isMuted) {
                audioManager.toggleMute(false)
                isMuted = false
            }
            if (isSpeakerOn) {
                audioManager.toggleSpeaker(false)
                isSpeakerOn = false
            }
            it.dismiss()
        }
        incomingCallScreen?.dismiss()
        callDurationHandler?.removeCallbacksAndMessages(null)
    }

    fun dismissAll() {
        registrationDialog?.dismiss()
        callDialog?.dismiss()
        dismissCallScreens()
    }

}