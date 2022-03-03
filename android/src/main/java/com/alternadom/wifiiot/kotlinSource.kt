package com.alternadom.wifiiot

import android.content.ContentValues.TAG
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log

class kotlinSource {
    val connectivityManager by lazy {
        MyApplication.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private val wifiNetworkCallback = object : ConnectivityManager.NetworkCallback() {
        // Called when the framework connects and has declared a new network ready for use.
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            listener?.onWifiConnected(network)
        }
        // Called when a network disconnects or otherwise no longer satisfies this request or callback.
        override fun onLost(network: Network) {
            super.onLost(network)
            listener?.onWifiDisconnected()
        }
    }

    private val mobileNetworkCallback = object : ConnectivityManager.NetworkCallback() {
        // Called when the framework connects and has declared a new network ready for use.
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            connectivityManager.bindProcessToNetwork(network)
            listener?.onMobileConnected(network)
        }

        // Called when a network disconnects or otherwise no longer satisfies this request or callback.
        override fun onLost(network: Network) {
            super.onLost(network)
            connectivityManager.bindProcessToNetwork(null)
            listener?.onMobileDisconnected()
        }
    }

    private fun setUpWifiNetworkCallback() {

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        try {
            connectivityManager.unregisterNetworkCallback(wifiNetworkCallback)
        } catch (e: Exception) {
            Log.d(TAG, "WiFi Network Callback was not registered or already unregistered")
        }

        connectivityManager.requestNetwork(request, wifiNetworkCallback)
    }

    private fun setUpMobileNetworkCallback() {

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        try {
            connectivityManager.unregisterNetworkCallback(mobileNetworkCallback)
        } catch (e: Exception) {
            Log.d(TAG, "Mobile Data Network Callback was not registered or already unregistered")
        }

        connectivityManager.requestNetwork(request, mobileNetworkCallback)
    }
}