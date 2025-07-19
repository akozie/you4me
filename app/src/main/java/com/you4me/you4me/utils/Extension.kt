package com.you4me.you4me.utils

import android.app.Activity
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import  com.you4me.you4me.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

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

fun closeSoftKeyboard(
    context: Context,
    activity: Activity,
) {
    activity.currentFocus?.let { view ->
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun Fragment.hideKeyboard() {
    val view = view?.findFocus() ?: view
    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view?.windowToken, 0)
}

 fun validateMedia(uri: Uri, ctx: Context): Boolean {
    val contentResolver = ctx.contentResolver
    val mimeType = contentResolver.getType(uri)

    return if (mimeType?.startsWith("video/") == true) {
        // Check video duration
        val mediaPlayer = MediaPlayer.create(ctx, uri)
        val duration = mediaPlayer?.duration?.toLong() ?: 0
        mediaPlayer?.release()
        duration <= 30000 // Validate that video duration is <= 30 seconds
    } else if (mimeType?.startsWith("image/") == true) {
        // Example validation for images (optional, can customize based on your requirements)
        true // Allow all images
    } else {
        false // Unsupported type
    }
}

fun getCategoryFromUri(
    context: Context,
    fileUri: Uri,
): String {
    val contentResolver: ContentResolver = context.contentResolver
    val mimeType = contentResolver.getType(fileUri) // Get the MIME type of the file

    return if (mimeType?.startsWith("image") == true) {
        "image"
    } else if (mimeType?.startsWith("video") == true) {
        "video"
    } else {
        "unknown" // Fallback if it's neither image nor video
    }
}

fun getReadableDate(dateString: String): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val inputDate = sdf.parse(dateString) ?: return dateString

    val calendarInput = Calendar.getInstance().apply { time = inputDate }
    val calendarToday = Calendar.getInstance()

    return when {
        isSameDay(calendarInput, calendarToday) -> "Today"
        isYesterday(calendarInput, calendarToday) -> "Yesterday"
        else -> {
            // Return formatted like "12–05–2025"
            SimpleDateFormat("dd–MM–yyyy", Locale.getDefault()).format(inputDate)
        }
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isYesterday(cal1: Calendar, cal2: Calendar): Boolean {
    cal2.add(Calendar.DAY_OF_YEAR, -1)
    val isYesterday = isSameDay(cal1, cal2)
    cal2.add(Calendar.DAY_OF_YEAR, 1) // Reset
    return isYesterday
}

 fun savePdf(content: TextView, activity: Activity, context: Context, pdfName: String) {
    val pdfDoc = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDoc.startPage(pageInfo)
    val canvas: Canvas = page.canvas

    val paint = Paint()
    paint.textSize = 12f

    val lines = content.text.split("\n")
    var y = 50

    for (line in lines) {
        canvas.drawText(line, 40f, y.toFloat(), paint)
        y += 20
        if (y > 800) break // Limit to 1 page (optional: add multi-page logic)
    }

    pdfDoc.finishPage(page)

    val date = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "$pdfName$date.pdf"
    val downloadsFolder =
        activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: activity.filesDir
    val file = File(downloadsFolder, fileName)

    try {
        pdfDoc.writeTo(FileOutputStream(file))
        Toast.makeText(context, "PDF saved to ${file.absolutePath}", Toast.LENGTH_LONG)
            .show()
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to save PDF: ${e.message}", Toast.LENGTH_LONG)
            .show()
    }

    pdfDoc.close()
}




