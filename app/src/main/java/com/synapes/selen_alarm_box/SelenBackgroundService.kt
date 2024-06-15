package com.synapes.selen_alarm_box

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import org.pjsip.pjsua2.AccountConfig
import org.pjsip.pjsua2.AuthCredInfo
import org.pjsip.pjsua2.CallOpParam
import org.pjsip.pjsua2.EpConfig
import org.pjsip.pjsua2.TransportConfig
import org.pjsip.pjsua2.pj_log_decoration
import org.pjsip.pjsua2.pjsip_transport_type_e
import org.pjsip.pjsua2.pjsua_state


class SelenBackgroundService : Service() {

    private val serviceJob = Job()

    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)


    companion object {
        internal val TAG = SelenBackgroundService::class.java.simpleName
        val libraryStartedLiveData = MutableLiveData<String>()
    }

    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, " --- BACKGROUND SERVICE STARTED...")
        /*
        serviceScope.launch {
            // blocking IO operation

            if (G.ep.libGetState() > pjsua_state.PJSUA_STATE_NULL) {
                return@launch
            }

            withContext(Dispatchers.Main) {
                try {
                    val epConfig = EpConfig()

                    /* Setup our log writer */
                    val logCfg = epConfig.logConfig
                    G.logWriter = MyLogWriter()
                    logCfg.writer = G.logWriter
                    logCfg.decor = logCfg.decor and
                            (pj_log_decoration.PJ_LOG_HAS_CR or
                                    pj_log_decoration.PJ_LOG_HAS_NEWLINE).inv().toLong()

                    /* Create & init PJSUA2 */
                    try {
                        G.ep.libCreate()
                        G.ep.libInit(epConfig)
                        val threadName = Thread.currentThread().name
                        G.ep.libRegisterThread(threadName)
                    } catch (e: Exception) {
                        println(e)
                    }

                    /* Create transports and account. */
                    try {
                        val sipTpConfig = TransportConfig()
                        sipTpConfig.port = Config.SIP_LISTENING_PORT.toLong()
                        G.ep.transportCreate(
                            pjsip_transport_type_e.PJSIP_TRANSPORT_UDP,
                            sipTpConfig
                        )

                        val accCfg = AccountConfig()
                        accCfg.idUri = Config.ACC_ID_URI
                        accCfg.natConfig.iceEnabled = false
                        accCfg.regConfig.registrarUri = Config.REGISTRA_URI
                        accCfg.sipConfig.authCreds.add(
                            AuthCredInfo(
                                "digest",
                                "*",
                                Config.USERNAME,
                                0,
                                Config.PASSWORD
                            )
                        )
                        G.acc.create(accCfg, true)
                    } catch (e: Exception) {
                        println(e)
                    }

                    /* Start PJSUA2 */
                    try {
                        G.ep.libStart()
                    } catch (e: Exception) {
                        println(e)
                    }
                    // Broadcast message to the main activity
                    libraryStartedLiveData.postValue(getString(R.string.library_started_message_background))

                } catch (e: Exception) {
                    Log.e(TAG, "Error in starting PJSUA2: $e")

                }
//                while (true) {
//                    for (i in 1..10) {
//                        delay(500)
//                        Log.d(TAG, " --- Background count: $i")
//                    }
//                    Log.d(TAG, " --- Background: One cycle stopped, starting next...")
//                }
                for (i in 1..10) {
                    delay(500)
                    Log.d(TAG, " --- BACKGROUND count: $i")

                }
//                if (G.ep.libGetState() != pjsua_state.PJSUA_STATE_RUNNING) return@withContext
//
//                //            Log.d(TAG, " ---BUTTON CLICKED JNI STRING: $stringJNI ------")
//
//                if (G.call == null) {
//                    try {/* Make call (to itself) */
//                        val call = MyCall(G.acc, -1)
//                        val prm = CallOpParam(true)
//                        call.makeCall(Config.CALL_DST_URI, prm)
//                        G.call = call
//                        // Turn on the LED
//                        if (isGpioAvailable()) writeGPIO(G.LED_GPIO, 0)
//                    } catch (e: Exception) {
//                        println(e)
//                    }
//                } else {
//                    try {
//                        G.ep.hangupAllCalls()
//                        // Turn off the LED
//                        if (isGpioAvailable()) writeGPIO(G.LED_GPIO, 1)
//                    } catch (e: Exception) {
//                        println(e)
//                    }
//                }
//
//                if (G.ep.libGetState() > pjsua_state.PJSUA_STATE_NULL) {
//                    return@withContext
//                }
            }
        }
        */
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
//        serviceJob.cancel()
        Log.d(TAG, " --- BACKGROUND SERVICE DESTROYED...")
    }
}