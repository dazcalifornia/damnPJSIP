package com.synapes.selen_alarm_box

import android.Manifest
import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AcousticEchoCanceler
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Process
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.synapes.selen_alarm_box.databinding.ActivityMainBinding
import kotlinx.coroutines.*
//import org.acra.ktx.sendWithAcra
import org.json.JSONException
import org.json.JSONObject
import org.pjsip.pjsua2.AccountConfig
import org.pjsip.pjsua2.AuthCredInfo
import org.pjsip.pjsua2.BuddyConfig
import org.pjsip.pjsua2.CallInfo
import org.pjsip.pjsua2.CallOpParam
import org.pjsip.pjsua2.pjsip_inv_state
import org.pjsip.pjsua2.pjsip_status_code
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity(), Handler.Callback, MyAppObserver {
    init {
        System.loadLibrary("pjsua2")
    }

    private lateinit var binding: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper(), this)

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val debounceCoroutineScope = CoroutineScope(Dispatchers.Main)

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private lateinit var loginDialogManager: LoginDialogManager

    private var lastButtonState = 0
    private var debounceTime = 100L

    private val utils = Utils()

    private var buddyList: ArrayList<Map<String, String?>> = ArrayList()

    private var mediaPlayer: MediaPlayer? = null

    private var isSoundPlayed = false

    private var internetStatus = false

    // Add these at the top of your MainActivity
    private var registrationDialog: Dialog? = null
    private var callDialog: Dialog? = null

    private var callScreen: Dialog? = null
    private var incomingCallScreen: Dialog? = null
    private var callDurationHandler: Handler? = null
    private var callStartTime: Long = 0


    // Singleton instance
    companion object {
        internal val TAG = MainActivity::class.java.simpleName
        var app: MyApp? = null
        var currentCall: MyCall? = null
        var account: MyAccount? = null
        var accCfg: AccountConfig? = null
        private var lastCallInfo: CallInfo? = null

        var isActivityRunning: Boolean = false
    }

    suspend fun CoroutineScope.flashLedRapidly() {
        while (isActive) {
            utils.turnOnLed()
            delay(100) // delay in milliseconds
            utils.turnOffLed()
            delay(100) // delay in milliseconds
        }
    }

    override fun onStart() {
        super.onStart()
        isActivityRunning = true
    }

    override fun onStop() {
        super.onStop()
        isActivityRunning = false
    }

    private val localReceiverCheckRegistration = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, " +++ XXX Received broadcast from SelenAlarmBox: ${intent.action} +++ ")
            if (intent.action == BroadcastAction.SELEN_VOIP_APP_CHECK_REGISTRATION_LOCAL) {
                // Check registration status
                Log.d(TAG, " +++  Received broadcast from SelenAlarmBox: ${intent.action} +++ ")
                val isRegistered = account?.isRegistrationActive() ?: false
                Log.d(
                    TAG,
                    " #######  Received broadcast from SelenAlarmBox: ${account?.isRegistrationActive()} +++ "
                )
                val registrationStatus = if (isRegistered) {
                    BroadcastEventMessage.VOIP_APP_REGISTRATION_SUCCESSFUL
                } else {
                    BroadcastEventMessage.VOIP_APP_REGISTRATION_FAILED
                }

                val statusIntent = Intent(BroadcastAction.SELEN_VOIP_APP_REGISTRATION_STATE)
                statusIntent.putExtra("message", registrationStatus)
                statusIntent.putExtra("telegram_command", "CHECK_REGISTRATION")
                sendBroadcast(statusIntent)
            }

        }
    }

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            LocalBroadcastManager.getInstance(this).registerReceiver(
                localReceiverCheckRegistration,
                IntentFilter(BroadcastAction.SELEN_VOIP_APP_CHECK_REGISTRATION_LOCAL)
            )
        // Turn off the LED when the app starts
        utils.turnOffLed()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "+++ Uncaught exception in thread ${thread.name} +++", throwable)
//            RuntimeException("Uncaught exception in thread ${thread.name}: $throwable").sendWithAcra()
            restartApp()
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkPermissions()


        // Log the current speakerphone status
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        Log.d(TAG, " --- Initial Speakerphone status: ${audioManager.isSpeakerphoneOn} --- ")

        // Check if the app has MODIFY_AUDIO_SETTINGS permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, " --- MODIFY_AUDIO_SETTINGS permission not granted --- ")
            return
        }

        // Enable Acoustic Echo Cancellation (AEC)
        val sessionId = audioManager.generateAudioSessionId()
        val aec = AcousticEchoCanceler.create(sessionId)
        Log.d(TAG, " --- AEC: $aec --- ")
        if (aec != null && aec.enabled.not()) {
            Log.d(TAG, " --- AEC enabled ---")
            aec.enabled = true
        }

        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
        val percentVolume = (maxVolume * 0.81).toInt()

        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, percentVolume, 0)
