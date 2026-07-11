package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // Extract message content
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Arena AI Update"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "Vedi le ultime novità su Chatbot Arena!"
        val url = remoteMessage.data["url"] ?: remoteMessage.data["link"]

        Log.d(TAG, "Message received: Title=$title, Body=$body, URL=$url")
        
        sendNotification(this, title, body, url)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New Firebase Token: $token")
        // In a real production app, this token would be sent to your backend server
    }

    companion object {
        private const val TAG = "ArenaFCMService"
        const val CHANNEL_ID = "arena_push_notifications"
        private const val CHANNEL_NAME = "Aggiornamenti Arena AI"
        private const val CHANNEL_DESC = "Notifiche push per novità, nuovi modelli e classifiche di Arena.ai"
        const val EXTRA_URL = "target_url"

        /**
         * Dispatches a native notification. Accessible globally for local demo notifications.
         */
        fun sendNotification(context: Context, title: String, messageBody: String, url: String? = null) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create Notification Channel for Android Oreo (8.0) and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = CHANNEL_DESC
                    enableLights(true)
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Intent to launch MainActivity when notification is tapped
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                if (url != null) {
                    putExtra(EXTRA_URL, url)
                }
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // System fallback icon
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            // Trigger notification
            notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        }
    }
}
