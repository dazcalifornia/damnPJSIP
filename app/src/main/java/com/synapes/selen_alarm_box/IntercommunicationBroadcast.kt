package com.synapes.selen_alarm_box

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.synapes.selen_alarm_box.MainActivity.Companion.account
import com.synapes.selen_alarm_box.MainActivity.Companion.currentCall
import org.pjsip.pjsua2.CallOpParam
import org.pjsip.pjsua2.pjsip_status_code

class IntercommunicationBroadcast: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val intentAction = intent.action
        val message = intent.getStringExtra("message")
        Log.d("IntercommunicationBroadcast", "Action: $intentAction, Message: $message")

        if (intentAction == BroadcastAction.SELEN_VOIP_APP_CALL_HQ) {
            val deviceId = intent.getStringExtra("device_id")
            val destinationNumber = intent.getStringExtra("destination_number")
            Log.d("IntercommunicationBroadcast", "Received broadcast from SelenAlarmBox: $message")
            Log.d("IntercommunicationBroadcast", "device_id: $deviceId, destination_number: $destinationNumber")

            // Call the MainActivity's handleButtonState method
//            val mainActivity = context as MainActivity
//            mainActivity.handleButtonState(0, destinationNumber)
//            mainActivity.handleButtonState(0)
            // Send a local broadcast to MainActivity
            val localIntent = Intent(BroadcastAction.SELEN_VOIP_APP_CALL_HQ_LOCAL)
            localIntent.putExtra("destination_number", destinationNumber)
            LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)
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