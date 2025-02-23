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

    fun toggleMute(isMuted: Boolean) {
        try {
            audioManager.isMicrophoneMute = isMuted
            Log.d(tag, "Microphone muted: $isMuted")
        } catch (e: Exception) {
            Log.e(tag, "Error toggling mute: $e")
        }
    }



    fun toggleSpeaker(isSpeakerOn: Boolean) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // For Android 12 and above
                val audioDeviceInfo = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                    .firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }

                if (audioDeviceInfo != null) {
                    audioManager.setCommunicationDevice(audioDeviceInfo)
                }
            } else {
                // For older Android versions
                @Suppress("DEPRECATION")
                audioManager.isSpeakerphoneOn = isSpeakerOn
            }

            audioManager.mode = if (isSpeakerOn) {
                AudioManager.MODE_IN_COMMUNICATION
            } else {
                AudioManager.MODE_NORMAL
            }
            Log.d(tag, "Speaker enabled: $isSpeakerOn")
        } catch (e: Exception) {
            Log.e(tag, "Error toggling speaker: $e")
        }
    }
}