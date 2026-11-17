package com.example.data.api

import com.example.data.model.ActiveMonthsResponse
import com.example.data.model.AppUpdateResponse
import com.example.data.model.BannerResponse
import com.example.data.model.DistrictsResponse
import com.example.data.model.PasswordResponse
import com.example.data.model.PayslipResponse
import com.example.data.model.TrafficResponse
import okhttp3.CacheControl
import okhttp3.ConnectionSpec
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object PayslipApiClient {
    const val DEFAULT_WEB_APP_URL = "https://script.google.com/macros/s/AKfycbwCsuhGCUK5udkwRy-s6nxTVbAupICjOOm2KL9KdTEaEFx-9UaR8eqrG4s681yuOXk-6g/exec"
    const val APP_API_TOKEN = "KP_PAYSLIP_APP_2026_V2_GUARD"

    private val okHttpClient = OkHttpClient.Builder()
        .cache(null)
        .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS))
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private fun isTrustedEndpoint(url: String): Boolean {
        val parsed = url.toHttpUrlOrNull() ?: return false
        return parsed.isHttps && parsed.host.equals("script.google.com", ignoreCase = true)
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private fun isValidJsonBody(isSuccessful: Boolean, body: String): Boolean {
        val trimmed = body.trim()
        if (!isSuccessful || trimmed.isBlank()) return false
        return !trimmed.startsWith("<!DOCTYPE", ignoreCase = true) &&
            !trimmed.startsWith("<html", ignoreCase = true) &&
            !trimmed.contains("Script function not found", ignoreCase = true)
    }

    suspend fun getDistricts(webAppUrl: String = DEFAULT_WEB_APP_URL): DistrictsResponse = withContext(Dispatchers.IO) {
        try {
            if (!isTrustedEndpoint(webAppUrl)) {
                return@withContext DistrictsResponse(success = false, districts = listOf("Bannu"), error = "Untrusted endpoint")
            }
            val url = "$webAppUrl?action=districts"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0) Gecko/111.0 Firefox/111.0")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!isValidJsonBody(response.isSuccessful, body)) {
                    return@withContext DistrictsResponse(success = false, districts = listOf("Bannu"), error = "Invalid server response")
                }
                val adapter = moshi.adapter(DistrictsResponse::class.java)
                val parsed = try { adapter.fromJson(body) } catch (_: Exception) { null }
                parsed ?: DistrictsResponse(success = false, districts = listOf("Bannu"), error = "Invalid JSON response")
            }
        } catch (e: Exception) {
            DistrictsResponse(success = false, districts = listOf("Bannu"), error = e.localizedMessage)
        }
    }

    suspend fun getTraffic(userId: String, webAppUrl: String = DEFAULT_WEB_APP_URL): TrafficResponse = withContext(Dispatchers.IO) {
        try {
            if (!isTrustedEndpoint(webAppUrl)) {
                return@withContext TrafficResponse(success = false, userId = userId, error = "Untrusted endpoint")
            }
            val url = "$webAppUrl?action=traffic&source=app&platform=android&userId=${URLEncoder.encode(userId, "UTF-8")}"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .header("Accept", "application/json, text/plain, */*")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val body = response.body?.string()?.trim() ?: ""
                if (!isValidJsonBody(response.isSuccessful, body)) {
                    return@withContext TrafficResponse(success = false, userId = userId, error = "Invalid traffic response")
                }
                val adapter = moshi.adapter(TrafficResponse::class.java)
                val parsed = try {
                    adapter.fromJson(body)
                } catch (e: Exception) {
                    null
                }
                parsed ?: TrafficResponse(success = false, userId = userId, error = "Failed to parse traffic response")
            }
        } catch (e: Exception) {
            TrafficResponse(success = false, userId = userId, error = e.localizedMessage)
        }
    }

    suspend fun getBanner(webAppUrl: String = DEFAULT_WEB_APP_URL): BannerResponse = withContext(Dispatchers.IO) {
        try {
            if (!isTrustedEndpoint(webAppUrl)) {
                return@withContext BannerResponse(success = false, error = "Untrusted endpoint")
            }
            val url = "$webAppUrl?action=banner"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0) Gecko/111.0 Firefox/111.0")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val defaultBanner = BannerResponse(
                    success = false,
                    bannerHeading = "اہم اپڈیٹ: ہمارا واٹس ایپ چینل جوائن کریں!",
                    bannerText = "خیبر ایلیمنٹری اینڈ سیکنڈری ایجوکیشن کی تازہ ترین خبروں، نوٹیفیکیشنز اور تبادلوں کی اپڈیٹس کے لیے ابھی ہمارا آفیشل واٹس ایپ چینل جوائن کریں۔",
                    bannerLink = "https://whatsapp.com/channel/0029VaCK4y0EquiKbkl09S1A",
                    popupStatus = "Show",
                    popupHeading = "مبارک ہو! آپ کی پے سلپ مل گئی ہے",
                    popupText = "اگر آپ چاہتے ہیں کہ اگلے مہینے کی سیلری اپڈیٹ نوٹیفیکیشنز سب سے پہلے آپ کے واٹس ایپ پر ملیں، تو ابھی جوائن کریں!",
                    popupLink = "https://whatsapp.com/channel/0029VaCK4y0EquiKbkl09S1A"
                )
                if (!isValidJsonBody(response.isSuccessful, body)) {
                    return@withContext defaultBanner
                }
                val adapter = moshi.adapter(BannerResponse::class.java)
                val parsed = try { adapter.fromJson(body) } catch (_: Exception) { null }
                parsed ?: defaultBanner
            }
        } catch (e: Exception) {
            BannerResponse(
                success = false,
                bannerHeading = "اہم اپڈیٹ: ہمارا واٹس ایپ چینل جوائن کریں!",
                bannerText = "خیبر ایلیمنٹری اینڈ سیکنڈری ایجوکیشن کی تازہ ترین خبروں، نوٹیفیکیشنز اور تبادلوں کی اپڈیٹس کے لیے ابھی ہمارا آفیشل واٹس ایپ چینل جوائن کریں۔",
                bannerLink = "https://whatsapp.com/channel/0029VaCK4y0EquiKbkl09S1A",
                popupStatus = "Show",
                popupHeading = "مبارک ہو! آپ کی پے سلپ مل گئی ہے",
                popupText = "اگر آپ چاہتے ہیں کہ اگلے مہینے کی سیلری اپڈیٹ نوٹیفیکیشنز سب سے پہلے آپ کے واٹس ایپ پر ملیں، تو ابھی جوائن کریں!",
                popupLink = "https://whatsapp.com/channel/0029VaCK4y0EquiKbkl09S1A",
                error = e.localizedMessage
            )
        }
    }

    suspend fun getActiveMonths(webAppUrl: String = DEFAULT_WEB_APP_URL): ActiveMonthsResponse = withContext(Dispatchers.IO) {
        try {
            if (!isTrustedEndpoint(webAppUrl)) {
                return@withContext ActiveMonthsResponse(success = false, error = "Untrusted endpoint")
            }
            val url = "$webAppUrl?action=activeMonths&appToken=${URLEncoder.encode(APP_API_TOKEN, "UTF-8")}"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0) Gecko/111.0 Firefox/111.0")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!isValidJsonBody(response.isSuccessful, body)) {
                    return@withContext ActiveMonthsResponse(success = false, error = "Invalid server response")
                }
                val adapter = moshi.adapter(ActiveMonthsResponse::class.java)
                val parsed = try { adapter.fromJson(body) } catch (_: Exception) { null }
                parsed ?: ActiveMonthsResponse(success = false, error = "Invalid JSON response")
            }
        } catch (e: Exception) {
            ActiveMonthsResponse(success = false, error = e.localizedMessage)
        }
    }

    suspend fun getAppUpdate(webAppUrl: String = DEFAULT_WEB_APP_URL): AppUpdateResponse = withContext(Dispatchers.IO) {
        try {
            if (!isTrustedEndpoint(webAppUrl)) {
                return@withContext AppUpdateResponse(success = false, error = "Untrusted endpoint")
            }
            val url = "$webAppUrl?action=appUpdate&appToken=${URLEncoder.encode(APP_API_TOKEN, "UTF-8")}"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0) Gecko/111.0 Firefox/111.0")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!isValidJsonBody(response.isSuccessful, body)) {
                    return@withContext AppUpdateResponse(success = false, error = "Invalid server response")
                }
                val adapter = moshi.adapter(AppUpdateResponse::class.java)
                val parsed = try { adapter.fromJson(body) } catch (_: Exception) { null }
                parsed ?: AppUpdateResponse(success = false, error = "Invalid JSON response")
            }
        } catch (e: Exception) {
            AppUpdateResponse(success = false, error = e.localizedMessage)
        }
    }

    suspend fun searchPaySlip(
        district: String,
        category: String,
        month: String,
        year: String,
        query: String,
        sessionToken: String = "",
        webAppUrl: String = DEFAULT_WEB_APP_URL
    ): PayslipResponse = withContext(Dispatchers.IO) {
        try {
            if (!isTrustedEndpoint(webAppUrl)) {
                return@withContext PayslipResponse(success = false, message = "سیکیورٹی الرٹ: غیر محفوظ سرور لنک مسترد کر دیا گیا۔")
            }

            val cleanQuery = query.replace(Regex("[^0-9]"), "").trim()
            if (cleanQuery.length < 13) {
                return@withContext PayslipResponse(success = false, message = "براہ کرم درست 13 ہندسوں کا شناختی کارڈ نمبر درج کریں۔")
            }

            val fields = mutableMapOf(
                "action" to "search",
                "district" to district.trim(),
                "category" to category.trim(),
                "month" to month.trim(),
                "year" to year.trim(),
                "query" to cleanQuery,
                "appToken" to APP_API_TOKEN
            )
            if (sessionToken.isNotBlank()) {
                fields["sessionToken"] = sessionToken
            }

            val body = executeSecurePost(webAppUrl, fields)
            if (body.isNullOrBlank()) {
                return@withContext PayslipResponse(success = false, message = "سرور سے درست جواب موصول نہیں ہوا۔ براہ کرم کچھ دیر بعد کوشش کریں۔")
            }
            val adapter = moshi.adapter(PayslipResponse::class.java)
            val res = try { adapter.fromJson(body) } catch (_: Exception) { null }
            res ?: PayslipResponse(success = false, message = "سرور سے جواب موصول نہیں ہوا۔")
        } catch (e: Exception) {
            PayslipResponse(success = false, message = e.localizedMessage ?: "کنکشن کی خرابی")
        }
    }

    val sharedHttpClient: OkHttpClient get() = okHttpClient

    /**
     * Executes sensitive credential operations strictly via HTTP POST body (never GET URL).
     */
    private fun executeSecurePost(
        webAppUrl: String,
        formFields: Map<String, String>
    ): String? {
        return try {
            if (!isTrustedEndpoint(webAppUrl)) return null
            val formBuilder = okhttp3.FormBody.Builder()
            formFields.forEach { (key, value) ->
                formBuilder.add(key, value)
            }
            val postRequest = Request.Builder()
                .url(webAppUrl)
                .post(formBuilder.build())
                .cacheControl(CacheControl.FORCE_NETWORK)
                .header("Cache-Control", "no-store, no-cache, must-revalidate")
                .header("Pragma", "no-cache")
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0) Gecko/111.0 Firefox/111.0")
                .header("Accept", "application/json, text/plain, */*")
                .build()

            okHttpClient.newCall(postRequest).execute().use { response ->
                val body = response.body?.string()?.trim() ?: ""
                val isHtmlError = body.contains("Script function not found", ignoreCase = true) ||
                        body.startsWith("<!DOCTYPE", ignoreCase = true) ||
                        body.startsWith("<html", ignoreCase = true)
                if (response.isSuccessful && !isHtmlError && body.isNotBlank()) {
                    body
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun verifyPassword(
        cnic: String,
        password: String,
        webAppUrl: String = DEFAULT_WEB_APP_URL
    ): PasswordResponse = withContext(Dispatchers.IO) {
        try {
            val cleanCnic = cnic.replace(Regex("[^0-9]"), "").trim()
            val fields = mapOf(
                "action" to "verifyPassword",
                "cnic" to cleanCnic,
                "password" to password.trim(),
                "appToken" to APP_API_TOKEN
            )
            val body = executeSecurePost(webAppUrl, fields)
            if (body.isNullOrBlank()) {
                return@withContext PasswordResponse(
                    success = false,
                    message = "انٹرنیٹ یا سرور کی خرابی کی وجہ سے تصدیق نہیں ہو سکی۔"
                )
            }
            val adapter = moshi.adapter(PasswordResponse::class.java)
            val parsed = try { adapter.fromJson(body) } catch (_: Exception) { null }
            parsed ?: PasswordResponse(
                success = false,
                message = "سرور سے درست تصدیق موصول نہیں ہوئی، براہ کرم دوبارہ کوشش کریں۔"
            )
        } catch (e: Exception) {
            PasswordResponse(
                success = false,
                message = "انٹرنیٹ یا سرور کی خرابی کی وجہ سے تصدیق نہیں ہو سکی۔",
                error = e.localizedMessage
            )
        }
    }

    suspend fun savePassword(
        cnic: String,
        password: String,
        email: String = "",
        dateTime: String = "",
        sessionToken: String = "",
        resetToken: String = "",
        webAppUrl: String = DEFAULT_WEB_APP_URL
    ): PasswordResponse = withContext(Dispatchers.IO) {
        try {
            val cleanCnic = cnic.replace(Regex("[^0-9]"), "").trim()
            val fields = mutableMapOf(
                "action" to "savePassword",
                "cnic" to cleanCnic,
                "password" to password.trim(),
                "email" to email.trim(),
                "dateTime" to dateTime.trim(),
                "appToken" to APP_API_TOKEN
            )
            if (sessionToken.isNotBlank()) fields["sessionToken"] = sessionToken
            if (resetToken.isNotBlank()) fields["resetToken"] = resetToken

            val body = executeSecurePost(webAppUrl, fields)
            if (body.isNullOrBlank()) {
                return@withContext PasswordResponse(
                    success = false,
                    message = "انٹرنیٹ یا سرور کی خرابی کی وجہ سے پاس ورڈ محفوظ نہیں ہو سکا"
                )
            }
            val adapter = moshi.adapter(PasswordResponse::class.java)
            val parsed = try { adapter.fromJson(body) } catch (_: Exception) { null }
            parsed ?: PasswordResponse(
                success = false,
                message = "سرور پر پاس ورڈ محفوظ نہیں ہو سکا، براہ کرم دوبارہ کوشش کریں۔"
            )
        } catch (e: Exception) {
            PasswordResponse(
                success = false,
                message = "انٹرنیٹ یا سرور کی خرابی کی وجہ سے پاس ورڈ محفوظ نہیں ہو سکا",
                error = e.localizedMessage
            )
        }
    }

    suspend fun saveRecoveryEmail(
        cnic: String,
        email: String,
        dateTime: String = "",
        sessionToken: String = "",
        webAppUrl: String = DEFAULT_WEB_APP_URL
    ): PasswordResponse = withContext(Dispatchers.IO) {
        try {
            val cleanCnic = cnic.replace(Regex("[^0-9]"), "").trim()
            val fields = mutableMapOf(
                "action" to "saveRecoveryEmail",
                "cnic" to cleanCnic,
                "email" to email.trim(),
                "dateTime" to dateTime.trim(),
                "appToken" to APP_API_TOKEN
            )
            if (sessionToken.isNotBlank()) fields["sessionToken"] = sessionToken

            val body = executeSecurePost(webAppUrl, fields)
            if (body.isNullOrBlank()) {
                return@withContext PasswordResponse(
                    success = false,
                    message = "انٹرنیٹ یا سرور کی خرابی کی وجہ سے ای میل محفوظ نہیں ہو سکی۔"
                )
            }
            val adapter = moshi.adapter(PasswordResponse::class.java)
            val parsed = try { adapter.fromJson(body) } catch (_: Exception) { null }
            parsed ?: PasswordResponse(
                success = false,
                message = "سرور پر ای میل محفوظ نہیں ہو سکی، براہ کرم دوبارہ کوشش کریں۔"
            )
        } catch (e: Exception) {
            PasswordResponse(
                success = false,
                message = "انٹرنیٹ یا سرور کی خرابی کی وجہ سے ای میل محفوظ نہیں ہو سکی۔",
                error = e.localizedMessage
            )
        }
    }

    suspend fun getRecoveryEmail(
        cnic: String,
        sessionToken: String = "",
        webAppUrl: String = DEFAULT_WEB_APP_URL
    ): PasswordResponse = withContext(Dispatchers.IO) {
        try {
            val cleanCnic = cnic.replace(Regex("[^0-9]"), "").trim()
            val fields = mutableMapOf(
                "action" to "getRecoveryEmail",
                "cnic" to cleanCnic,
                "appToken" to APP_API_TOKEN
            )
            if (sessionToken.isNotBlank()) fields["sessionToken"] = sessionToken

            val body = executeSecurePost(webAppUrl, fields)
            if (body.isNullOrBlank()) {
                return@withContext PasswordResponse(success = false)
            }
            val adapter = moshi.adapter(PasswordResponse::class.java)
            try { adapter.fromJson(body) } catch (_: Exception) { null } ?: PasswordResponse(success = false)
        } catch (_: Exception) {
            PasswordResponse(success = false)
        }
    }

    suspend fun verifyPersonnelNoForReset(
        district: String,
        category: String,
        month: String,
        year: String,
        cnic: String,
        personnelNo: String,
        webAppUrl: String = DEFAULT_WEB_APP_URL
    ): PasswordResponse = withContext(Dispatchers.IO) {
        try {
            val cleanCnic = cnic.replace(Regex("[^0-9]"), "").trim()
            val cleanPn = personnelNo.replace(Regex("[^0-9]"), "").trim()
            val fields = mapOf(
                "action" to "verifyPersonnelNoForReset",
                "district" to district.trim(),
                "category" to category.trim(),
                "month" to month.trim(),
                "year" to year.trim(),
                "cnic" to cleanCnic,
                "personnelNo" to cleanPn,
                "appToken" to APP_API_TOKEN
            )
            val body = executeSecurePost(webAppUrl, fields)
            if (body.isNullOrBlank()) {
                return@withContext PasswordResponse(success = false)
            }
            val adapter = moshi.adapter(PasswordResponse::class.java)
            try { adapter.fromJson(body) } catch (_: Exception) { null } ?: PasswordResponse(success = false)
        } catch (_: Exception) {
            PasswordResponse(success = false)
        }
    }

    suspend fun verifyRecoveryEmailForReset(
        cnic: String,
        email: String,
        webAppUrl: String = DEFAULT_WEB_APP_URL
    ): PasswordResponse = withContext(Dispatchers.IO) {
        try {
            val cleanCnic = cnic.replace(Regex("[^0-9]"), "").trim()
            val fields = mapOf(
                "action" to "verifyRecoveryEmailForReset",
                "cnic" to cleanCnic,
                "email" to email.trim(),
                "appToken" to APP_API_TOKEN
            )
            val body = executeSecurePost(webAppUrl, fields)
            if (body.isNullOrBlank()) {
                return@withContext PasswordResponse(success = false)
            }
            val adapter = moshi.adapter(PasswordResponse::class.java)
            try { adapter.fromJson(body) } catch (_: Exception) { null } ?: PasswordResponse(success = false)
        } catch (_: Exception) {
            PasswordResponse(success = false)
        }
    }
}
