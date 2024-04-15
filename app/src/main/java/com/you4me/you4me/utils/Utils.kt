package com.you4me.you4me.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.util.Patterns
import android.widget.TextView
import com.you4me.you4me.R
import java.text.SimpleDateFormat
import java.util.Locale

object Utils {

    const val ADD_EVENT_REQUEST_CODE = 1001 // Any unique request code
    const val GOOGLE_SIGN_IN_RQ_CODE = 100
    const val TAG_TOUCH_START_X = 1000

    fun getDateFormat() = SimpleDateFormat("yyyy/MM/dd", Locale.UK)
    fun isInternetConnected() : Boolean {
        return try {
            val command = "ping -c 1 google.com"
            Runtime.getRuntime().exec(command).waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    fun formatDate(dateString: String, timeString: String): String {
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

    fun showAlertDialog(
        context: Context,
        message: String,
        positiveButtonTitle: String,
        negativeButtonTitle: String,
        onPositiveButtonClick: () -> Unit,
        onNegativeButtonClick: () -> Unit,
    ) {
        val alertDialog = android.app.AlertDialog.Builder(context).setMessage(message)
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

//    fun showErrorDialog(activity: Activity, message: String, resolve: String? = null) {
//        val view = activity.layoutInflater.inflate(R.layout.error_dialog, null)
//        view.findViewById<TextView>(R.id.title).text = activity.getString(R.string.error)
//        view.findViewById<TextView>(R.id.message).text = message
//        if (resolve != null) view.findViewById<TextView>(R.id.resolve).text = resolve
//
//        val dialog = Dialog(activity)
//        dialog.setContentView(view)
//        dialog.show()
//    }
}