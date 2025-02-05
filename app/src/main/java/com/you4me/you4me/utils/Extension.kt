package com.you4me.you4me.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import  com.you4me.you4me.R

//safe navigate
fun NavController.safeNavigate(direction: NavDirections) {
    currentDestination?.getAction(direction.actionId)?.run { navigate(direction) }
}

fun NavController.safeNavigateUp() {
    currentDestination?.run { navigateUp() }
}

fun NavController.safeNavigate(
    @IdRes currentDestinationId: Int,
    @IdRes id: Int,
    args: Bundle? = null
) {
    if (currentDestinationId == currentDestination?.id) {
        navigate(id, args)
    }
}
//...................................

//For Toast
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG)
        .show()
}

//disable user interaction
fun Activity.disableUserInteraction() {
    this.window.setFlags(
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
    )
}

fun Activity.enableUserInteraction() {
    this.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
}

private var dialog: Dialog? = null

fun Context.showSimpleProgressDialog() {
    try {
        if (dialog == null) {
            dialog = Dialog(this, R.style.FullScreenProgressDialogStyle)
                .apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setContentView(R.layout.dialog_loader)
                setCancelable(false)
            }
        }
        if (dialog?.isShowing == false) {
            dialog?.show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun removeSimpleProgressDialog() {
    try {
        dialog?.let {
            if (it.isShowing) {
                it.dismiss()
                dialog = null
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


