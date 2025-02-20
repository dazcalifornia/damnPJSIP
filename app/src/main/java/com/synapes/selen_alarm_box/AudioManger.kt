package com.synapes.selen_alarm_box

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.audiofx.AcousticEchoCanceler
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import android.Manifest

class AudioSystemManager(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val tag = "AudioSystemManager"

    fun setupAudioSystem() {
        if (!checkAudioPermissions()) return
        setupEchoCancellation()
        setupVolumeAndMode()
        setupSpeakerphone()
    }

    private fun checkAudioPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
            Log.e(tag, "MODIFY_AUDIO_SETTINGS permission not granted")
            return false
        }
        return true
    }

    private fun setupEchoCancellation() {
        val sessionId = audioManager.generateAudioSessionId()
        val aec = AcousticEchoCanceler.create(sessionId)
        if (aec != null && !aec.enabled) {
            aec.enabled = true
            Log.d(tag, "AEC enabled")
        }
    }

    private fun setupVolumeAndMode() {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
        val percentVolume = (maxVolume * 0.81).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, percentVolume, 0)
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
    }

    private fun setupSpeakerphone() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            setupModernSpeakerphone()
        } else {
            setupLegacySpeakerphone()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun setupModernSpeakerphone() {
        val audioDeviceInfo = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
        if (audioDeviceInfo != null) {
            audioManager.setCommunicationDevice(audioDeviceInfo)
            Log.d(tag, "Speakerphone set using setCommunicationDevice")
        }
    }

    @Suppress("DEPRECATION")
    private fun setupLegacySpeakerphone() {
        audioManager.isSpeakerphoneOn = true
        Log.d(tag, "Speakerphone set using deprecated method")
    }
}