package com.synapes.selen_alarm_box

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

interface Informer {
    fun finished(output: String?)
    fun setPercent(percent: String?)
}

class UpdateApp(private val context: Context, private val informer: Informer) {

    suspend fun updateApp(urlString: String) = coroutineScope {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "GET"
                connection.connect()
                val fileLength = connection.contentLength

                val path = "${Environment.getExternalStorageDirectory()}/download/"
                val file = File(path).apply { mkdirs() }

                val outputFile = File(file, "app.apk").apply {
                    if (exists()) delete()
                }

                connection.inputStream.use { input ->
                    FileOutputStream(outputFile).use { output ->
                        val buffer = ByteArray(1024)
                        var total: Long = 0
                        var len: Int
                        while (input.read(buffer).also { len = it } != -1) {
                            output.write(buffer, 0, len)
                            total += len.toLong()
                            val downloadPercentage =
                                if (fileLength > 0) (total * 100 / fileLength).toInt() else 0
                            informer.setPercent("$downloadPercentage%")
                        }
                    }
                }

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.fromFile(File("$path/app.apk"))
                    type = "application/vnd.android.package-archive"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Update Error: ${e.message}")
            }
        }
    }

    companion object {
        private const val TAG = "UpdateApp"
    }
}