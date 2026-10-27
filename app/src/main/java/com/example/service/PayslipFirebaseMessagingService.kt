package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PayslipFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Automatically subscribe this device to the "all_users" topic
        try {
            FirebaseMessaging.getInstance().subscribeToTopic("all_users")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "پے سلپ پورٹل اطلاع"
        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: remoteMessage.data["message"]
            ?: "آپ کے لیے نئی اپڈیٹ موجود ہے۔"

        showNotification(title, body)
        try {
            com.example.data.NotificationStore.addNotification(applicationContext, title, body)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "payslip_portal_notifications"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "تنخواہ اور نوٹیفیکیشنز (Payslip Updates)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "خیبر پختونخوا پے سلپس اور سرکاری اطلاعات"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFF0D9488.toInt())
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        fun initializeSubscriptions() {
            try {
                FirebaseMessaging.getInstance().subscribeToTopic("all_users")
                FirebaseMessaging.getInstance().subscribeToTopic("news_updates")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
