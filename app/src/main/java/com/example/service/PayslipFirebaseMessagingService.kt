package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.NotificationStore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PayslipFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM Token: $token")
        subscribeAllTopics()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}, data: ${remoteMessage.data}")

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: remoteMessage.data["header"]
            ?: "پے سلپ پورٹل اطلاع"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: remoteMessage.data["message"]
            ?: remoteMessage.data["text"]
            ?: "آپ کے لیے نئی اپڈیٹ موجود ہے۔"

        showNotification(title, body)

        try {
            NotificationStore.addNotification(applicationContext, title, body)
        } catch (e: Exception) {
            Log.e(TAG, "Error storing notification", e)
        }
    }

    private fun showNotification(title: String, message: String) {
        createNotificationChannel(applicationContext)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("notification_title", title)
            putExtra("notification_body", message)
            putExtra("from_fcm", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFF0D9488.toInt())
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        const val TAG = "PayslipFCM"
        const val CHANNEL_ID = "payslip_portal_notifications"
        private val TOPICS = listOf("all_users", "all", "payslip_updates", "news_updates", "general")

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val existing = notificationManager.getNotificationChannel(CHANNEL_ID)
                if (existing == null) {
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        "تنخواہ اور نوٹیفیکیشنز (Payslip Updates)",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "خیبر پختونخوا پے سلپس اور سرکاری اطلاعات"
                        enableLights(true)
                        lightColor = 0xFF0D9488.toInt()
                        enableVibration(true)
                        vibrationPattern = longArrayOf(0, 300, 200, 300)
                        setShowBadge(true)
                    }
                    notificationManager.createNotificationChannel(channel)
                }
            }
        }

        fun subscribeAllTopics() {
            try {
                val messaging = FirebaseMessaging.getInstance()
                for (topic in TOPICS) {
                    messaging.subscribeToTopic(topic).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Subscribed to topic: $topic")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed subscribing to topics", e)
            }
        }

        fun initializeSubscriptions(context: Context) {
            createNotificationChannel(context)
            subscribeAllTopics()

            try {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Active FCM Token: ${task.result}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed getting FCM token", e)
            }
        }
    }
}
