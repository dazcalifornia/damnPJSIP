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
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
//        serviceJob.cancel()
        Log.d(TAG, " --- BACKGROUND SERVICE DESTROYED...")
    }
}