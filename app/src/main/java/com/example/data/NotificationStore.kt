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

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _hasUnread = MutableStateFlow(false)
    val hasUnread: StateFlow<Boolean> = _hasUnread.asStateFlow()

    fun init(context: Context) {
        val prefs = getPrefs(context)
        loadFromPrefs(prefs)
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    private fun loadFromPrefs(prefs: SharedPreferences) {
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
        val prefs = getPrefs(context)
        val currentList = _notifications.value.toMutableList()
        val newNotification = AppNotification(
            id = System.currentTimeMillis().toString(),
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        currentList.add(0, newNotification)
        // Keep max 20 notifications
        val trimmed = currentList.take(20)

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
}
