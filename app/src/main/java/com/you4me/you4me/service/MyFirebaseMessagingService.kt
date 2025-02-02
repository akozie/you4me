package com.you4me.you4me.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.annotation.RequiresApi
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
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sendToken(token)
        Log.d("TOKEN_PROFI", "YEAH")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels()
        }

        getRemoteNotification(remoteMessage)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupChannels() {
        val adminChannelName: CharSequence = "You4me"
        val adminChannelDescription = "New notification"
        val adminChannel = NotificationChannel(
            "123", adminChannelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE)
                as NotificationManager
        notificationManager.createNotificationChannel(adminChannel)
    }

    private fun getRemoteNotification(remoteMessage: RemoteMessage) {
        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            handleNotification(remoteMessage)
        } else {
            // Check if message contains a data payload.
            if (remoteMessage.data.isNotEmpty()) {
                Log.e(
                    "You4meNotification", "Data Payload: " +
                            remoteMessage.data.toString()
                )
                try {
                    handleNotificationMessage(remoteMessage)
                } catch (e: Exception) {
                    Log.e("You4meNotification", "Exception: " + e.message)
                }
            }
        }
    }

    private fun handleNotification(remoteMessage: RemoteMessage) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                this,
                0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getActivity(
                this,
                0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        val random = Random()
        val m = random.nextInt(9999 - 1000) + 1000
        val channelId = "123"
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.icon)
                .setColor(resources.getColor(R.color.white, null))
                .setContentTitle(
                    remoteMessage.notification?.title
                )
                .setContentText(
                    remoteMessage.notification?.body
                )
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(
                        remoteMessage.notification?.body
                    )
                )
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        resources,
                        R.drawable.icon
                    )
                )
                .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(m /* ID of notification */, notificationBuilder.build())
    }


    private fun sendToken(token: String) {
        val sharedPref = SharedPrefHelper(applicationContext)
        val userProfile = sharedPref.getString(SharedPrefHelper.USER_PROFILE)
        if (userProfile.isNotEmpty()) {
            Log.d("TOKEN_PROFILEID", userProfile)
            val gson = Gson()
            val user = gson.fromJson(userProfile, User::class.java)
            updateToken(user.userId, token)
        } else {
            //do nothing
        }
    }

    private fun handleNotificationMessage(remoteMessage: RemoteMessage) {
        val incomingMessage = remoteMessage.data["MessageBody"]
        incomingMessage?.let {
            //You need to convert the string to your notification object

            showNotification(
                remoteMessage.notification?.title.toString(),
                remoteMessage.notification?.body.toString()
            )

        }
    }

    private fun showNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                applicationContext, 0, intent,
                PendingIntent.FLAG_IMMUTABLE
            )

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
        val repo = MainRepository(
            RemoteDataSource().buildApi(
                ApiCollector::class.java
            )
        )
        scope.launch { repo.updatePushToken(userId, obj) }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}