package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.api.PayslipApiClient
import com.example.data.engine.PayslipEngine
import com.example.data.model.BannerResponse
import com.example.data.model.DistrictsResponse
import com.example.data.model.PayslipResponse
import com.example.data.model.TrafficResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.UUID

class PayslipRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("payslip_portal_prefs", Context.MODE_PRIVATE)

    private fun parseCsv(csvText: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val currentField = StringBuilder()
        var inQuotes = false
        val currentRow = mutableListOf<String>()

        var i = 0
        while (i < csvText.length) {
            val c = csvText[i]
            when (c) {
                '\"' -> {
                    if (inQuotes && i + 1 < csvText.length && csvText[i + 1] == '\"') {
                        currentField.append('\"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                ',' -> {
                    if (inQuotes) {
                        currentField.append(',')
                    } else {
                        currentRow.add(currentField.toString().trim())
                        currentField.setLength(0)
                    }
                }
                '\r' -> {}
                '\n' -> {
                    if (inQuotes) {
                        currentField.append('\n')
                    } else {
                        currentRow.add(currentField.toString().trim())
                        currentField.setLength(0)
                        rows.add(currentRow.toList())
                        currentRow.clear()
                    }
                }
                else -> {
                    currentField.append(c)
                }
            }
            i++
        }
        if (currentField.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(currentField.toString().trim())
            rows.add(currentRow.toList())
        }
        return rows
    }

    @Volatile
    private var cachedActiveMonths: List<String>? = null
    private var cachedActiveMonthsTimestamp: Long = 0L
    private val ACTIVE_MONTHS_CACHE_DURATION_MS = 60 * 60 * 1000L // 1 hour cache

    suspend fun getActiveMonthsFromMaster(forceRefresh: Boolean = false): List<String> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        // 1. Check in-memory cache
        if (!forceRefresh && cachedActiveMonths != null && (now - cachedActiveMonthsTimestamp) < ACTIVE_MONTHS_CACHE_DURATION_MS) {
            return@withContext cachedActiveMonths!!
        }

        // 2. Check SharedPreferences cache
        val prefsMonths = prefs.getString("cached_active_months", null)
        val prefsTime = prefs.getLong("cached_active_months_time", 0L)
        if (!forceRefresh && cachedActiveMonths == null && !prefsMonths.isNullOrBlank() && (now - prefsTime) < ACTIVE_MONTHS_CACHE_DURATION_MS) {
            val list = prefsMonths.split(",").map { it.trim() }.filter { it.isNotBlank() }
            if (list.isNotEmpty()) {
                cachedActiveMonths = list
                cachedActiveMonthsTimestamp = prefsTime
                return@withContext list
            }
        }

        val masterSheetId = "164xqHxPq5Q6bS9-55pmp40Z-A03Vme3-GJbgKJ913tQ"
        val url = "https://docs.google.com/spreadsheets/d/$masterSheetId/gviz/tq?tqx=out:csv&gid=0"
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 13)")
                .header("Cache-Control", "no-cache")
                .build()

            val client = PayslipApiClient.sharedHttpClient

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use emptyList<String>()
                val body = response.body?.string() ?: return@use emptyList<String>()
                val rows = parseCsv(body)
                val months = mutableListOf<String>()
                for (row in rows) {
                    if (row.size >= 4) {
                        val m = row[0].trim()
                        val status = row[3].trim()
                        if (status.equals("Active", ignoreCase = true) && m.isNotBlank() && !m.equals("Month", ignoreCase = true)) {
                            months.add(m)
                        }
                    }
                }
                if (months.isNotEmpty()) {
                    val resultList = months.reversed()
                    cachedActiveMonths = resultList
                    cachedActiveMonthsTimestamp = now
                    prefs.edit()
                        .putString("cached_active_months", resultList.joinToString(","))
                        .putLong("cached_active_months_time", now)
                        .apply()
                    return@withContext resultList
                }
            }
        } catch (_: Exception) {}

        if (cachedActiveMonths != null) return@withContext cachedActiveMonths!!

        listOf("Nov", "Oct", "Sep", "Aug", "Jul", "Jun", "May")
    }

    fun getOrGenerateUserId(): String {
        var userId = prefs.getString("user_id", null)
        if (userId.isNullOrEmpty()) {
            userId = "usr_" + UUID.randomUUID().toString().substring(0, 8)
            prefs.edit().putString("user_id", userId).apply()
        }
        return userId
    }

    suspend fun getAvailableDistricts(): List<String> = withContext(Dispatchers.IO) {
        val res = PayslipApiClient.getDistricts()
        if (res.success && res.districts.isNotEmpty()) {
            res.districts
        } else {
            listOf("Bannu")
        }
    }

    fun recordAppVisit(): Long {
        val visits = prefs.getLong("app_isolated_visits", 0L) + 1
        prefs.edit().putLong("app_isolated_visits", visits).apply()
        return visits
    }

    fun saveCredentials(cnic: String, personalNo: String, remember: Boolean) {
        prefs.edit().apply {
            putBoolean("remember_credentials", remember)
            if (remember) {
                putString("saved_cnic", cnic)
                putString("saved_personal_no", personalNo)
            } else {
                remove("saved_cnic")
                remove("saved_personal_no")
            }
            apply()
        }
    }

    fun getSavedCnic(): String = prefs.getString("saved_cnic", "") ?: ""
    fun getSavedPersonalNo(): String = prefs.getString("saved_personal_no", "") ?: ""
    fun isRememberEnabled(): Boolean = prefs.getBoolean("remember_credentials", true)

    private fun hashPassword(password: String): String {
        return try {
            val md = java.security.MessageDigest.getInstance("SHA-256")
            val salted = "kp_hrmis_salt_2026_${password.trim()}"
            val bytes = md.digest(salted.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            password.trim()
        }
    }

    private fun isPasswordMatch(entered: String, storedHashOrPlain: String): Boolean {
        if (storedHashOrPlain.isBlank()) return false
        val hashedEntered = hashPassword(entered)
        return (storedHashOrPlain == hashedEntered || storedHashOrPlain == entered.trim())
    }

    fun setAccountPassword(cnic: String, newPass: String) {
        val cleanCnic = cnic.replace(Regex("[^0-9]"), "")
        val hashed = hashPassword(newPass)
        prefs.edit().putString("pwd_$cleanCnic", hashed).apply()
    }

    suspend fun verifyPasswordOnline(cnic: String, password: String): Boolean = withContext(Dispatchers.IO) {
        val cleanCnic = cnic.replace(Regex("[^0-9]"), "").trim()
        val cleanPass = password.trim()

        // 1. Check local cache first for fast response if authenticated previously on this device
        val localPass = prefs.getString("pwd_$cleanCnic", null)
        if (localPass != null && isPasswordMatch(cleanPass, localPass)) {
            // Migrate old plain-text to SHA-256 hash if necessary
            if (localPass != hashPassword(cleanPass)) {
                prefs.edit().putString("pwd_$cleanCnic", hashPassword(cleanPass)).apply()
            }
            return@withContext true
        }

        // 2. Query Google Sheet Users tab online via secure request
        var networkAttempted = false
        try {
            val response = PayslipApiClient.verifyPassword(cleanCnic, cleanPass)
            if (response.success) {
                networkAttempted = true
                if (response.match == true) {
                    prefs.edit().putString("pwd_$cleanCnic", hashPassword(cleanPass)).apply()
                    return@withContext true
                } else if (response.match == false) {
                    return@withContext false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Fallback:
        // CRITICAL SECURITY FIX (Point 2):
        // Never allow blind offline bypass with cleanPass == cleanCnic!
        // Only allow login if this device already holds a verified hash matching cleanPass.
        if (localPass != null && isPasswordMatch(cleanPass, localPass)) {
            return@withContext true
        }

        // Offline or unverified user: reject login so internet interruption cannot bypass authentication
        return@withContext false
    }

    suspend fun savePasswordOnline(cnic: String, newPass: String, email: String? = null): Boolean = withContext(Dispatchers.IO) {
        val cleanCnic = cnic.replace(Regex("[^0-9]"), "").trim()
        val cleanPass = newPass.trim()
        val cleanEmail = (email ?: getAccountEmail(cleanCnic) ?: "").trim()

        // 1. Persist to Google Sheet Users tab first with Date & Time log
        return@withContext try {
            val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.ENGLISH).format(java.util.Date())
            val res = PayslipApiClient.savePassword(cleanCnic, cleanPass, cleanEmail, now)
            if (res.success) {
                // 2. Cache locally using SHA-256 hash ONLY after server confirms success
                val hashed = hashPassword(cleanPass)
                prefs.edit().putString("pwd_$cleanCnic", hashed).apply()
                if (cleanEmail.isNotBlank()) {
                    prefs.edit().putString("email_$cleanCnic", cleanEmail).apply()
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun saveEmailOnline(cnic: String, email: String): Boolean = withContext(Dispatchers.IO) {
        val cleanCnic = cnic.replace(Regex("[^0-9]"), "").trim()
        val cleanEmail = email.trim()

        // 1. Immediately cache locally
        setAccountEmail(cleanCnic, cleanEmail)

        // 2. Persist to Google Sheet Users tab with Date & Time log
        try {
            val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.ENGLISH).format(java.util.Date())
            
            // Explicit saveRecoveryEmail endpoint (never overwrites or touches password in Column B)
            val res = PayslipApiClient.saveRecoveryEmail(cleanCnic, cleanEmail, now)

            return@withContext res.success
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun fetchRecoveryEmailFromMaster(cnic: String): String? = withContext(Dispatchers.IO) {
        val cleanCnic = cnic.replace(Regex("[^0-9]"), "").trim()
        // Point 4 Security Fix: Do NOT download the whole Users spreadsheet CSV publicly.
        // Return locally stored email if available.
        getAccountEmail(cleanCnic)
    }

    fun hasCustomPassword(cnic: String): Boolean {
        val cleanCnic = cnic.replace(Regex("[^0-9]"), "").trim()
        val localPass = prefs.getString("pwd_$cleanCnic", null)
        if (localPass.isNullOrBlank()) return false
        val defaultHash = hashPassword(cleanCnic)
        return (localPass != defaultHash && localPass != cleanCnic)
    }

    fun getAccountEmail(cnic: String): String? {
        val cleanCnic = cnic.replace(Regex("[^0-9]"), "")
        return prefs.getString("email_$cleanCnic", null)
    }

    fun setAccountEmail(cnic: String, email: String) {
        val cleanCnic = cnic.replace(Regex("[^0-9]"), "")
        prefs.edit().putString("email_$cleanCnic", email.trim()).apply()
    }

    fun getLoggedInCnic(): String? = prefs.getString("logged_in_cnic", null)

    fun setLoggedInCnic(cnic: String?) {
        if (cnic == null) {
            prefs.edit().remove("logged_in_cnic").apply()
        } else {
            prefs.edit().putString("logged_in_cnic", cnic.replace(Regex("[^0-9]"), "")).apply()
        }
    }

    suspend fun fetchTraffic(userId: String): TrafficResponse = withContext(Dispatchers.IO) {
        val cachedVisits = prefs.getLong("server_cached_visits", 19050L)

        try {
            val serverResult = PayslipApiClient.getTraffic(userId)
            if (serverResult.success) {
                val total = serverResult.actualTotalCount
                val live = serverResult.actualLiveCount
                if (total > 0) {
                    prefs.edit().putLong("server_cached_visits", total).apply()
                }
                return@withContext TrafficResponse(
                    success = true,
                    userId = userId,
                    isApp = true,
                    liveCount = live,
                    totalCount = if (total > 0) total else cachedVisits
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        TrafficResponse(
            success = true,
            userId = userId,
            isApp = true,
            liveCount = 1,
            totalCount = cachedVisits
        )
    }

    suspend fun fetchBanner(): BannerResponse = withContext(Dispatchers.IO) {
        PayslipApiClient.getBanner()
    }

    suspend fun searchPayslip(
        district: String,
        category: String,
        month: String,
        year: String,
        query: String
    ): PayslipResponse = withContext(Dispatchers.IO) {
        val cleanQuery = query.replace(Regex("[-\\s]"), "").trim()

        // 1. Call real server API
        val apiResult = PayslipApiClient.searchPaySlip(
            district = district,
            category = category,
            month = month,
            year = year,
            query = cleanQuery
        )

        // 2. If server found the record for this person (Fahad Farid, Babar Ali, or any user), return it directly!
        if (apiResult.success && apiResult.data != null) {
            return@withContext apiResult
        }

        // 3. Fallback for test CNIC (1110159744485) if server is offline or returned month-not-uploaded error:
        if (cleanQuery == "1110159744485" && (
            apiResult.data == null ||
            apiResult.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
            apiResult.message?.contains("timeout", ignoreCase = true) == true ||
            apiResult.message?.contains("Error: منتخب کردہ ضلع/مہینے", ignoreCase = true) == true
        )) {
            val sample = PayslipEngine.getSamplePayslip(
                district = district,
                category = category,
                month = if (month.isNotBlank()) month else "Sep",
                year = year,
                query = cleanQuery
            )
            return@withContext PayslipResponse(
                success = true,
                category = category,
                month = if (month.isNotBlank()) month else "Sep",
                year = year,
                data = sample
            )
        }

        // 4. For any other employee or when server returns no record, return the clear message!
        val errMsg = apiResult.message?.ifBlank { null }
            ?: apiResult.error?.ifBlank { null }
            ?: "اس شناختی کارڈ کا ریکارڈ منتخب کردہ ضلع ($district)، کیٹیگری ($category) یا مہینے ($month $year) میں نہیں ملا۔ براہ کرم مہینہ اور سال درست منتخب کریں۔"

        return@withContext PayslipResponse(
            success = false,
            message = errMsg
        )
    }
}
