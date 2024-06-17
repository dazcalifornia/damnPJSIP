package com.synapes.selen_alarm_box

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AcousticEchoCanceler
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Process
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.synapes.selen_alarm_box.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import org.pjsip.pjsua2.AccountConfig
import org.pjsip.pjsua2.AuthCredInfo
import org.pjsip.pjsua2.BuddyConfig
import org.pjsip.pjsua2.CallInfo
import org.pjsip.pjsua2.CallOpParam
import org.pjsip.pjsua2.pjsip_inv_state
import org.pjsip.pjsua2.pjsip_status_code

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

    private var lastButtonState = 0
    private var debounceTime = 250L

    private val utils = Utils()

    private var buddyList: ArrayList<Map<String, String?>> = ArrayList()

    private var mediaPlayer: MediaPlayer? = null

    private var isSoundPlayed = false

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

    private val airplaneModeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_AIRPLANE_MODE_CHANGED) {
                val isAirplaneModeOn = intent.getBooleanExtra("state", false)
                if (!isAirplaneModeOn) {
                    // Airplane mode has been turned off
                    // Do what you need to do here
                    binding.networkStatusTextView.text = "Airplane Mode is On"

                } else {
                    // Airplane mode has been turned on
                    // Do what you need to do here
                    binding.networkStatusTextView.text = "Airplane Mode is Off"
                }

            }
        }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Turn off the LED when the app starts
        utils.turnOffLed()

        registerReceiver(airplaneModeReceiver, IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED))

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("Error", "Uncaught exception in thread ${thread.name}", throwable)
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


        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // Enable Acoustic Echo Cancellation (AEC)
        val sessionId = audioManager.generateAudioSessionId()
        val aec = AcousticEchoCanceler.create(sessionId)
        if (aec != null && aec.enabled.not()) {
            Log.d(TAG, " --- AEC enabled ---")
            aec.enabled = true
        }

        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
        val percentVolume = (maxVolume * 0.81).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, percentVolume, 0)
        audioManager.mode = AudioManager.MODE_NORMAL
        audioManager.isSpeakerphoneOn = true


        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkRequest =
            NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED).build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                Log.d(TAG, " +++ NetworkCallback: Capabilities changed +++ ")
                // TODO: implement notify observer
                notifyChangeNetwork("Network CHANGED")
