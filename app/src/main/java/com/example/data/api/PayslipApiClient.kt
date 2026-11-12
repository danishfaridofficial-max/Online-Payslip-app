package com.example.data.api

import com.example.data.model.BannerResponse
import com.example.data.model.DistrictsResponse
import com.example.data.model.PasswordResponse
import com.example.data.model.PayslipResponse
import com.example.data.model.TrafficResponse
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

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    suspend fun getDistricts(webAppUrl: String = DEFAULT_WEB_APP_URL): DistrictsResponse = withContext(Dispatchers.IO) {
        try {
            val url = "$webAppUrl?action=districts"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0) Gecko/111.0 Firefox/111.0")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val adapter = moshi.adapter(DistrictsResponse::class.java)
                adapter.fromJson(body) ?: DistrictsResponse(success = true, districts = listOf("Bannu"))
            }
        } catch (e: Exception) {
            DistrictsResponse(success = false, districts = listOf("Bannu"), error = e.localizedMessage)
        }
    }

    suspend fun getTraffic(userId: String, webAppUrl: String = DEFAULT_WEB_APP_URL): TrafficResponse = withContext(Dispatchers.IO) {
        try {
            val url = "$webAppUrl?action=traffic&source=app&platform=android&userId=${URLEncoder.encode(userId, "UTF-8")}"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .header("Accept", "application/json, text/plain, */*")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val body = response.body?.string()?.trim() ?: ""
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
            val url = "$webAppUrl?action=banner"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0) Gecko/111.0 Firefox/111.0")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val adapter = moshi.adapter(BannerResponse::class.java)
                adapter.fromJson(body) ?: BannerResponse(
                    success = true,
                    bannerHeading = "اہم اپڈیٹ: ہمارا واٹس ایپ چینل جوائن کریں!",
                    bannerText = "خیبر ایلیمنٹری اینڈ سیکنڈری ایجوکیشن کی تازہ ترین خبروں، نوٹیفیکیشنز اور تبادلوں کی اپڈیٹس کے لیے ابھی ہمارا آفیشل واٹس ایپ چینل جوائن کریں۔",
                    bannerLink = "https://whatsapp.com/channel/0029VaCK4y0EquiKbkl09S1A",
                    popupStatus = "Show",
                    popupHeading = "مبارک ہو! آپ کی پے سلپ مل گئی ہے",
                    popupText = "اگر آپ چاہتے ہیں کہ اگلے مہینے کی سیلری اپڈیٹ نوٹیفیکیشنز سب سے پہلے آپ کے واٹس ایپ پر ملیں، تو ابھی جوائن کریں!",
                    popupLink = "https://whatsapp.com/channel/0029VaCK4y0EquiKbkl09S1A"
                )
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

    suspend fun searchPaySlip(
        district: String,
        category: String,
        month: String,
        year: String,
        query: String,
        webAppUrl: String = DEFAULT_WEB_APP_URL
    ): PayslipResponse = withContext(Dispatchers.IO) {
        try {
            val encodedDistrict = URLEncoder.encode(district, "UTF-8")
            val encodedCategory = URLEncoder.encode(category, "UTF-8")
            val encodedMonth = URLEncoder.encode(month, "UTF-8")
            val encodedYear = URLEncoder.encode(year, "UTF-8")
            val cleanQuery = query.replace(Regex("[-\\s]"), "")
            val encodedQuery = URLEncoder.encode(cleanQuery, "UTF-8")

            val url = "$webAppUrl?action=search&district=$encodedDistrict&category=$encodedCategory&month=$encodedMonth&year=$encodedYear&query=$encodedQuery"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0) Gecko/111.0 Firefox/111.0")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val body = response.body?.string()?.trim() ?: ""
                if (!response.isSuccessful || body.startsWith("<!DOCTYPE", ignoreCase = true) || body.startsWith("<html", ignoreCase = true)) {
                    return@withContext PayslipResponse(success = false, message = "سرور سے درست جواب موصول نہیں ہوا۔ براہ کرم کچھ دیر بعد کوشش کریں۔")
                }
                val adapter = moshi.adapter(PayslipResponse::class.java)
                val res = try { adapter.fromJson(body) } catch (_: Exception) { null }
                res ?: PayslipResponse(success = false, message = "سرور سے جواب موصول نہیں ہوا۔")
            }
        } catch (e: Exception) {
            PayslipResponse(success = false, message = e.localizedMessage ?: "کنکشن کی خرابی")
        }
    }

    val sharedHttpClient: OkHttpClient get() = okHttpClient

    /**
     * Executes sensitive credential operations via POST body to avoid leaking CNIC/passwords in URLs.
     * Automatically falls back to GET if the deployed Google Apps Script does not have doPost(e) yet.
     */
    private fun executeSecurePostWithFallback(
        webAppUrl: String,
        formFields: Map<String, String>
    ): String {
        // 1. Primary Attempt: Send sensitive credentials in HTTP POST body (Zero URL leakage)
        try {
            val formBuilder = okhttp3.FormBody.Builder()
            formFields.forEach { (key, value) ->
                formBuilder.add(key, value)
            }
            val postRequest = Request.Builder()
                .url(webAppUrl)
                .post(formBuilder.build())
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0) Gecko/111.0 Firefox/111.0")
                .header("Accept", "application/json, text/plain, */*")
                .build()

            okHttpClient.newCall(postRequest).execute().use { response ->
                val body = response.body?.string()?.trim() ?: ""
                val isHtmlError = body.contains("Script function not found", ignoreCase = true) ||
                        body.startsWith("<!DOCTYPE", ignoreCase = true) ||
                        body.startsWith("<html", ignoreCase = true)
                if (response.isSuccessful && !isHtmlError && body.isNotBlank()) {
                    return body
                }
            }
        } catch (_: Exception) {}

        // 2. Safe Fallback to GET for backwards compatibility until Apps Script doPost is published
        val queryParams = formFields.entries.joinToString("&") { (k, v) ->
            "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
        }
        val getUrl = if (webAppUrl.contains("?")) "$webAppUrl&$queryParams" else "$webAppUrl?$queryParams"
        val getRequest = Request.Builder()
            .url(getUrl)
            .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0) Gecko/111.0 Firefox/111.0")
            .header("Accept", "application/json, text/plain, */*")
            .build()

        return okHttpClient.newCall(getRequest).execute().use { response ->
            response.body?.string()?.trim() ?: ""
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
                "password" to password.trim()
            )
            val body = executeSecurePostWithFallback(webAppUrl, fields)
            if (body.isBlank() || body.startsWith("<!DOCTYPE", ignoreCase = true) || body.startsWith("<html", ignoreCase = true)) {
                return@withContext PasswordResponse(success = false, message = "سرور سے جواب موصول نہیں ہوا۔ براہ کرم انٹرنیٹ چیک کریں۔")
            }
            val adapter = moshi.adapter(PasswordResponse::class.java)
            val parsed = try { adapter.fromJson(body) } catch (_: Exception) { null }
            parsed ?: PasswordResponse(success = false, message = "سرور سے تصدیق موصول نہیں ہوئی۔")
        } catch (e: Exception) {
            PasswordResponse(success = false, error = e.localizedMessage)
        }
    }

    suspend fun savePassword(
        cnic: String,
        password: String,
        email: String = "",
        dateTime: String = "",
        webAppUrl: String = DEFAULT_WEB_APP_URL
    ): PasswordResponse = withContext(Dispatchers.IO) {
        try {
            val cleanCnic = cnic.replace(Regex("[^0-9]"), "").trim()
            val fields = mapOf(
                "action" to "savePassword",
                "cnic" to cleanCnic,
                "password" to password.trim(),
                "email" to email.trim(),
                "dateTime" to dateTime.trim()
            )
            val body = executeSecurePostWithFallback(webAppUrl, fields)
            if (body.isBlank() || body.startsWith("<!DOCTYPE", ignoreCase = true) || body.startsWith("<html", ignoreCase = true)) {
                return@withContext PasswordResponse(success = false, message = "سرور پر محفوظ نہیں ہو سکا۔")
            }
            val adapter = moshi.adapter(PasswordResponse::class.java)
            val parsed = try { adapter.fromJson(body) } catch (_: Exception) { null }
            parsed ?: PasswordResponse(success = false, message = "سرور پر محفوظ نہیں ہو سکا۔")
        } catch (e: Exception) {
            PasswordResponse(success = false, error = e.localizedMessage)
        }
    }

    suspend fun saveRecoveryEmail(
        cnic: String,
        email: String,
        dateTime: String = "",
        webAppUrl: String = DEFAULT_WEB_APP_URL
    ): PasswordResponse = withContext(Dispatchers.IO) {
        try {
            val cleanCnic = cnic.replace(Regex("[^0-9]"), "").trim()
            val fields = mapOf(
                "action" to "saveRecoveryEmail",
                "cnic" to cleanCnic,
                "email" to email.trim(),
                "dateTime" to dateTime.trim()
            )
            val body = executeSecurePostWithFallback(webAppUrl, fields)
            if (body.isBlank() || body.startsWith("<!DOCTYPE", ignoreCase = true) || body.startsWith("<html", ignoreCase = true)) {
                return@withContext PasswordResponse(success = false, message = "سرور پر محفوظ نہیں ہو سکا۔")
            }
            val adapter = moshi.adapter(PasswordResponse::class.java)
            val parsed = try { adapter.fromJson(body) } catch (_: Exception) { null }
            parsed ?: PasswordResponse(success = false, message = "سرور پر محفوظ نہیں ہو سکا۔")
        } catch (e: Exception) {
            PasswordResponse(success = false, error = e.localizedMessage)
        }
    }
}
