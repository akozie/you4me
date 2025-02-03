package com.you4me.you4me.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.Log
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.HashMap
import java.util.Locale

object Utils {
    const val ADD_EVENT_REQUEST_CODE = 1001 // Any unique request code
    const val GOOGLE_SIGN_IN_RQ_CODE = 100
    const val TAG_TOUCH_START_X = 1000
    const val BANNER_TIMEOUT = 5000L

    fun getDateFormat() = SimpleDateFormat("yyyy/MM/dd", Locale.UK)

    fun isInternetConnected(): Boolean {
        return try {
            val command = "ping -c 1 google.com"
            Runtime.getRuntime().exec(command).waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    fun formatDate(
        dateString: String,
        timeString: String,
    ): String {
        // Combine date and time strings
        val combinedString = "$dateString $timeString"

        // Define the input format
        val inputFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

        // Define the output format
        val outputFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy hh:mm a", Locale.getDefault())

        // Parse the combined string into a Date object
        val date = inputFormat.parse(combinedString)

        // Format the Date object into the desired format
        Log.d("CHECKING", outputFormat.format(date!!))
        return outputFormat.format(date!!)
    }

    fun String.toSentenceCase(): String {
        return this.lowercase().replaceFirstChar { it.uppercase() }
    }

    fun showAlertDialog(
        context: Context,
        message: String,
        positiveButtonTitle: String,
        negativeButtonTitle: String,
        onPositiveButtonClick: () -> Unit,
        onNegativeButtonClick: () -> Unit,
    ) {
        val alertDialog =
            android.app.AlertDialog.Builder(context).setMessage(message)
                .setPositiveButton(positiveButtonTitle) { _, _ ->
                    onPositiveButtonClick()
                    // Set action here
                }
                .setNegativeButton(negativeButtonTitle) { _, _ ->
                    onNegativeButtonClick()
                    // set action here
                }.setCancelable(false).create()

        alertDialog.show()
    }

    fun getCategoryFromString(fileUrl: String): String {
        val mimeTypeMap = MimeTypeMap.getSingleton()
        val extension = fileUrl.substringAfterLast('.', "").lowercase()

        val mimeType = mimeTypeMap.getMimeTypeFromExtension(extension)

        return when {
            mimeType?.startsWith("image") == true -> "image"
            mimeType?.startsWith("video") == true -> "video"
            else -> "unknown"
        }
    }

    suspend fun generateVideoThumbnail(videoUrl: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            return@withContext try {
                retriever.setDataSource(videoUrl, HashMap()) // Use secureUrl here
                val bitmap = retriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                retriever.release()
                bitmap
            } catch (e: Exception) {
                e.printStackTrace()
                retriever.release()
                null
            }
        }
    }
}
