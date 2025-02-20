package com.synapes.selen_alarm_box

import android.content.Context
import android.net.Uri
import android.util.Log

object PreferencesManager {
    private const val TAG = "PREFERENCE MANAGER"
    private const val PREFS_NAME = "selen_voip_prefs"

    private const val KEY_EXTENSION_NUMBER = "extension_number"
    private const val KEY_DESTINATION_NUMBER = "destination_number"
    private const val DEFAULT_SELF_EXT = "999999999"
    private const val DEFAULT_DESTINATION_EXT = "1000"

    fun getSelfExtension(context: Context): String {
        return getFromSharedPreferences(context, KEY_EXTENSION_NUMBER, DEFAULT_SELF_EXT)
    }

    fun getDestinationExtension(context: Context): String {
        return getFromSharedPreferences(context, KEY_DESTINATION_NUMBER, DEFAULT_DESTINATION_EXT)
    }

    // Optional: Add setters if you want to change values programmatically
    fun setSelfExtension(context: Context, value: String) {
        setInSharedPreferences(context, KEY_EXTENSION_NUMBER, value)
    }

    fun setDestinationExtension(context: Context, value: String) {
        setInSharedPreferences(context, KEY_DESTINATION_NUMBER, value)
    }

    private fun getFromSharedPreferences(context: Context, key: String, defaultValue: String): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    private fun setInSharedPreferences(context: Context, key: String, value: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(key, value).apply()
    }

    fun dumpAllPreferences(context: Context) {
        Log.d(TAG, "Dumping all SharedPreferences:")
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        for ((key, value) in prefs.all) {
            Log.d(TAG, "Key: $key, Value: $value")
        }
    }
}