//                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
//                    notifyChangeNetwork("Internet Available")
//                } else {
//                    notifyChangeNetwork("No Internet Connection")
//                }
            }

            override fun onAvailable(network: Network) {
                Log.d(" --NetworkCallback --", " ***** Internet available ***** ")
                notifyChangeNetwork("Internet OK")
            }

            override fun onLost(network: Network) {
                Log.d(" -- NetworkCallback", " ***** No internet connection ******")

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

        // Set the network status text on first start
        if (utils.isNetworkDeviceTurnedOn(this)) {
            binding.networkStatusTextView.text = getString(R.string.internet_up)
        } else {
            binding.networkStatusTextView.text = getString(R.string.internet_down)
        }

        val vpnStatus = utils.isVpnActive(this)
        val airplaneModeStatus = utils.isAirplaneModeOn(this)
        Log.d(TAG, " --- Airplane Mode Status: $airplaneModeStatus --- ")
        Log.d(TAG, " --- VPN Status: $vpnStatus --- ")
        if (!vpnStatus) {
            // play sound
            // flash LED
            disableCallButton("VPN is not active")
            coroutineScope.launch {
                flashLedRapidly()
            }
            playSoundInLoop(SoundType.NO_VPN)
        } else if (airplaneModeStatus) {
            binding.networkStatusTextView.text = "Please disable Airplane Mode"
            coroutineScope.launch {
                flashLedRapidly()
            }
            playSoundInLoop(SoundType.AIRPLANE_MODE)
        } else {
            stopPlayingSound()
            // cancel flashing LED
            try {
                coroutineScope.cancel()
            } catch (e: Exception) {
                Log.e(TAG, "Error in coroutine cancel: $e")
            }
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

        startPJSUA()

        binding.callButton.setOnClickListener() {
            if (binding.callButton.text == CallButtonTyoe.RE_REGISTER) {
                forceReRegistration()
            }

            if (currentCall == null) {
                try {
                    val call = MyCall(account, -1)
                    val prm = CallOpParam(true)
                    call.makeCall(Config.CALL_DST_URI, prm)
                    currentCall = call
                    utils.turnOnLed()
                } catch (e: Exception) {
                    println(e)
                }
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
    }

    private fun startPJSUA() {
        Log.d(TAG, " --- Starting PJSUA --- ")

        // Start foreground notification service anyway (regardless of the run mode)
        // This is because the service is needed to keep the app alive
        // And run on Android 7 will crash if run in foreground only
        val foregroundIntent = Intent(this, SelenForegroundService::class.java)
        startService(foregroundIntent)
        SelenForegroundService.libraryStartedLiveData.observe(this) { message ->
            Log.d(TAG, " --- Foreground Service: $message --- ")
        }

        // Initialize PJSUA
        app = app ?: MyApp().also {
            try {
                if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
                    Log.d(TAG, " --- Debuggable mode --- Waiting for 3 seconds")
                    Thread.sleep(3000)
                }
            } catch (e: InterruptedException) {
                Log.e(TAG, "Error in thread sleep: $e")
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

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, " --- onDestroy ---")
        unregisterReceiver(airplaneModeReceiver)
        connectivityManager.unregisterNetworkCallback(networkCallback)
        stopPlayingSound()
        coroutineScope.cancel()
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
            Manifest.permission.FOREGROUND_SERVICE_LOCATION
        )
        ActivityCompat.requestPermissions(this@MainActivity, permissions, 0)
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

    override fun notifyIncomingCall(call: MyCall?) {
        Log.d(
            TAG,
            " ## OBSERVER notifyIncomingCall ## - Incoming call: ${call!!.info.remoteUri}"
        )
        val msg = handler.obtainMessage(CallMessageType.INCOMING_CALL, call)
        msg.sendToTarget()
    }

    override fun notifyCallState(call: MyCall?) {
        Log.d(TAG, " ## OBSERVER notifyCallState ## - Call state: ${call?.info?.stateText}")
        if (currentCall == null || call!!.id != currentCall!!.id) return
        var ci: CallInfo? = null
        try {
            ci = call.info
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (ci != null) {
            val msg = handler.obtainMessage(CallMessageType.CALL_STATE, ci)
            msg.sendToTarget()
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
        Log.d(TAG, " ## OBSERVER notifyBuddyState ## - Buddy state: ${buddy!!.statusText}")
        val msg = handler.obtainMessage(CallMessageType.BUDDY_STATE, buddy)
        msg.sendToTarget()
    }

    override fun notifyChangeNetwork(status: String?) {
        Log.d(TAG, " ## OBSERVER notifyChangeNetwork ## - Network status: $status")
        val msg = handler.obtainMessage(CallMessageType.CHANGE_NETWORK, status)
        msg.sendToTarget()
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

                app!!.handleNetworkChange()
                binding.networkStatusTextView.text = status
            }

            CallMessageType.BUDDY_STATE -> {
                val buddy = msg.obj as MyBuddy
                Log.d(TAG, " +++ handleMessage - Buddy state: ${buddy.statusText} +++ ")
//                binding.destinationTextView.text =
//                    "HQ #${Config.DESTINATION_EXT}: Status: ${buddy.statusText}"

                /* Return back Call activity */
                notifyCallState(currentCall)
            }

            CallMessageType.REG_STATE -> {
                Log.d(TAG, " +++ handleMessage - Registration state: ${msg.obj} +++ ")
                var registrationInfoText = msg.obj as String
                registrationInfoText += " - ${accCfg!!.idUri}"
                binding.registrationStatusTextView.text = registrationInfoText
//                binding.registrationInfotextView.text = registrationInfoText
//
                if (msg.obj.toString().contains("Registration successful")) {
                    // play 'success' wav file
                    binding.callButton.text = CallButtonTyoe.CALL_HQ
                    if (!isSoundPlayed) {
                        playSuccessSoundOnce()
                        isSoundPlayed = true
                    }
                }
//
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
                    // TODO: simulate call when busy
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
                    }
                    currentCall = call

                    try {
                        lastCallInfo = currentCall!!.info
//                        updateCallState(lastCallInfo)
                    } catch (e: Exception) {
                        println(e)
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
        }
    }

    private fun restartApp() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, pendingIntent)
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
}