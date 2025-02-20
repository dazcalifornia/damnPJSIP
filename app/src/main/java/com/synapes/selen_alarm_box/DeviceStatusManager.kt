package com.synapes.selen_alarm_box

import android.content.Context
import android.util.Log

class DeviceStatusManager(
    private val context: Context,
    private val utils: Utils,
    private val networkManager: NetworkManager,
    private val onAirplaneMode: () -> Unit,
    private val onNoVPN: () -> Unit,
    private val onNoInternet: () -> Unit,
    private val onNormalState: () -> Unit
) {
    private val tag = "DeviceStatusManager"

    fun checkDeviceStatus(): Boolean {
        val airplaneModeStatus = utils.isAirplaneModeOn(context)
        val vpnStatus = utils.isVpnActive(context)
        val internetStatus = networkManager.internetStatus

        Log.d(tag, "Status Check - Airplane: $airplaneModeStatus, VPN: $vpnStatus, Internet: $internetStatus")

        when {
            airplaneModeStatus -> {
                Log.d(tag, "Device check failed: Airplane mode ON")
                onAirplaneMode()
                return false
            }
            !vpnStatus -> {
                Log.d(tag, "Device check failed: No VPN")
                onNoVPN()
                return false
            }
            !internetStatus -> {
                Log.d(tag, "Device check failed: No internet")
                onNoInternet()
                return false
            }
            else -> {
                Log.d(tag, "All device checks passed")
                onNormalState()
                return true
            }
        }
    }
}