//        audioManager.mode = AudioManager.MODE_NORMAL
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val audioDeviceInfo = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                .firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
            if (audioDeviceInfo != null) {
                audioManager.setCommunicationDevice(audioDeviceInfo)
                Log.d(TAG, " --- Speakerphone set using setCommunicationDevice --- ")
            } else {
                Log.e(TAG, " --- Built-in speaker not found --- ")
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.isSpeakerphoneOn = true
            Log.d(TAG, " --- Speakerphone set using deprecated method --- ")
        }

        // Log the final speakerphone status
        Log.d(TAG, " --- Final Speakerphone status: ${audioManager.isSpeakerphoneOn} --- ")


        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkRequest =
            NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED).build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(" --NetworkCallback --", " ***** Internet available ***** ")
                notifyChangeNetwork("Internet OK")
                internetStatus = true
            }

            override fun onLost(network: Network) {
                Log.d(" -- NetworkCallback", " ***** No internet connection ******")

                internetStatus = false
                notifyChangeNetwork("No Internet")
                coroutineScope.launch {
                    flashLedRapidly()
                }
                playSoundInLoop(SoundType.NO_INTERNET)
            }

        }

        connectivityManager.registerNetworkCallback(
            networkRequest, networkCallback
        )

        // END NETWORK REGISTRATION CALLBACK

        val airplaneModeStatus = utils.isAirplaneModeOn(this)
        val vpnStatus = utils.isVpnActive(this)

//        Log.d(TAG, " --- Airplane Mode Status: $airplaneModeStatus --- ")
//        Log.d(TAG, " --- VPN Status: $vpnStatus --- ")
//        Log.d(TAG, " --- Internet Status: $internetStatus --- ")

        if (airplaneModeStatus) {
//            binding.networkStatusTextView.text = "Please disable Airplane Mode"
            disableCallButton("Airplane On")
            coroutineScope.launch {
                flashLedRapidly()
            }
            playSoundInLoop(SoundType.AIRPLANE_MODE)
            return
        } else if (!vpnStatus) {
            disableCallButton("VPN is not active")
            coroutineScope.launch {
                flashLedRapidly()
            }
            playSoundInLoop(SoundType.NO_VPN)
            return
        } else {
            stopPlayingSound()
            // cancel flashing LED
            try {
                coroutineScope.cancel()
            } catch (e: Exception) {
                Log.e(TAG, "Error in coroutine cancel: $e")
//                RuntimeException("Error in coroutine cancel: $e").sendWithAcra()
            }
        }


        startSelenForegroundService()

        if (!vpnStatus || airplaneModeStatus || !internetStatus) {
            Log.d(
                TAG,
                " --- VPN is not active, or device is in airplane mode, or internet is not available. Not starting PJSUA. --- "
            )
        } else {
            startPJSUA()
        }

        // leave this one for ui hardware
        /* Setup Hardware Call button */
        if (utils.isGpioAvailable()) {
            debounceCoroutineScope.launch {
                debounceButton(this)
            }
        }

        "${Config.USERNAME} -> ${Config.DESTINATION_EXT}".also { binding.callerTextView.text = it }

        when (Config.APP_RUN_MODE) {
            RunMode.UI -> {
                configureButtons(mode = RunMode.UI)
            }

            RunMode.BACKGROUND -> {
                configureButtons(mode = RunMode.BACKGROUND)

                val backgroundIntent = Intent(this, SelenBackgroundService::class.java)
                startService(backgroundIntent)
                SelenBackgroundService.libraryStartedLiveData.observe(this) { message ->
                    binding.runModeStatusText.text = message
                }
            }

            RunMode.FOREGROUND -> {
                configureButtons(mode = RunMode.FOREGROUND)

                val foregroundIntent = Intent(this, SelenForegroundService::class.java)
                startService(foregroundIntent)
                SelenForegroundService.libraryStartedLiveData.observe(this) { message ->
                    binding.runModeStatusText.text = message
                }
            }

            else -> {
                // Handle other cases
                Log.e(TAG, " --- SHOULD NOT BE HERE ---")
            }
        }


        if (Config.DEBUG_MODE) {
            binding.debugButton.setOnClickListener() {
                utils.simulateCrash()
            }
        } else {
            binding.debugButton.isEnabled = false
        }

