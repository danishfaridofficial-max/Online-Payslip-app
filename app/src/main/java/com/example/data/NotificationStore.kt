package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

object NotificationStore {
    private const val PREF_NAME = "payslip_notifications_pref"
    private const val KEY_NOTIFICATIONS = "saved_notifications"
    private const val KEY_HAS_UNREAD = "has_unread_notification"
    private const val KEY_FCM_TOKEN = "saved_fcm_token"

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _hasUnread = MutableStateFlow(false)
    val hasUnread: StateFlow<Boolean> = _hasUnread.asStateFlow()

    private val _openDialogRequested = MutableStateFlow(false)
    val openDialogRequested: StateFlow<Boolean> = _openDialogRequested.asStateFlow()

    fun requestOpenDialog() {
        _openDialogRequested.value = true
    }

    fun consumeOpenDialogRequest(): Boolean {
        val req = _openDialogRequested.value
        if (req) _openDialogRequested.value = false
        return req
    }

    private val _fcmToken = MutableStateFlow<String?>(null)
    val fcmToken: StateFlow<String?> = _fcmToken.asStateFlow()

    fun init(context: Context) {
        val prefs = getPrefs(context)
        loadFromPrefs(prefs)
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    private fun loadFromPrefs(prefs: SharedPreferences) {
        _fcmToken.value = prefs.getString(KEY_FCM_TOKEN, null)
        val jsonStr = prefs.getString(KEY_NOTIFICATIONS, null)
        val list = mutableListOf<AppNotification>()
        if (!jsonStr.isNullOrBlank()) {
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        AppNotification(
                            id = obj.optString("id", "${System.currentTimeMillis()}_$i"),
                            title = obj.optString("title", ""),
                            message = obj.optString("message", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            isRead = obj.optBoolean("isRead", false)
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _notifications.value = list
        _hasUnread.value = prefs.getBoolean(KEY_HAS_UNREAD, list.any { !it.isRead })
    }

    fun addNotification(context: Context, title: String, message: String) {
        if (title.isBlank() && message.isBlank()) return
        val currentList = _notifications.value.toMutableList()

        // Deduplicate if identical title and message arrived within 4 seconds
        if (currentList.isNotEmpty()) {
            val first = currentList[0]
            if (first.title == title && first.message == message && (System.currentTimeMillis() - first.timestamp < 4000L)) {
                return
            }
        }

        val prefs = getPrefs(context)
        val newNotification = AppNotification(
            id = System.currentTimeMillis().toString(),
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        currentList.add(0, newNotification)
        // Keep up to 30 notifications
        val trimmed = currentList.take(30)

        // Save to prefs
        val array = JSONArray()
        for (item in trimmed) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("message", item.message)
                put("timestamp", item.timestamp)
                put("isRead", item.isRead)
            }
            array.put(obj)
        }

        prefs.edit()
            .putString(KEY_NOTIFICATIONS, array.toString())
            .putBoolean(KEY_HAS_UNREAD, true)
            .apply()

        _notifications.value = trimmed
        _hasUnread.value = true
    }

    fun markAllAsRead(context: Context) {
        val prefs = getPrefs(context)
        val updated = _notifications.value.map { it.copy(isRead = true) }
        val array = JSONArray()
        for (item in updated) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("message", item.message)
                put("timestamp", item.timestamp)
                put("isRead", true)
            }
            array.put(obj)
        }

        prefs.edit()
            .putString(KEY_NOTIFICATIONS, array.toString())
            .putBoolean(KEY_HAS_UNREAD, false)
            .apply()

        _notifications.value = updated
        _hasUnread.value = false
    }

    fun clearAll(context: Context) {
        getPrefs(context).edit().clear().apply()
        _notifications.value = emptyList()
        _hasUnread.value = false
    }

    fun saveFcmToken(context: Context, token: String) {
        if (token.isBlank()) return
        _fcmToken.value = token
        getPrefs(context).edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    fun getSavedFcmToken(context: Context): String? {
        return _fcmToken.value ?: getPrefs(context).getString(KEY_FCM_TOKEN, null)
    }

    fun ensureFirebaseInitialized(context: Context): com.google.firebase.FirebaseApp? {
        return try {
            val apps = com.google.firebase.FirebaseApp.getApps(context)
            if (apps.isNotEmpty()) {
                com.google.firebase.FirebaseApp.getInstance()
            } else {
                val auto = com.google.firebase.FirebaseApp.initializeApp(context)
                auto ?: run {
                    val options = com.google.firebase.FirebaseOptions.Builder()
                        .setApplicationId("1:964621085853:android:7e752337f570980d62f874")
                        .setApiKey("AIzaSyBPNkc-Beh7TE3AkrH3FW0vfJPfgGwDldU")
                        .setGcmSenderId("964621085853")
                        .setProjectId("online-paylisp")
                        .setStorageBucket("online-paylisp.firebasestorage.app")
                        .build()
                    com.google.firebase.FirebaseApp.initializeApp(context.applicationContext, options)
                }
            }
        } catch (e: Throwable) {
            android.util.Log.e("NotificationStore", "ensureFirebaseInitialized auto failed, forcing options", e)
            try {
                val options = com.google.firebase.FirebaseOptions.Builder()
                    .setApplicationId("1:964621085853:android:7e752337f570980d62f874")
                    .setApiKey("AIzaSyBPNkc-Beh7TE3AkrH3FW0vfJPfgGwDldU")
                    .setGcmSenderId("964621085853")
                    .setProjectId("online-paylisp")
                    .setStorageBucket("online-paylisp.firebasestorage.app")
                    .build()
                com.google.firebase.FirebaseApp.initializeApp(context.applicationContext, options)
            } catch (e2: Throwable) {
                android.util.Log.e("NotificationStore", "Fallback initializeApp failed", e2)
                null
            }
        }
    }

    fun fetchFcmToken(context: Context, onComplete: ((String?, String?) -> Unit)? = null) {
        try {
            ensureFirebaseInitialized(context)
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && !task.result.isNullOrBlank()) {
                        val token = task.result
                        saveFcmToken(context, token)
                        onComplete?.invoke(token, null)
                    } else {
                        val err = task.exception?.localizedMessage ?: "ٹوکن موصول نہیں ہوا"
                        android.util.Log.e("NotificationStore", "Failed to retrieve FCM token: $err", task.exception)
                        onComplete?.invoke(null, err)
                    }
                }
        } catch (e: Throwable) {
            val err = e.localizedMessage ?: "Firebase error"
            android.util.Log.e("NotificationStore", "Exception getting FCM token: $err", e)
            onComplete?.invoke(null, err)
        }
    }
}
