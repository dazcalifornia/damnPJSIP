package com.synapes.selen_alarm_box

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.LifecycleOwner

class ServiceManager(
    private val context: Context,
    private val onServiceMessage: (String) -> Unit
) {
    private val tag = "ServiceManager"

    fun startServices() {
        startSelenForegroundService()
        setupRunMode()
    }

    private fun startSelenForegroundService() {
        Log.d(tag, "Starting SelenForegroundService")
        val foregroundIntent = Intent(context, SelenForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(foregroundIntent)
        } else {
            context.startService(foregroundIntent)
        }

        // Observe service status
        SelenForegroundService.libraryStartedLiveData.observe(context as LifecycleOwner) { message ->
            Log.d(tag, "Foreground Service: $message")
            onServiceMessage(message)
        }
    }

    private fun setupRunMode() {
        when (Config.APP_RUN_MODE) {
            RunMode.UI -> {
                Log.d(tag, "Setting up UI mode")
                configureButtons(RunMode.UI)
            }
            RunMode.BACKGROUND -> {
                Log.d(tag, "Setting up Background mode")
                setupBackgroundMode()
            }
            RunMode.FOREGROUND -> {
                Log.d(tag, "Setting up Foreground mode")
                setupForegroundMode()
            }
            else -> {
                Log.e(tag, "Invalid run mode")
            }
        }
    }

    private fun configureButtons(mode: String) {
        when (mode) {
            RunMode.UI -> {
                // UI mode configuration
            }
            RunMode.BACKGROUND, RunMode.FOREGROUND -> {
                // Disable buttons for background/foreground modes
            }
        }
    }

    private fun setupBackgroundMode() {
        configureButtons(RunMode.BACKGROUND)
        val backgroundIntent = Intent(context, SelenBackgroundService::class.java)
        context.startService(backgroundIntent)

        SelenBackgroundService.libraryStartedLiveData.observe(context as LifecycleOwner) { message ->
            onServiceMessage(message)
        }
    }

    private fun setupForegroundMode() {
        configureButtons(RunMode.FOREGROUND)
        val foregroundIntent = Intent(context, SelenForegroundService::class.java)
        context.startService(foregroundIntent)

        SelenForegroundService.libraryStartedLiveData.observe(context as LifecycleOwner) { message ->
            onServiceMessage(message)
        }
    }
}