//        binding.callButton.setOnClickListener() {
//            if (binding.callButton.text == CallButtonTyoe.RE_REGISTER) {
//                forceReRegistration()
//            }
//
//            if (currentCall == null) {
//                try {
//                    val call = MyCall(account, -1)
//                    val prm = CallOpParam(true)
//                    call.makeCall(Config.CALL_DST_URI, prm)
//                    currentCall = call
//                    utils.turnOnLed()
//                } catch (e: Exception) {
//                    println(e)
////                    RuntimeException("Error in making call: $e").sendWithAcra()
//                }
//            } else {
//                try {
//                    val prm = CallOpParam()
//                    prm.statusCode = pjsip_status_code.PJSIP_SC_DECLINE
//                    currentCall!!.hangup(prm)
//                    utils.turnOffLed()
//                } catch (e: Exception) {
//                    println(e)
////                    RuntimeException("Error in hanging up call: $e").sendWithAcra()
//                }
//            }
//        }

//        lifecycleScope.launch {
//            checkForUpdates()
//        }

            // Modify your call button to show call dialog
            binding.callButton.setOnClickListener {
                if (currentCall == null) {
                    showCallDialog()
                } else {
                    try {
                        val prm = CallOpParam()
                        prm.statusCode = pjsip_status_code.PJSIP_SC_DECLINE
                        currentCall!!.hangup(prm)
                        utils.turnOffLed()
                    } catch (e: Exception) {
                        println(e)
                    }
                }
            }

            loginDialogManager = LoginDialogManager(this)
            // Add click listener for settings button
            binding.settingsButton.setOnClickListener {
                showSettingsDialog()
            }


            // Add a register button to your layout and handle it
            binding.settingsButton.setOnClickListener {
                showRegistrationDialog()
            }
    }


    private fun showCallingScreen(number: String) {
        callScreen = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
            setContentView(R.layout.calling_screen)
            setCancelable(false)

            val callNumberText = findViewById<TextView>(R.id.callNumberText)
            val callStatusText = findViewById<TextView>(R.id.callStatusText)
            val callDurationText = findViewById<TextView>(R.id.callDurationText)
            val endCallButton = findViewById<Button>(R.id.endCallButton)

            callNumberText.text = number
            callStatusText.text = "Calling..."

            // Start call duration timer
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
                try {
                    if (currentCall != null) {
                        val prm = CallOpParam()
                        prm.statusCode = pjsip_status_code.PJSIP_SC_DECLINE
                        currentCall!!.hangup(prm)
                        utils.turnOffLed()
                    }
                    dismiss()
                } catch (e: Exception) {
                    println(e)
                }
            }

            show()
            callDurationHandler?.postDelayed(durationRunnable, 0)
        }
    }

    private fun showIncomingCallScreen(call: MyCall) {
        incomingCallScreen = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
            setContentView(R.layout.incoming_screen)
            setCancelable(false)

            val incomingNumberText = findViewById<TextView>(R.id.incomingNumberText)
            val answerButton = findViewById<Button>(R.id.answerButton)
            val declineButton = findViewById<Button>(R.id.declineButton)

            try {
                val callInfo = call.info
                incomingNumberText.text = callInfo.remoteUri
            } catch (e: Exception) {
                println(e)
            }

            answerButton.setOnClickListener {
                try {
                    val prm = CallOpParam()
                    prm.statusCode = pjsip_status_code.PJSIP_SC_OK
                    call.answer(prm)
                    currentCall = call
                    utils.turnOnLed()
                    dismiss()

                    // Show calling screen after answering
                    showCallingScreen(call.info.remoteUri)
                } catch (e: Exception) {
                    println(e)
                }
            }

            declineButton.setOnClickListener {
                try {
                    val prm = CallOpParam()
                    prm.statusCode = pjsip_status_code.PJSIP_SC_DECLINE
                    call.hangup(prm)
                    utils.turnOffLed()
                    dismiss()
                } catch (e: Exception) {
                    println(e)
                }
            }

            show()
        }
    }


    private fun showRegistrationDialog() {
        registrationDialog = Dialog(this).apply {
            setContentView(R.layout.registration_dialog)
            setCancelable(true)

            val username = findViewById<EditText>(R.id.usernameEditText)
            val password = findViewById<EditText>(R.id.passwordEditText)
            val registerButton = findViewById<Button>(R.id.registerButton)
            val cancelButton = findViewById<Button>(R.id.cancelRegButton)

            username.setText(Config.USERNAME)
            password.setText(Config.PASSWORD)

            registerButton.setOnClickListener {
                val newUsername = username.text.toString()
                val newPassword = password.text.toString()

                if (newUsername.isBlank() || newPassword.isBlank()) {
                    Toast.makeText(this@MainActivity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Update configuration
                Config.USERNAME = newUsername
                Config.PASSWORD = newPassword
                Config.SELF_EXT = newUsername
                Config.ACC_ID_URI = "sip:${newUsername}@${Config.SERVER_ADDRESS}"

                // Save to preferences
                PreferencesManager.setSelfExtension(this@MainActivity, newUsername)

                // Force re-registration
                forceReRegistration()

                dismiss()
                Toast.makeText(this@MainActivity, "Registration updated", Toast.LENGTH_SHORT).show()
            }

            cancelButton.setOnClickListener {
                dismiss()
            }

            show()
        }
    }

    private fun showCallDialog() {
        callDialog = Dialog(this).apply {
            setContentView(R.layout.call_dialog)
            setCancelable(true)

            val destination = findViewById<EditText>(R.id.destinationEditText)
            val makeCallButton = findViewById<Button>(R.id.makeCallButton)
            val cancelButton = findViewById<Button>(R.id.cancelCallButton)

            destination.setText(Config.DESTINATION_EXT)

            makeCallButton.setOnClickListener {
                val destNumber = destination.text.toString()
                if (destNumber.isBlank()) {
                    Toast.makeText(this@MainActivity, "Please enter destination number", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Update config and make call...
                try {
                    val call = MyCall(account, -1)
                    val prm = CallOpParam(true)
                    call.makeCall(Config.CALL_DST_URI, prm)
                    currentCall = call
                    utils.turnOnLed()
                    dismiss()

                    // Show calling screen
                    showCallingScreen(destNumber)
                } catch (e: Exception) {
                    println(e)
                    Toast.makeText(this@MainActivity, "Failed to make call", Toast.LENGTH_SHORT).show()
                }
            }

            cancelButton.setOnClickListener {
                dismiss()
            }

            show()
        }
    }

    private fun showSettingsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.login_dialog)

        // Get dialog views
        val selfExtEditText = dialog.findViewById<EditText>(R.id.selfExtEditText)
        val destExtEditText = dialog.findViewById<EditText>(R.id.destinationExtEditText)
        val saveButton = dialog.findViewById<Button>(R.id.saveButton)
        val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)

        // Set current values
        selfExtEditText.setText(PreferencesManager.getSelfExtension(this))
        destExtEditText.setText(PreferencesManager.getDestinationExtension(this))

        // Handle save button
        saveButton.setOnClickListener {
            val selfExt = selfExtEditText.text.toString()
            val destExt = destExtEditText.text.toString()

            if (selfExt.isBlank() || destExt.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save new values
            PreferencesManager.setSelfExtension(this, selfExt)
            PreferencesManager.setDestinationExtension(this, destExt)

            // Update Config
            Config.SELF_EXT = selfExt
            Config.DESTINATION_EXT = destExt
            Config.ACC_ID_URI = "sip:$selfExt@${Config.SERVER_ADDRESS}"
            Config.CALL_DST_URI = "sip:$destExt@${Config.SERVER_ADDRESS}"
            Config.USERNAME = selfExt
            Config.PASSWORD = selfExt

            // Force re-registration with new credentials
            forceReRegistration()

            // Update UI
            "${Config.USERNAME} -> ${Config.DESTINATION_EXT}".also {
                binding.callerTextView.text = it
            }

            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        // Handle cancel button
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        // Show dialog
        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                showLoginDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLoginDialog() {
        loginDialogManager.showLoginDialog { selfExt, destExt ->
            // Save the new values
            PreferencesManager.setSelfExtension(this, selfExt)
            PreferencesManager.setDestinationExtension(this, destExt)

            // Update Config
            Config.SELF_EXT = selfExt
            Config.DESTINATION_EXT = destExt
            Config.ACC_ID_URI = "sip:$selfExt@${Config.SERVER_ADDRESS}"
            Config.CALL_DST_URI = "sip:$destExt@${Config.SERVER_ADDRESS}"
            Config.USERNAME = selfExt
            Config.PASSWORD = selfExt

            // Force re-registration with new credentials
            forceReRegistration()

            // Update UI
            "${Config.USERNAME} -> ${Config.DESTINATION_EXT}".also {
                binding.callerTextView.text = it
            }

            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startSelenForegroundService() {
        Log.d(TAG, " --- Starting SelenForegroundService --- ")
        val foregroundIntent = Intent(this, SelenForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(foregroundIntent)
        } else {
            startService(foregroundIntent)
        }
        SelenForegroundService.libraryStartedLiveData.observe(this) { message ->
            Log.d(TAG, " --- Foreground Service: $message --- ")
        }
    }

    private fun startPJSUA() {
        Log.d(TAG, " --- Starting PJSUA --- ")


        // Initialize PJSUA
        app = app ?: MyApp().also {
            try {
                if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
                    Log.d(TAG, " --- Debuggable mode --- Waiting for 3 seconds")
                    Thread.sleep(3000)
                }
            } catch (e: InterruptedException) {
                Log.e(TAG, "Error in thread sleep: $e")
//                RuntimeException("Error in thread sleep: $e").sendWithAcra()
            }
            it.init(this, filesDir.absolutePath)
        }
        if (app!!.accList.size == 0) {
            accCfg = AccountConfig()
            accCfg!!.idUri = Config.ACC_ID_URI
            accCfg!!.regConfig.registrarUri = Config.REGISTRA_URI
            accCfg!!.sipConfig.authCreds.add(
                AuthCredInfo(
                    "digest",
                    "*",
                    Config.USERNAME,
                    0,
                    Config.PASSWORD
                )
            )
            accCfg!!.natConfig.iceEnabled = false
            accCfg!!.videoConfig.autoTransmitOutgoing = false
            accCfg!!.videoConfig.autoShowIncoming = false
            account = app!!.addAcc(accCfg!!)
        } else {
            account = app!!.accList[0]
            accCfg = account!!.cfg
        }

        buddyList = ArrayList()

        val buddyCfg1 = BuddyConfig()
        buddyCfg1.uri = Config.CALL_DST_URI
        buddyCfg1.subscribe = true
        account!!.addBuddy(buddyCfg1)

        for (i in account!!.buddyList.indices) {
            buddyList.add(
                putData(
                    account!!.buddyList[i].cfg.uri,
                    account!!.buddyList[i].statusText
                )
            )
        }

        for (i in buddyList.indices) {
            Log.d(TAG, " +++ Buddy: ${buddyList[i]} +++ ")
        }
    }


    private suspend fun debounceButton(scope: CoroutineScope) {
        while (scope.isActive) {
            val currentButtonState = if (utils.isButtonPressed()) 0 else 1
            if (currentButtonState != lastButtonState) {
                delay(debounceTime)
                if (currentButtonState == if (utils.isButtonPressed()) 0 else 1) {
                    lastButtonState = currentButtonState
                    handleButtonState(currentButtonState)
                }
            }
            delay(50L) // check button state every 50ms
        }
    }


    private fun handleButtonState(buttonState: Int) {
        // Button State = 1 -> Button Released
        // Button State = 0 -> Button Pressed
        // LED ON = 0, LED OFF = 1

        // Pressed and no ongoing call -> make call
        if (buttonState == 0 && currentCall == null) {

            // Simulate crash
//            utils.simulateCrash()

            try {/* Make call (to itself) */
                val call = MyCall(account, -1)
                val prm = CallOpParam(true)
                call.makeCall(Config.CALL_DST_URI, prm)
                currentCall = call
                // OUTPUT GPIO0 -- GPIO1003
                // LED ON
                utils.turnOnLed()
            } catch (e: Exception) {
                println(e)
//                RuntimeException("Error in making call: $e").sendWithAcra()
            }
        }

        // Button Released and in current call -> keep led on
        else if (buttonState == 1 && currentCall != null) {
            utils.turnOnLed()
        }

        // Pressed and ongoing call -> hangup
        else if (buttonState == 0 && currentCall != null) {
            try {
                val prm = CallOpParam()
                prm.statusCode = pjsip_status_code.PJSIP_SC_DECLINE
                currentCall!!.hangup(prm)
                // if led is on turn it off
                if (buttonState == 0) utils.turnOffLed()
            } catch (e: Exception) {
                println(e)
//                RuntimeException("Error in hanging up call: $e").sendWithAcra()
            }
        }
        // Normal state (button released and no ongoing call) -> turn off led
        else if (buttonState == 1 && currentCall == null) {
            utils.turnOffLed()
        } else {
            Log.e(TAG, " ******** CAN'T REACH THIS POINT ********")
        }
    }

    private fun configureButtons(mode: String) {
        if (mode == RunMode.UI) {
            Log.d(TAG, " --- UI MODE ---")
            "UI Mode".also { binding.runModeStatusText.text = it }
        }
        if (mode == RunMode.BACKGROUND) {
            Log.d(TAG, " --- BACKGROUND MODE ---")
            disableCallButton("BG Mode")
        }
        if (mode == RunMode.FOREGROUND) {
            Log.d(TAG, " --- FOREGROUND MODE ---")
            disableCallButton("FG Mode")
        }
    }

    private fun disableCallButton(runModeText: String) {
        binding.callButton.isEnabled = false
        binding.callButton.text = runModeText
    }

    override fun onPause() {
        super.onPause()
        try {
            // Unregister PJSUA
            if (app?.accList?.size!! > 0 && account?.isRegistrationActive() == true) {
                account?.setRegistration(false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering PJSUA: $e")
//            RuntimeException("RuntimeException - Error unregistering PJSUA - onPause: $e").sendWithAcra()
        }
    }

    // Don't forget to cleanup in onDestroy
    override fun onDestroy() {
        super.onDestroy()
        callDurationHandler?.removeCallbacksAndMessages(null)
        callScreen?.dismiss()
        incomingCallScreen?.dismiss()
    }

    private fun putData(uri: String, status: String?): HashMap<String, String?> {
        val item = HashMap<String, String?>()
        item["uri"] = uri
        item["status"] = status
        return item
    }

    /***********************************************************************************************
     * PJSUA2 Callbacks
     ******************************************************************************************/
    override fun notifyRegState(code: Int, reason: String?, expiration: Long) {
        var msgStr = ""
        msgStr += if (expiration == 0L) "Unregistration" else "Registration"
        msgStr += if (code / 100 == 2) " successful" else " failed: $reason"
        Log.d(TAG, " ## OBSERVER notifyRegState ## - Registration status: $msgStr +++ ")
        val msg = handler.obtainMessage(CallMessageType.REG_STATE, msgStr)
        msg.sendToTarget()
    }

    // Update your existing notifyIncomingCall
    override fun notifyIncomingCall(call: MyCall?) {
        runOnUiThread {
            showIncomingCallScreen(call!!)
        }
    }




    // Update your existing notifyCallState
    override fun notifyCallState(call: MyCall?) {
        if (currentCall == null || call!!.id != currentCall!!.id) return

        try {
            val ci = call.info
            runOnUiThread {
                when (ci.state) {
                    pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED -> {
                        callScreen?.dismiss()
                        incomingCallScreen?.dismiss()
                        callDurationHandler?.removeCallbacksAndMessages(null)
                        currentCall = null
                        utils.turnOffLed()
                    }
                    pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED -> {
                        // Call connected
                        callScreen?.findViewById<TextView>(R.id.callStatusText)?.text = "Connected"
                    }
                    else -> {
                        // Handle other states if needed
                    }
                }
            }
        } catch (e: Exception) {
            println(e)
        }
    }

    override fun notifyCallMediaState(call: MyCall?) {
        Log.d(
            TAG,
            " ## OBSERVER notifyCallMediaState ## - Call media state: ${call!!.info.stateText}"
        )
        val msg = handler.obtainMessage(CallMessageType.CALL_MEDIA_STATE, null)
        msg.sendToTarget()
    }

    override fun notifyBuddyState(buddy: MyBuddy?) {
        try {
            Log.d(TAG, " ## OBSERVER notifyBuddyState ## - Buddy state: ${buddy!!.statusText}")
            val msg = handler.obtainMessage(CallMessageType.BUDDY_STATE, buddy)
            msg.sendToTarget()
        } catch (e: Exception) {
            Log.e(TAG, "Error in notifyBuddyState: $e")
//            RuntimeException("Error in notifyBuddyState: $e").sendWithAcra()
        }
    }

    override fun notifyChangeNetwork(status: String?) {
        try {
            if (status == null) {
                Log.e(TAG, " +++ Error in notifyChangeNetwork: status is null +++ ")
                return
            } else {
                Log.d(TAG, " ## OBSERVER notifyChangeNetwork ## - Network status: $status")
                val msg = handler.obtainMessage(CallMessageType.CHANGE_NETWORK, status)
                msg.sendToTarget()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in notifyChangeNetwork: $e")
//            RuntimeException("Error in notifyChangeNetwork: $e").sendWithAcra()
        }
    }

    /***********************************************************************************************
     * Handler Callback
     ******************************************************************************************/
    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            0 -> {
                app!!.deinit()
                finish()
                Runtime.getRuntime().gc()
                Process.killProcess(Process.myPid())
            }

            CallMessageType.CALL_STATE -> {
                val ci = msg.obj as CallInfo
                if (currentCall == null || ci == null || ci.id != currentCall!!.id) {
                    Log.e(
                        TAG,
                        " +++ handleMessage - Call state event received, but call info is invalid +++ "
                    )
                    return true
                }

                // Not in call or call done
                if (ci.state == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                    currentCall!!.delete()
                    currentCall = null
                    utils.turnOffLed()
                    if (account?.isRegistrationActive() == true) {
                        binding.callButton.text = CallButtonTyoe.CALL_HQ
                        try {
                            coroutineScope.cancel()
                            stopPlayingSound()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in coroutine cancel: $e")
//                            RuntimeException("Error in coroutine cancel: $e").sendWithAcra()
                        }
                    } else {
                        binding.callButton.text = CallButtonTyoe.RE_REGISTER
                        coroutineScope.launch { flashLedRapidly() }
                        playSoundInLoop(SoundType.RETRY_REGISTER)
                    }
                }

                val callingStates = arrayOf(
                    pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED,
                    pjsip_inv_state.PJSIP_INV_STATE_CALLING,
                    pjsip_inv_state.PJSIP_INV_STATE_EARLY
                )

                if (ci.state in callingStates) {
                    binding.callButton.text = CallButtonTyoe.CALLING

                }

                if (ci.state == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
                    binding.callButton.text = CallButtonTyoe.HANGUP
                }

                binding.callStatusTextView.text = ci.stateText
            }

            CallMessageType.CALL_MEDIA_STATE -> {
                Log.d(TAG, " +++ handleMessage - Call media only implementing audio. +++ ")
                return false
            }

            CallMessageType.CHANGE_NETWORK -> {
                val status = msg.obj as String
                Log.d(TAG, " +++ handleMessage - Network status: $status +++ ")

                try {
                    app!!.handleNetworkChange()
                } catch (e: Exception) {
                    Log.e(TAG, "Error in handling network change: $e")
//                    RuntimeException("Error in handling network change: $e").sendWithAcra()
                }
                binding.networkStatusTextView.text = status
            }

            CallMessageType.BUDDY_STATE -> {
                try {
                    val buddy = msg.obj as MyBuddy
                    Log.d(TAG, " +++ handleMessage - Buddy state: ${buddy.statusText} +++ ")

                    /* Return back Call activity */
                    notifyCallState(currentCall)

                } catch (e: Exception) {
                    Log.e(TAG, "Error in buddy state: $e")
//                    RuntimeException("Error in buddy state: $e").sendWithAcra()
                }
            }

            CallMessageType.REG_STATE -> {
                Log.d(TAG, " +++ handleMessage - Registration state: ${msg.obj} +++ ")
                var registrationInfoText = msg.obj as String
                registrationInfoText += " - ${accCfg!!.idUri}"
                binding.registrationStatusTextView.text = registrationInfoText

                if (msg.obj.toString().contains("Registration successful")) {
                    // play 'success' wav file
                    binding.callButton.text = CallButtonTyoe.CALL_HQ
                    if (!isSoundPlayed) {
                        playSuccessSoundOnce()
                        isSoundPlayed = true
                    }
                }

                if (msg.obj.toString().contains("Registration failed")) {
                    // play 'Registration failed' wav file
                    // flash LED
                    binding.callButton.text = CallButtonTyoe.RE_REGISTER
                    if (account?.isRegistrationActive() == true) {
                        binding.callButton.text = CallButtonTyoe.CALL_HQ
                        try {
                            coroutineScope.cancel()
                            stopPlayingSound()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in coroutine cancel: $e")
//                            RuntimeException("Error in coroutine cancel: $e").sendWithAcra()
                        }
                    } else {
                        binding.callButton.text = CallButtonTyoe.RE_REGISTER
                        coroutineScope.launch { flashLedRapidly() }
                        playSoundInLoop(SoundType.RETRY_REGISTER)
                    }
                }
            }

            CallMessageType.INCOMING_CALL -> {
                /* Incoming call */
                val call = msg.obj as MyCall
                val prm = CallOpParam()
                /* Only one call at anytime */
                if (currentCall != null) {
                    /*
                    prm.setStatusCode(pjsip_status_code.PJSIP_SC_BUSY_HERE);
                    try {
                        call.hangup(prm);
                    } catch (e: Exception ) {
                        Log.e(TAG, "Error in hanging up call: $e")
                    }
                     */
                    call.delete()
                    return true
                } else {
                    /* Answer with ringing */
//                    prm.statusCode = pjsip_status_code.PJSIP_SC_RINGING

                    /* Answer with OK - (answer immediately) */
                    prm.statusCode = pjsip_status_code.PJSIP_SC_OK

                    try {
                        call.answer(prm)
                        utils.turnOnLed()
                    } catch (e: Exception) {
                        utils.turnOffLed()
                        Log.e(TAG, " +++ Error in answering call: $e +++ ")
//                        RuntimeException("Error in answering call: $e").sendWithAcra()
                    }
                    currentCall = call

                    try {
                        lastCallInfo = currentCall!!.info
//                        updateCallState(lastCallInfo)
                    } catch (e: Exception) {
                        println(e)
//                        RuntimeException("Error in updating call state: $e").sendWithAcra()
                    }
                }
            }

            else -> {
                Log.d(TAG, " +++ handleMessage - Unknown message type: ${msg.what} +++ ")
                return false
            }

        }
        return true
    }

    private fun forceReRegistration() {
        try {
            // Check if there's an ongoing call
            if (currentCall != null) {
                // End the call
                val prm = CallOpParam()
                prm.statusCode = pjsip_status_code.PJSIP_SC_DECLINE
                currentCall!!.hangup(prm)
                currentCall = null
            }

            account?.setRegistration(true)
            binding.callButton.text = CallButtonTyoe.RE_REGISTER

        } catch (e: Exception) {
            Log.e(TAG, "Error in force re-registration: $e")
//            RuntimeException("Error in force re-registration: $e").sendWithAcra()
        }
    }

    private fun restartApp() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val mPendingIntentId = 123456
        val mPendingIntent = PendingIntent.getActivity(
            applicationContext,
            mPendingIntentId,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val mgr = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
        System.exit(0)
    }

    private fun playSoundInLoop(soundFileType: String) {
        coroutineScope.launch {
            if (mediaPlayer == null) {
                when (soundFileType) {
                    SoundType.NO_VPN -> {
                        mediaPlayer = MediaPlayer.create(this@MainActivity, R.raw.no_vpn).apply {
                            start()
                        }
                    }

                    SoundType.AIRPLANE_MODE -> {
                        mediaPlayer = MediaPlayer.create(
                            this@MainActivity,
                            R.raw.no_internet_turn_off_airplane_mode
                        ).apply {
                            start()
                        }
                    }

                    SoundType.NO_INTERNET -> {
                        mediaPlayer = MediaPlayer.create(
                            this@MainActivity,
                            R.raw.no_internet_check_mobile_data
                        ).apply {
                            start()
                        }
                    }

                    SoundType.RETRY_REGISTER -> {
                        mediaPlayer =
                            MediaPlayer.create(this@MainActivity, R.raw.registration_failed).apply {
                                start()
                            }
                    }
                }
            }
            while (isActive) { // Loop as long as the coroutine is active
                delay(10000) // wait for 10 seconds
                if (mediaPlayer != null) {
                    mediaPlayer?.start() // play the sound again
                }
            }
        }
    }

    private fun playSuccessSoundOnce() {
        mediaPlayer = MediaPlayer.create(this@MainActivity, R.raw.success).apply {
            start()
        }
    }

    private fun stopPlayingSound() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    object CallMessageType {
        const val INCOMING_CALL = 1
        const val CALL_STATE = 2
        const val REG_STATE = 3
        const val BUDDY_STATE = 4
        const val CALL_MEDIA_STATE = 5
        const val CHANGE_NETWORK = 6
    }

    object CallButtonTyoe {
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


    private fun checkPermissions() {
        // TODO: READ MORE PERMISSIONS
        val permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.FOREGROUND_SERVICE_MICROPHONE,
            Manifest.permission.FOREGROUND_SERVICE_PHONE_CALL,
            Manifest.permission.FOREGROUND_SERVICE_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
        ActivityCompat.requestPermissions(this@MainActivity, permissions, 0)
    }

    @Throws(IOException::class)
    fun downloadData(strUrl: String?): String {
        var data = ""
        if (strUrl == null) {
            throw IllegalArgumentException("URL cannot be null")
        }
        try {
            val url = URL(strUrl)
            val connection = url.openConnection() as? HttpURLConnection
            connection?.setRequestMethod("GET")
            connection?.connect()
            val `is` = connection?.inputStream
            if (`is` == null) {
                throw IOException("Could not open connection")
            }
            val br = BufferedReader(InputStreamReader(`is`))
            val sb = StringBuffer()
            var line: String? = ""
            while (br.readLine().also { line = it } != null) {
                sb.append(line)
            }
            data = sb.toString()
            br.close()
            `is`.close()
        } catch (e: java.lang.Exception) {
            Log.d(TAG, "downloadData: Error! ", e)
            throw e
        }
        return data
    }


    private fun showNegativeMessage() {
        runOnUiThread {
            Toast.makeText(
                this@MainActivity,
                "Error in downloading JSON data",
                Toast.LENGTH_LONG
            ).show()
        }
    }

//    override fun finished(output: String?) {
//        progressDialog?.dismiss()
//    }
//
//    override fun setPercent(percent: String?) {
//        progressDialog?.setMessage("Downloading " + percent);
//    }


}