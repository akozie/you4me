package com.you4me.you4me.service

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.you4me.you4me.R
import com.you4me.you4me.models.User
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.RemoteDataSource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.main.MainActivity
import com.you4me.you4me.utils.SharedPrefHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sendToken(token)
        Log.d("TOKEN_PROFI", "YEAH")

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle FCM messages here
        if (remoteMessage.data.isNotEmpty()){
            handleNotificationMessage(remoteMessage)
            Log.d("TOKEN_PRO", "userProfile")
        }

    }

    private fun sendToken(token: String) {
        val sharedPref = SharedPrefHelper(applicationContext)
        val userProfile = sharedPref.getString(SharedPrefHelper.USER_PROFILE)
        if (userProfile.isNotEmpty()){
        Log.d("TOKEN_PROFILEID", userProfile)
        val gson = Gson()
        val user = gson.fromJson(userProfile, User::class.java)
        updateToken(user.userId, token)
        }else{
            //do nothing
        }
    }

    private fun handleNotificationMessage(remoteMessage: RemoteMessage) {
        val incomingMessage = remoteMessage.data["MessageBody"]
        incomingMessage?.let {
            //You need to convert the string to your notification object

//            showNotification(
//                remoteMessage.notification?.title.toString(),
//                remoteMessage.notification?.body.toString()
//            )

        }
    }

    private fun showNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, "YOU_4_ME_CHANNEL_ID")
            .setSmallIcon(R.drawable.icon)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {

            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val id = SystemClock.uptimeMillis().toInt();
                notify(id, builder.build())
            }
        }
    }

    private fun updateToken(userId: String, token: String) {
        val obj = JsonObject()
        obj.addProperty("pushToken", token)
        val repo = MainRepository(RemoteDataSource().buildApi(ApiCollector::class.java))
        scope.launch { repo.updatePushToken(userId, obj) }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}