package com.synapes.selen_alarm_box

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import org.pjsip.pjsua2.*


class SelenForegroundService : Service() {
    companion object {
        internal val TAG = SelenForegroundService::class.java.simpleName
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "selenvoip_channel_101"
        const val CHANNEL_NAME = "selenvoip_channel_notification"
        var libraryStartedLiveData = MutableLiveData<String>()
    }

    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not yet implemented")
    }

    private fun buildNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntentFlags: Int =
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        val pendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, pendingIntentFlags)

        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SELEN VOIP Foreground Service")
            .setContentText(" ... SELEN VOIP service is running ...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chanel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            chanel.description = CHANNEL_NAME
            notificationBuilder.setChannelId(CHANNEL_ID)
            mNotificationManager.createNotificationChannel(chanel)
        }
        return notificationBuilder.build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        Log.d(TAG, " --- FOREGROUND SERVICE STARTED...")
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)

        libraryStartedLiveData.postValue("Foreground service started")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Restart the service if it gets destroyed
        val broadcastIntent = Intent(this, RestartBroadcastReceiver::class.java)
        sendBroadcast(broadcastIntent)
    }
}


