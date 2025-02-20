package com.synapes.selen_alarm_box

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log

class NetworkManager(
    private val context: Context,
    private val onNetworkAvailable: () -> Unit,
    private val onNetworkLost: () -> Unit
) {
    private val tag = "NetworkManager"
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    var internetStatus: Boolean = false
        private set

    fun setupNetworkMonitoring() {
        setupNetworkCallback()
        registerNetworkCallback()
        internetStatus = isNetworkAvailable()
        Log.d(tag, "Initial network status: $internetStatus")
    }

    private fun setupNetworkCallback() {
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(tag, "Internet available")
                internetStatus = true
                onNetworkAvailable()
            }

            override fun onLost(network: Network) {
                Log.d(tag, "No internet connection")
                internetStatus = false
                onNetworkLost()
            }
        }
    }

    private fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()

        networkCallback?.let {
            connectivityManager.registerNetworkCallback(networkRequest, it)
        }
    }

    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    fun cleanup() {
        networkCallback?.let {
            connectivityManager.unregisterNetworkCallback(it)
        }
    }
}