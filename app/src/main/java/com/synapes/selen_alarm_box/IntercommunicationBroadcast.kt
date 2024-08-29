package com.synapes.selen_alarm_box

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.synapes.selen_alarm_box.MainActivity.Companion.account
import com.synapes.selen_alarm_box.MainActivity.Companion.currentCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.pjsip.pjsua2.CallOpParam
import org.pjsip.pjsua2.pjsip_status_code
import kotlin.random.Random

class IntercommunicationBroadcast: BroadcastReceiver() {
    private var callJob: Job? = null

    override fun onReceive(context: Context, intent: Intent) {
        val intentAction = intent.action
        val message = intent.getStringExtra("message")
        val deviceId = intent.getStringExtra("device_id")
        val destinationNumber = intent.getStringExtra("destination_number")
        val testMode = intent.getBooleanExtra("test_mode", false)
        val testModeActive = intent.getBooleanExtra("test_mode_active", false)

        Log.d("IntercommunicationBroadcast", "Action: $intentAction, Message: $message, Device ID: $deviceId, Destination Number: $destinationNumber, test_mode: $testMode, test_mode_active: $testModeActive")

        if (intentAction == BroadcastAction.SELEN_VOIP_APP_CALL_HQ) {
            Log.d("IntercommunicationBroadcast", "Received broadcast from SelenAlarmBox: $message")
            Log.d("IntercommunicationBroadcast", "device_id: $deviceId, destination_number: $destinationNumber")

            if (testModeActive) {
                callJob = CoroutineScope(Dispatchers.IO).launch {
                    while (isActive) {
                        val callIntent = Intent(BroadcastAction.SELEN_VOIP_APP_CALL_HQ_LOCAL)
                        callIntent.putExtra("destination_number", destinationNumber)
                        LocalBroadcastManager.getInstance(context).sendBroadcast(callIntent)
                        val delayDuration = 30000L
                        delay(delayDuration)
//                    val randomDelay = Random.nextLong(5000, 300000) // Random delay between 5 secs to 5 minutes
//                    delay(randomDelay)
                        for (i in delayDuration downTo 0 step 1000) {
                            Log.d("IntercommunicationBroadcast", "Time until next call: ${i / 1000} seconds")
                            delay(1000)
                        }
                    }
                }
            } else {
                callJob?.cancel()

                // Regular one-time call
                val localIntent = Intent(BroadcastAction.SELEN_VOIP_APP_CALL_HQ_LOCAL)
                localIntent.putExtra("destination_number", destinationNumber)
                LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)

            }

        }

        if (intentAction == BroadcastAction.SELEN_VOIP_APP_CHECK_REGISTRATION) {
            // Send a local broadcast to MainActivity
            Log.d("IntercommunicationBroadcast", " +++///777 Received broadcast from SelenAlarmBox: $message +++ ")
            val localIntent = Intent(BroadcastAction.SELEN_VOIP_APP_CHECK_REGISTRATION_LOCAL)
            LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)
        }


    }
}

//if (currentCall == null) {
//    try {
//        val call = MyCall(account, -1)
//        val prm = CallOpParam(true)
//        call.makeCall(Config.CALL_DST_URI, prm)
//        currentCall = call
//        utils.turnOnLed()
//    } catch (e: Exception) {
//        println(e)
//    }
//} else {
//    try {
//        val prm = CallOpParam()
//        prm.statusCode = pjsip_status_code.PJSIP_SC_DECLINE
//        currentCall!!.hangup(prm)
//        utils.turnOffLed()
//    } catch (e: Exception) {
//        println(e)
//    }
//}