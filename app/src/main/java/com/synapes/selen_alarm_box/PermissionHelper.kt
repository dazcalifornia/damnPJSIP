package com.synapes.selen_alarm_box

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionsHelper {
    private const val PERMISSIONS_REQUEST_CODE = 123

    // Only include core permissions that are actually needed
    private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.READ_PHONE_STATE
        )
    } else {
        arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE
        )
    }

    private val PERMISSION_DESCRIPTIONS = mapOf(
        Manifest.permission.RECORD_AUDIO to "Microphone access is needed for voice calls",
        Manifest.permission.READ_PHONE_STATE to "Phone state access is needed to handle calls properly",
        Manifest.permission.POST_NOTIFICATIONS to "Notification permission is needed to show call status"
    )

    fun checkAndRequestPermissions(activity: Activity) {
        val permissionsToRequest = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                permissionsToRequest,
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    fun areAllPermissionsGranted(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun handlePermissionResult(
        activity: Activity,
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            val deniedPermissions = permissions.filterIndexed { index, _ ->
                grantResults[index] != PackageManager.PERMISSION_GRANTED
            }

            if (deniedPermissions.isNotEmpty()) {
                showSettingsPrompt(activity, deniedPermissions)
            }
        }
    }

    private fun showSettingsPrompt(activity: Activity, deniedPermissions: List<String>) {
        val message = buildString {
            appendLine("The following permissions are needed but not granted:")
            deniedPermissions.forEach { permission ->
                appendLine("• ${PERMISSION_DESCRIPTIONS[permission] ?: permission}")
            }
            appendLine("\nPlease enable them in Settings to use this app.")
        }

        AlertDialog.Builder(activity)
            .setTitle("Permissions Required")
            .setMessage(message)
            .setPositiveButton("Open Settings") { _, _ ->
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", activity.packageName, null)
                    activity.startActivity(this)
                }
            }
            .setNegativeButton("Exit App") { _, _ -> activity.finish() }
            .setCancelable(false)
            .show()
    }
}