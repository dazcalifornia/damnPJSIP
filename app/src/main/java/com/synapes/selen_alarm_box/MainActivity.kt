package com.synapes.selen_alarm_box

import android.app.AlarmManager
import android.app.PendingIntent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo

import android.media.MediaPlayer

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Process
import android.util.Log
import android.view.Menu
import android.view.MenuItem

import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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

    private lateinit var loginDialogManager: LoginDialogManager
    private lateinit var viewManager: ViewManager

    private lateinit var audioManager: AudioSystemManager
    private lateinit var networkManager: NetworkManager
    private lateinit var deviceManager: DeviceStatusManager
    private lateinit var uiManager: UIManager
    private lateinit var serviceManager: ServiceManager
    private lateinit var accountManager: AccountManager
    private lateinit var callStateManager: CallStateManager

    private val utils = Utils()
    private var buddyList: ArrayList<Map<String, String?>> = ArrayList()
    private var mediaPlayer: MediaPlayer? = null
    private var isSoundPlayed = false
    private var internetStatus = false

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
        setupBasicUI()
        setupErrorHandler()
        initializeManagers()
        startServices()

        accountManager = AccountManager(this, viewManager, binding)
        callStateManager = CallStateManager(this, viewManager)

        PermissionsHelper.checkAndRequestPermissions(this)
        startSelenForegroundService()

        LocalBroadcastManager.getInstance(this).registerReceiver(
            localReceiverCheckRegistration,
            IntentFilter(BroadcastAction.SELEN_VOIP_APP_CHECK_REGISTRATION_LOCAL)
        )
        // Turn off the LED when the app starts
        utils.turnOffLed()


        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Setup settings button
        binding.settingsButton.setOnClickListener {
            accountManager.showAccountChangeDialog()
        }

    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!PermissionsHelper.areAllPermissionsGranted(this)) {
            PermissionsHelper.handlePermissionResult(this, requestCode, permissions, grantResults)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                accountManager.showAccountChangeDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
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
        networkManager.cleanup()
        coroutineScope.cancel()
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
            try {
                viewManager.showIncomingCallScreen(
                    remoteUri = call!!.info.remoteUri,
                    onAnswer = {
                        try {
                            val prm = CallOpParam()
                            prm.statusCode = pjsip_status_code.PJSIP_SC_OK
                            call.answer(prm)
                            currentCall = call
                            utils.turnOnLed()

                            // Show calling screen
                            viewManager.showCallingScreen(
                                number = call.info.remoteUri,
                                onEndCall = {
                                    try {
                                        val hangupPrm = CallOpParam()
                                        hangupPrm.statusCode = pjsip_status_code.PJSIP_SC_DECLINE
                                        currentCall!!.hangup(hangupPrm)
                                        utils.turnOffLed()
                                    } catch (e: Exception) {
                                        println(e)
                                    }
                                }
                            )
                        } catch (e: Exception) {
                            println(e)
                        }
                    },
                    onDecline = {
                        try {
                            val prm = CallOpParam()
                            prm.statusCode = pjsip_status_code.PJSIP_SC_DECLINE
                            call.hangup(prm)
                            utils.turnOffLed()
                        } catch (e: Exception) {
                            println(e)
                        }
                    }
                )
            } catch (e: Exception) {
                println(e)
            }
        }
    }

    // Update your existing notifyCallState

    override fun notifyCallState(call: MyCall?) {
        if (currentCall == null || call == null) return

        try {
            val ci = call.info
            when (ci.state) {
                pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED -> {
                    callStateManager.handleEvent(CallEvent.CallDisconnected)
                    currentCall = null  // Clear the reference
                    utils.turnOffLed()
                }
                pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED -> {
                    callStateManager.handleEvent(CallEvent.CallConnected)
                }
                else -> {}
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in notifyCallState: $e")
            callStateManager.handleEvent(CallEvent.CallError(e.message ?: "Unknown error"))
        }
    }

//    override fun notifyCallState(call: MyCall?) {
//        if (currentCall == null || call!!.id != currentCall!!.id) return
//
//        try {
//            val ci = call.info
//            runOnUiThread {
//                when (ci.state) {
//                    pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED -> {
//                        viewManager.dismissCallScreens()
//                        currentCall = null
//                        utils.turnOffLed()
//                    }
//                    pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED -> {
//                        viewManager.updateCallStatus("Connected")
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            println(e)
//        }
//    }

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
            runOnUiThread {
                binding.networkStatusTextView.text = status
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
                        binding.callButton.text = CallButtonType.CALL_HQ
                        try {
                            coroutineScope.cancel()
                            stopPlayingSound()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in coroutine cancel: $e")
//                            RuntimeException("Error in coroutine cancel: $e").sendWithAcra()
                        }
                    } else {
                        binding.callButton.text = CallButtonType.RE_REGISTER
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
                    binding.callButton.text = CallButtonType.CALLING

                }

                if (ci.state == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
                    binding.callButton.text = CallButtonType.HANGUP
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
//                    app!!.handleNetworkChange()
                    if (app != null) {
                        app!!.handleNetworkChange()
                        Log.d(TAG, "Network change handled successfully")
                    } else {
                        Log.d(TAG, "App is null, attempting to initialize PJSUA")
                        if (!utils.isAirplaneModeOn(this) && utils.isVpnActive(this)) {
                            startPJSUA()
                        }
                    }
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
                    binding.callButton.text = CallButtonType.CALL_HQ
                    if (!isSoundPlayed) {
                        playSuccessSoundOnce()
                        isSoundPlayed = true
                    }
                }

                if (msg.obj.toString().contains("Registration failed")) {
                    // play 'Registration failed' wav file
                    // flash LED
                    binding.callButton.text = CallButtonType.RE_REGISTER
                    if (account?.isRegistrationActive() == true) {
                        binding.callButton.text = CallButtonType.CALL_HQ
                        try {
                            coroutineScope.cancel()
                            stopPlayingSound()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in coroutine cancel: $e")
//                            RuntimeException("Error in coroutine cancel: $e").sendWithAcra()
                        }
                    } else {
                        binding.callButton.text = CallButtonType.RE_REGISTER
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
            binding.callButton.text = CallButtonType.RE_REGISTER

        } catch (e: Exception) {
            Log.e(TAG, "Error in force re-registration: $e")
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


//    before that
private fun setupBasicUI() {
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    viewManager = ViewManager(this)
    enableEdgeToEdge()
    setupWindowInsets()
    utils.turnOffLed()
}

    private fun setupErrorHandler() {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", throwable)
            restartApp()
        }
    }

    private fun initializeManagers() {
        // Initialize managers
        audioManager = AudioSystemManager(this)

        networkManager = NetworkManager(
            context = this,
            onNetworkAvailable = { handleNetworkAvailable() },
            onNetworkLost = { handleNetworkLost() }
        )

        deviceManager = DeviceStatusManager(
            context = this,
            utils = utils,
            networkManager = networkManager,
            onAirplaneMode = { handleAirplaneMode() },
            onNoVPN = { handleNoVPN() },
            onNoInternet = { handleNoInternet() },
            onNormalState = { handleNormalState() }
        )

        uiManager = UIManager(this, binding, viewManager)

        // Setup systems
        PermissionsHelper.checkAndRequestPermissions(this)
        audioManager.setupAudioSystem()
        networkManager.setupNetworkMonitoring()

        if (deviceManager.checkDeviceStatus()) {
            startPJSUA()
        }

        uiManager.setupUIComponents(
            onMakeCall = { destination -> handleMakeCall(destination) },
            onEndCall = { handleEndCall() },
            onRegister = { username, password -> handleRegistration(username, password) }
        )
        serviceManager = ServiceManager(
            context = this,
            onServiceMessage = { message ->
                binding.runModeStatusText.text = message
            }
        )
    }

    private fun startServices() {
        serviceManager.startServices()
    }

//    new Things

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun handleNetworkAvailable() {
        Log.d(TAG, " ***** Internet available ***** ")
        notifyChangeNetwork("Internet OK")
        internetStatus = true
    }
    private fun handleNetworkLost() {
        Log.d(TAG, " ***** No internet connection ******")
        internetStatus = false
        notifyChangeNetwork("No Internet")
        coroutineScope.launch { flashLedRapidly() }
        playSoundInLoop(SoundType.NO_INTERNET)
    }
    private fun handleNoInternet() {
        disableCallButton("No Internet")
        coroutineScope.launch { flashLedRapidly() }
        playSoundInLoop(SoundType.NO_INTERNET)
    }
    private fun handleAirplaneMode() {
        disableCallButton("Airplane On")
        coroutineScope.launch { flashLedRapidly() }
        playSoundInLoop(SoundType.AIRPLANE_MODE)
    }
    private fun handleNoVPN() {
        disableCallButton("VPN is not active")
        coroutineScope.launch { flashLedRapidly() }
        playSoundInLoop(SoundType.NO_VPN)
    }
    private fun handleNormalState() {
        stopPlayingSound()
        try {
            coroutineScope.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error in coroutine cancel: $e")
        }
    }
    // Before making a new call, check if we can
    private fun handleMakeCall(destination: String) {
        try {
            // Update Config with new destination
            Config.DESTINATION_EXT = destination
            Config.CALL_DST_URI = "sip:${destination}@${Config.SERVER_ADDRESS}"

            // Save to preferences
            PreferencesManager.setDestinationExtension(this, destination)

            // Update UI
            binding.callerTextView.text = "${Config.USERNAME} -> ${Config.DESTINATION_EXT}"

            // Make the call
            val call = MyCall(account, -1)
            val prm = CallOpParam(true)
            call.makeCall(Config.CALL_DST_URI, prm)
            currentCall = call
            utils.turnOnLed()

            // Show calling screen
            viewManager.showCallingScreen(
                number = destination,
                onEndCall = { handleEndCall() }
            )
        } catch (e: Exception) {
            println(e)
            Toast.makeText(this, "Failed to make call", Toast.LENGTH_SHORT).show()
        }
    }


    private fun handleEndCall() {
        try {
            if (currentCall != null) {
                val hangupPrm = CallOpParam()
                hangupPrm.statusCode = pjsip_status_code.PJSIP_SC_DECLINE
                currentCall!!.hangup(hangupPrm)
                currentCall = null  // Clear the reference
                utils.turnOffLed()

                // Make sure state manager knows about the end call
                callStateManager.handleEvent(CallEvent.EndCall)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error ending call: $e")
            callStateManager.handleEvent(CallEvent.CallError(e.message ?: "Unknown error"))
        }
    }
    private fun handleRegistration(username: String, password: String) {
        // Update configuration
        Config.USERNAME = username
        Config.PASSWORD = password
        Config.SELF_EXT = username
        Config.ACC_ID_URI = "sip:${username}@${Config.SERVER_ADDRESS}"

        // Save to preferences
        PreferencesManager.setSelfExtension(this, username)

        // Force re-registration
        forceReRegistration()
    }

}