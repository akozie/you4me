package com.siddigital.enairaofflineapp.utils

import android.app.Activity
import android.app.Dialog
import android.util.Patterns
import android.widget.TextView
import com.you4me.you4me.R
import java.text.SimpleDateFormat
import java.util.Locale

object Utils {

    fun getDateFormat() = SimpleDateFormat("yyyy/MM/dd", Locale.UK)
    fun isInternetConnected() : Boolean {
        return try {
            val command = "ping -c 1 google.com"
            Runtime.getRuntime().exec(command).waitFor() == 0
        } catch (e: Exception) {
            false
        }
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