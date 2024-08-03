package com.synapes.selen_alarm_box

import android.content.Context
import android.net.Uri
import android.util.Log

object PreferencesManager {
    private const val TAG = "PREFERENCE MANAGER"

    private const val KEY_EXTENSION_NUMBER = "extension_number"
    private const val KEY_DESTINATION_NUMBER = "destination_number"
    private const val DEFAULT_SELF_EXT = "999999999"
    private const val DEFAULT_DESTINATION_EXT = "1000"

    private val CONTENT_URI = Uri.parse("content://com.synapes.android_voip_watchdog.provider/prefs")


    fun getSelfExtension(context: Context): String {
        return getFromSharedPreferences(context, KEY_EXTENSION_NUMBER) ?: DEFAULT_SELF_EXT
    }
//
    fun getDestinationExtension(context: Context): String {
        return getFromSharedPreferences(context, KEY_DESTINATION_NUMBER) ?: DEFAULT_DESTINATION_EXT
    }
//
    private fun getFromSharedPreferences(context: Context, key: String): String? {
        val projection = arrayOf("value")
        val selection = "key = ?"
        val selectionArgs = arrayOf(key)
        try {
            context.contentResolver.query(
                CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndexOrThrow("value"))
                }
            }
        } catch (e: Exception) {
            Log.e("PreferencesManager", "Error getting from SharedPreferences", e)
        }
        return null
    }
//
//    fun saveExtension(context: Context, key: String, value: String) {
//        val values = android.content.ContentValues().apply {
//            put("key", key)
//            put("value", value)
//        }
//        try {
//            context.contentResolver.insert(SharedPrefsProvider.CONTENT_URI, values)
//        } catch (e: Exception) {
//            Log.e("PreferencesManager", "Error saving to SharedPreferences", e)
//        }
//    }
//
//    fun updateExtensions(context: Context, newSelfExt: String, newDestExt: String) {
//        saveExtension(context, KEY_EXTENSION_NUMBER, newSelfExt)
//        saveExtension(context, KEY_DESTINATION_NUMBER, newDestExt)
//    }

    fun dumpAllPreferences(context: Context) {
        Log.d(TAG, "Dumping all SharedPreferences:")
        try {
            context.contentResolver.query(
                CONTENT_URI,
                null, // Projection: null means all columns
                null, // Selection: null means all rows
                null, // Selection args
                null  // Sort order
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    do {
                        val key = cursor.getString(cursor.getColumnIndexOrThrow("key"))
                        val value = cursor.getString(cursor.getColumnIndexOrThrow("value"))
                        Log.d(TAG, "Key: $key, Value: $value")
                    } while (cursor.moveToNext())
                } else {
                    Log.d(TAG, "No preferences found.")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error dumping SharedPreferences", e)
        }
    }
}
