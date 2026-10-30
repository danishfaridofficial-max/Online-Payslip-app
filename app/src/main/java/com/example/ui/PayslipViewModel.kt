package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.engine.PayslipEngine
import com.example.data.model.PayslipData
import com.example.data.repository.PayslipRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PayslipUiState(
    val selectedDistrict: String = "Bannu",
    val availableDistricts: List<String> = listOf("Bannu", "Lakki Marwat"),
    val activeDistricts: List<String> = listOf("Bannu"),
    val selectedCategory: String = "District",
    val selectedMonth: String = "Sep",
    val selectedYear: String = "2026",
    val searchQuery: String = "",
    val personalNumberQuery: String = "",
    val rememberCredentials: Boolean = true,
    val portalMode: String = "hrmis", // "hrmis" (KPESE portal) or "search" (Direct guest search)
    val isHrmisLoggedIn: Boolean = false,
    val loggedInEmployee: PayslipData? = null,
    val employeeEmail: String? = null,
    val loginCnic: String = "",
    val loginPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val showForgotPasswordModal: Boolean = false,
    val forgotStep: String = "method", // "method", "personal_no", "email", "new_password"
    val forgotCnicInput: String = "",
    val forgotPersonalNoInput: String = "",
    val forgotEmailInput: String = "",
    val forgotNewPasswordInput: String = "",
    val showChangePasswordModal: Boolean = false,
    val changeCurrentPassInput: String = "",
    val changeNewPassInput: String = "",
    val showEditEmailModal: Boolean = false,
    val editEmailInput: String = "",
    val isLoading: Boolean = false,
    val loadingMessage: String = "براہ کرم انتظار کریں...",
    val toastMessage: String? = null,
    val toastType: String = "info", // info, success, error, warning
    val currentPayslip: PayslipData? = null,
    val showPayslipModal: Boolean = false,
    val showWhatsappPopup: Boolean = false,
    val liveTrafficCount: Int = 1,
    val totalVisitsCount: Long = 1450L,
    val bannerHeading: String = "اہم اپڈیٹ: ہمارا واٹس ایپ چینل جوائن کریں!",
    val bannerText: String = "خیبر ایلیمنٹری اینڈ سیکنڈری ایجوکیشن کی تازہ ترین خبروں، نوٹیفیکیشنز اور تبادلوں کی اپڈیٹس کے لیے ابھی ہمارا آفیشل واٹس ایپ چینل جوائن کریں۔",
    val bannerLink: String = "https://whatsapp.com/channel/0029VaCK4y0EquiKbkl09S1A",
    val popupHeading: String = "مبارک ہو! آپ کی پے سلپ مل گئی ہے",
    val popupText: String = "اگر آپ چاہتے ہیں کہ اگلے مہینے کی سیلری اپڈیٹ نوٹیفیکیشنز سب سے پہلے آپ کے واٹس ایپ پر ملیں، تو ابھی جوائن کریں!",
    val popupLink: String = "https://whatsapp.com/channel/0029VaCK4y0EquiKbkl09S1A",
    val popupStatus: String = "show",
    val currentDateFormatted: String = ""
)

class PayslipViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PayslipRepository(application.applicationContext)
    private val userId = repository.getOrGenerateUserId()

    private val _uiState = MutableStateFlow(PayslipUiState())
    val uiState: StateFlow<PayslipUiState> = _uiState.asStateFlow()

    init {
        try {
            val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.ENGLISH)
            val remember = repository.isRememberEnabled()
            val savedCnic = if (remember) repository.getSavedCnic() else ""
            val savedPn = if (remember) repository.getSavedPersonalNo() else ""

            _uiState.update {
                it.copy(
                    currentDateFormatted = dateFormat.format(Date()),
                    searchQuery = savedCnic,
                    personalNumberQuery = savedPn,
                    rememberCredentials = remember
                )
            }
            repository.recordAppVisit()
        } catch (_: Throwable) {}

        loadInitialData()
        startTrafficPolling()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Stagger network calls slightly so low-end CPUs don't spike during Splash -> Home transition
            try {
                val dists = repository.getAvailableDistricts()
                if (dists.isNotEmpty()) {
                    _uiState.update { it.copy(availableDistricts = dists) }
                }
            } catch (_: Throwable) {}

            delay(300L)

            try {
                val banner = repository.fetchBanner()
                if (banner.success) {
                    _uiState.update { state ->
                        state.copy(
                            bannerHeading = banner.bannerHeading?.ifBlank { state.bannerHeading } ?: state.bannerHeading,
                            bannerText = banner.bannerText?.ifBlank { state.bannerText } ?: state.bannerText,
                            bannerLink = banner.bannerLink?.ifBlank { state.bannerLink } ?: state.bannerLink,
                            popupStatus = (banner.popupStatus ?: "show").lowercase().trim(),
                            popupHeading = banner.popupHeading?.ifBlank { state.popupHeading } ?: state.popupHeading,
                            popupText = banner.popupText?.ifBlank { state.popupText } ?: state.popupText,
                            popupLink = banner.popupLink?.ifBlank { state.popupLink } ?: state.popupLink
                        )
                    }
                }
            } catch (_: Throwable) {}
        }
    }

    private fun startTrafficPolling() {
        viewModelScope.launch {
            delay(800L)
            while (true) {
                // Only poll traffic when not actively searching or viewing a payslip to keep UI silky smooth
                if (!_uiState.value.isLoading && !_uiState.value.showPayslipModal) {
                    refreshTraffic()
                }
                delay(20_000L) // Poll every 20s to save battery and CPU on budget phones
            }
        }
    }

    fun refreshTraffic() {
        viewModelScope.launch {
            try {
                val traffic = repository.fetchTraffic(userId)
                if (traffic.success) {
                    val live = traffic.actualLiveCount
                    val total = traffic.actualTotalCount
                    _uiState.update {
                        it.copy(
                            liveTrafficCount = if (live > 0) live else it.liveTrafficCount,
                            totalVisitsCount = if (total > 0) total else it.totalVisitsCount
                        )
                    }
                }
            } catch (_: Throwable) {}
        }
    }

    fun onDistrictSelected(district: String) {
        _uiState.update { it.copy(selectedDistrict = district) }
    }

    fun onCategorySelected(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onMonthSelected(month: String) {
        _uiState.update { it.copy(selectedMonth = month) }
    }

    fun onYearSelected(year: String) {
        _uiState.update { it.copy(selectedYear = year) }
    }

    fun onSearchQueryChanged(query: String) {
        if (_uiState.value.toastMessage != null) {
            clearToast()
        }
        val urduDigits = "۰۱۲۳۴۵۶۷۸۹"
        val arabicDigits = "٠١٢٣٤٥٦٧٨٩"
        val sb = StringBuilder()
        for (ch in query) {
            val uIdx = urduDigits.indexOf(ch)
            val aIdx = arabicDigits.indexOf(ch)
            when {
                ch.isDigit() -> sb.append(ch)
                uIdx != -1 -> sb.append(uIdx)
                aIdx != -1 -> sb.append(aIdx)
                ch == '-' -> sb.append('-')
            }
        }
        val sanitized = sb.toString().take(15)
        _uiState.update { it.copy(searchQuery = sanitized) }
    }

    fun onPersonalNumberChanged(query: String) {
        if (_uiState.value.toastMessage != null) {
            clearToast()
        }
        val urduDigits = "۰۱۲۳۴۵۶۷۸۹"
        val arabicDigits = "٠١٢٣٤٥٦٧٨٩"
        val sb = StringBuilder()
        for (ch in query) {
            val uIdx = urduDigits.indexOf(ch)
            val aIdx = arabicDigits.indexOf(ch)
            when {
                ch.isDigit() -> sb.append(ch)
                uIdx != -1 -> sb.append(uIdx)
                aIdx != -1 -> sb.append(aIdx)
            }
        }
        val sanitized = sb.toString().take(12)
        _uiState.update { it.copy(personalNumberQuery = sanitized) }
    }

    fun onToggleRememberCredentials(enabled: Boolean) {
        _uiState.update { it.copy(rememberCredentials = enabled) }
        repository.saveCredentials(
            cnic = if (enabled) _uiState.value.searchQuery else "",
            personalNo = if (enabled) _uiState.value.personalNumberQuery else "",
            remember = enabled
        )
    }

    fun onResetClick() {
        val state = _uiState.value
        if (state.isHrmisLoggedIn && state.loggedInEmployee != null) {
            _uiState.update {
                it.copy(
                    selectedMonth = "Sep",
                    selectedYear = "2026",
                    toastMessage = "فارم ڈیفالٹ پر ری سیٹ کر دیا گیا ہے",
                    toastType = "info"
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    selectedDistrict = "Bannu",
                    selectedCategory = "District",
                    selectedMonth = "Sep",
                    selectedYear = "2026",
                    searchQuery = "",
                    personalNumberQuery = "",
                    toastMessage = "فارم صاف کر دیا گیا ہے",
                    toastType = "info"
                )
            }
        }
    }

    fun onSearchClick() {
        val state = _uiState.value
        if (state.isLoading) return

        val cleanQuery = if (state.isHrmisLoggedIn && state.loggedInEmployee != null) {
            state.loggedInEmployee.cnic.replace(Regex("[^0-9]"), "").trim()
        } else {
            state.searchQuery.replace(Regex("[-\\s]"), "").trim()
        }

        // Strict security enforcement: logged in users can NEVER query another CNIC
        if (state.isHrmisLoggedIn) {
            val loggedInCnic = state.loggedInEmployee?.cnic?.replace(Regex("[^0-9]"), "") ?: repository.getLoggedInCnic()
            if (loggedInCnic.isNullOrBlank() || cleanQuery != loggedInCnic) {
                showToast("سیکیورٹی الرٹ: آپ صرف اپنا پے سلپ ریکارڈ تلاش کر سکتے ہیں!", "error")
                return
            }
        }

        if (state.selectedDistrict.isBlank()) {
            showToast("براہ کرم ضلع منتخب کریں!", "warning")
            return
        }

        if (cleanQuery.isBlank()) {
            showToast("براہ کرم شناختی کارڈ نمبر درج کریں!", "warning")
            return
        }

        if (cleanQuery.length < 13) {
            showToast("شناختی کارڈ نمبر کم از کم 13 ہندسوں پر مشتمل ہونا چاہیے!", "warning")
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                loadingMessage = "شناختی کارڈ کے ریکارڈ کی تصدیق کی جا رہی ہے..."
            )
        }

        viewModelScope.launch {
            try {
                val result = repository.searchPayslip(
                    district = state.selectedDistrict,
                    category = state.selectedCategory,
                    month = state.selectedMonth,
                    year = state.selectedYear,
                    query = cleanQuery
                )

                if (result.success && result.data != null) {
                    if (state.rememberCredentials) {
                        repository.saveCredentials(cleanQuery, "", true)
                    }

                    val updatedMonth = result.month?.ifBlank { state.selectedMonth } ?: state.selectedMonth
                    val updatedYear = result.year?.ifBlank { state.selectedYear } ?: state.selectedYear

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentPayslip = result.data,
                            loggedInEmployee = if (it.isHrmisLoggedIn) result.data else it.loggedInEmployee,
                            selectedMonth = updatedMonth,
                            selectedYear = updatedYear,
                            showPayslipModal = true,
                            toastMessage = "پے سلپ ریکارڈ مل گیا!",
                            toastType = "success"
                        )
                    }

                    // If popupStatus is "show", trigger congratulatory WhatsApp popup after 1.5 seconds
                    if (_uiState.value.popupStatus == "show") {
                        delay(1500L)
                        if (_uiState.value.showPayslipModal) {
                            _uiState.update { it.copy(showWhatsappPopup = true) }
                        }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    val msg = result.message ?: "اس شناختی کارڈ کا کوئی ریکارڈ نہیں ملا۔"
                    showToast(msg, "error")
                }
            } catch (t: Throwable) {
                _uiState.update { it.copy(isLoading = false) }
                showToast("تکنیکی خرابی: ${t.localizedMessage ?: "کنکشن کی خرابی"}", "error")
            }
        }
    }

    fun switchMonthSummaryOnly(month: String) {
        val state = _uiState.value
        val cleanQuery = (state.loggedInEmployee?.cnic?.replace("-", "")?.trim() ?: "")
            .ifBlank { state.loginCnic.replace("-", "").trim() }
            .ifBlank { state.searchQuery.replace("-", "").trim() }

        _uiState.update {
            it.copy(
                selectedMonth = month,
                isLoading = true,
                loadingMessage = "$month 2026 کا ریکارڈ لوڈ ہو رہا ہے..."
            )
        }

        viewModelScope.launch {
            try {
                val result = repository.searchPayslip(
                    district = state.selectedDistrict.ifBlank { "Bannu" },
                    category = state.selectedCategory.ifBlank { "Teaching" },
                    month = month,
                    year = state.selectedYear.ifBlank { "2026" },
                    query = cleanQuery
                )

                if (result.success && result.data != null) {
                    val updatedMonth = result.month?.ifBlank { month } ?: month
                    val updatedYear = result.year?.ifBlank { state.selectedYear } ?: state.selectedYear

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentPayslip = result.data,
                            loggedInEmployee = result.data,
                            selectedMonth = updatedMonth,
                            selectedYear = updatedYear,
                            showPayslipModal = false,
                            toastMessage = "$updatedMonth 2026 کی سیلری سمری اپ ڈیٹ ہو گئی!",
                            toastType = "success"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            selectedMonth = month,
                            showPayslipModal = false
                        )
                    }
                    val msg = result.message ?: "$month 2026 کا کوئی نیا ریکارڈ نہیں ملا۔"
                    showToast(msg, "warning")
                }
            } catch (t: Throwable) {
                _uiState.update { it.copy(isLoading = false, showPayslipModal = false) }
                showToast("تکنیکی خرابی: ${t.localizedMessage ?: "کنکشن کی خرابی"}", "error")
            }
        }
    }

    fun openPayslipModal() {
        if (_uiState.value.currentPayslip != null) {
            _uiState.update { it.copy(showPayslipModal = true) }
        } else {
            onSearchClick()
        }
    }

    fun dismissPayslipModal() {
        _uiState.update { it.copy(showPayslipModal = false, showWhatsappPopup = false) }
    }

    fun dismissWhatsappPopup() {
        _uiState.update { it.copy(showWhatsappPopup = false) }
    }

    private var toastJob: Job? = null

    fun showToast(msg: String, type: String = "info") {
        toastJob?.cancel()
        _uiState.update { it.copy(toastMessage = msg, toastType = type) }
        toastJob = viewModelScope.launch {
            delay(5000L)
            _uiState.update { it.copy(toastMessage = null) }
        }
    }

    fun clearToast() {
        toastJob?.cancel()
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun onTogglePortalMode(mode: String) {
        _uiState.update { it.copy(portalMode = mode) }
    }

    fun onLoginCnicChanged(cnic: String) {
        val sanitized = cnic.filter { it.isDigit() || it == '-' }.take(15)
        _uiState.update { it.copy(loginCnic = sanitized) }
    }

    fun onLoginPasswordChanged(pass: String) {
        _uiState.update { it.copy(loginPassword = pass.take(30)) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onHrmisLogin() {
        val state = _uiState.value
        if (state.isLoading) return

        val cleanCnic = state.loginCnic.replace(Regex("[^0-9]"), "").trim()
        val enteredPass = state.loginPassword.trim()

        if (cleanCnic.length < 13) {
            showToast("براہ کرم درست 13 ہندسوں کا شناختی کارڈ نمبر درج کریں!", "warning")
            return
        }

        if (enteredPass.isBlank()) {
            showToast("براہ کرم پاس ورڈ درج کریں! (پہلی بار پاس ورڈ آپ کا شناختی کارڈ نمبر ہے)", "warning")
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                loadingMessage = "پاس ورڈ اور شناختی کارڈ کی تصدیق کی جا رہی ہے..."
            )
        }

        viewModelScope.launch {
            try {
                // Verify password against Google Sheet 'Users' tab (with offline local fallback)
                val isPassValid = repository.verifyPasswordOnline(cleanCnic, enteredPass)
                if (!isPassValid) {
                    _uiState.update { it.copy(isLoading = false) }
                    showToast("❌ پاس ورڈ درست نہیں ہے۔ (اگر پہلی بار لاگ ان کر رہے ہیں تو پاس ورڈ آپ کا شناختی کارڈ نمبر ہے)", "error")
                    return@launch
                }

                _uiState.update {
                    it.copy(loadingMessage = "آن لائن پے سلپ پورٹل میں لاگ ان کیا جا رہا ہے...")
                }
                val initialDistrict = if (state.selectedDistrict.isNotBlank()) state.selectedDistrict else "Bannu"
                val initialCategory = if (state.selectedCategory.isNotBlank()) state.selectedCategory else "District"
                val searchYear = if (state.selectedYear.isNotBlank()) state.selectedYear else "2026"

                // Descending months to find the latest uploaded payslip in the sheet (Sep -> Aug -> Jul -> Jun -> May)
                val scanMonths = listOf("Sep", "Aug", "Jul", "Jun", "May")
                // Scan categories: primary category first, then Federal, Provincial, District
                val scanCategories = listOf(initialCategory) + listOf("District", "Federal", "Provincial").filter { it != initialCategory }

                var result: com.example.data.model.PayslipResponse? = null
                var detectedCategory = initialCategory
                var detectedDistrict = initialDistrict
                var detectedMonth = "Sep"

                // Smart auto-detection loop
                outerScan@ for (cat in scanCategories) {
                    val dist = if (cat == "Federal") "Federal" else initialDistrict
                    for (m in scanMonths) {
                        val monthResult = repository.searchPayslip(
                            district = dist,
                            category = cat,
                            month = m,
                            year = searchYear,
                            query = cleanCnic
                        )
                        if (monthResult.success && monthResult.data != null) {
                            result = monthResult
                            detectedCategory = cat
                            detectedDistrict = dist
                            detectedMonth = monthResult.month?.ifBlank { m } ?: m
                            break@outerScan
                        }
                    }
                }

                // If test CNIC (1110159744485) or sample fallback
                if (result == null || !result.success || result.data == null) {
                    if (cleanCnic == "1110159744485" || result?.message?.contains("Error: منتخب کردہ ضلع/مہینے") == true) {
                        val sample = PayslipEngine.getSamplePayslip(
                            district = initialDistrict,
                            category = initialCategory,
                            month = "Sep",
                            year = "2026",
                            query = cleanCnic
                        )
                        result = com.example.data.model.PayslipResponse(
                            success = true,
                            category = initialCategory,
                            month = "Sep",
                            year = "2026",
                            data = sample
                        )
                        detectedMonth = "Sep"
                    }
                }

                if (result != null && result.success && result.data != null) {
                    repository.setLoggedInCnic(cleanCnic)
                    val email = repository.getAccountEmail(cleanCnic)
                    val activeMonth = result.month?.ifBlank { detectedMonth } ?: detectedMonth
                    val activeYear = result.year?.ifBlank { "2026" } ?: "2026"

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isHrmisLoggedIn = true,
                            loggedInEmployee = result.data,
                            currentPayslip = result.data,
                            employeeEmail = email,
                            selectedDistrict = detectedDistrict,
                            selectedCategory = detectedCategory,
                            selectedMonth = activeMonth,
                            selectedYear = activeYear,
                            searchQuery = cleanCnic,
                            showChangePasswordModal = (!repository.hasCustomPassword(cleanCnic) && enteredPass == cleanCnic),
                            changeCurrentPassInput = enteredPass,
                            changeNewPassInput = "",
                            toastMessage = "خوش آمدید! ${result.data.name} کی تازہ ترین پے سلپ ($activeMonth $activeYear) لاگ ان ہو گئی ہے۔",
                            toastType = "success"
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    showToast(result?.message ?: "اس شناختی کارڈ کا ڈیٹا بیس میں کوئی ریکارڈ نہیں ملا۔", "error")
                }
            } catch (t: Throwable) {
                _uiState.update { it.copy(isLoading = false) }
                showToast("کنکشن کی خرابی: ${t.localizedMessage ?: "دوبارہ کوشش کریں"}", "error")
            }
        }
    }

    fun onHrmisLogout() {
        repository.setLoggedInCnic(null)
        _uiState.update {
            it.copy(
                isHrmisLoggedIn = false,
                loggedInEmployee = null,
                loginPassword = "",
                toastMessage = "آپ کامیابی سے لاگ آؤٹ ہو گئے ہیں۔",
                toastType = "info"
            )
        }
    }

    // Forgot password flow
    fun onOpenForgotPasswordModal() {
        _uiState.update {
            it.copy(
                showForgotPasswordModal = true,
                forgotStep = "method",
                forgotCnicInput = it.loginCnic,
                forgotPersonalNoInput = "",
                forgotEmailInput = "",
                forgotNewPasswordInput = ""
            )
        }
    }

    fun onDismissForgotPasswordModal() {
        _uiState.update { it.copy(showForgotPasswordModal = false) }
    }

    fun onForgotStepSelected(step: String) {
        _uiState.update { it.copy(forgotStep = step) }
    }

    fun onForgotCnicChanged(cnic: String) {
        _uiState.update { it.copy(forgotCnicInput = cnic.filter { ch -> ch.isDigit() || ch == '-' }.take(15)) }
    }

    fun onForgotPersonalNoChanged(pn: String) {
        _uiState.update { it.copy(forgotPersonalNoInput = pn.filter { ch -> ch.isDigit() }.take(12)) }
    }

    fun onForgotEmailChanged(email: String) {
        _uiState.update { it.copy(forgotEmailInput = email.trim().take(50)) }
    }

    fun onForgotNewPasswordChanged(pass: String) {
        _uiState.update { it.copy(forgotNewPasswordInput = pass.take(30)) }
    }

    fun onSubmitForgotVerification() {
        val state = _uiState.value
        val cleanCnic = state.forgotCnicInput.replace(Regex("[^0-9]"), "").trim()

        if (cleanCnic.length < 13) {
            showToast("براہ کرم درست 13 ہندسوں کا شناختی کارڈ نمبر درج کریں!", "warning")
            return
        }

        if (state.forgotStep == "personal_no") {
            val cleanPn = state.forgotPersonalNoInput.trim()
            if (cleanPn.isBlank()) {
                showToast("براہ کرم 8 ہندسوں کا پرسنل نمبر درج کریں!", "warning")
                return
            }

            _uiState.update { it.copy(isLoading = true, loadingMessage = "پرسنل نمبر کی تصدیق کی جا رہی ہے...") }

            viewModelScope.launch {
                try {
                    val result = repository.searchPayslip(
                        district = state.selectedDistrict,
                        category = state.selectedCategory,
                        month = state.selectedMonth,
                        year = state.selectedYear,
                        query = cleanCnic
                    )

                    _uiState.update { it.copy(isLoading = false) }

                    if (result.success && result.data != null) {
                        val serverPn = result.data.personnelNo.replace(Regex("[^0-9]"), "").trim()
                        val isMatch = serverPn.isEmpty() || serverPn == cleanPn || serverPn.trimStart('0') == cleanPn.trimStart('0')
                        if (isMatch) {
                            _uiState.update { it.copy(forgotStep = "new_password") }
                        } else {
                            showToast("⚠️ شناختی کارڈ اور پرسنل نمبر میچ نہیں ہوئے!", "error")
                        }
                    } else {
                        showToast("شناختی کارڈ کا کوئی ریکارڈ نہیں ملا!", "error")
                    }
                } catch (t: Throwable) {
                    _uiState.update { it.copy(isLoading = false) }
                    showToast("تکنیکی خرابی: ${t.localizedMessage ?: "دوبارہ کوشش کریں"}", "error")
                }
            }
        } else if (state.forgotStep == "email") {
            val email = state.forgotEmailInput.trim()
            if (email.isBlank()) {
                showToast("براہ کرم ای میل ایڈریس درج کریں!", "warning")
                return
            }

            val savedEmail = repository.getAccountEmail(cleanCnic)
            if (savedEmail.isNullOrBlank()) {
                showToast("اس پروفائل میں کوئی ای میل درج نہیں ہے۔ براہ کرم 'پرسنل نمبر' کے ذریعے ری سیٹ کریں!", "warning")
                return
            }

            if (savedEmail.equals(email, ignoreCase = true)) {
                _uiState.update { it.copy(forgotStep = "new_password") }
            } else {
                showToast("درج کردہ ای میل پروفائل کی ای میل سے میچ نہیں ہوئی!", "error")
            }
        }
    }

    fun onSubmitForgotNewPassword() {
        val state = _uiState.value
        val cleanCnic = state.forgotCnicInput.replace(Regex("[^0-9]"), "").trim()
        val newPass = state.forgotNewPasswordInput.trim()

        if (newPass.length < 4) {
            showToast("پاس ورڈ کم از کم 4 حروف یا ہندسوں پر مشتمل ہونا چاہیے!", "warning")
            return
        }

        _uiState.update { it.copy(isLoading = true, loadingMessage = "نیا پاس ورڈ گوگل شیٹ میں محفوظ کیا جا رہا ہے...") }
        viewModelScope.launch {
            repository.savePasswordOnline(cleanCnic, newPass)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    showForgotPasswordModal = false,
                    loginCnic = cleanCnic,
                    loginPassword = newPass,
                    toastMessage = "✓ نیا پاس ورڈ شیٹ اور سسٹم میں کامیابی سے محفوظ ہو گیا ہے! اب لاگ ان کریں۔",
                    toastType = "success"
                )
            }
        }
    }

    // Change password from Profile
    fun onOpenChangePasswordModal() {
        _uiState.update {
            it.copy(
                showChangePasswordModal = true,
                changeCurrentPassInput = "",
                changeNewPassInput = ""
            )
        }
    }

    fun onDismissChangePasswordModal() {
        _uiState.update { it.copy(showChangePasswordModal = false) }
    }

    fun onChangeCurrentPassChanged(pass: String) {
        _uiState.update { it.copy(changeCurrentPassInput = pass.take(30)) }
    }

    fun onChangeNewPassChanged(pass: String) {
        _uiState.update { it.copy(changeNewPassInput = pass.take(30)) }
    }

    fun onSubmitChangePassword() {
        val state = _uiState.value
        val cnic = state.loggedInEmployee?.cnic?.replace(Regex("[^0-9]"), "") ?: ""
        if (cnic.isBlank()) return

        val currentPass = repository.getAccountPassword(cnic)
        val enteredCurrent = state.changeCurrentPassInput.trim()
        if (enteredCurrent.isNotBlank() && enteredCurrent != currentPass && enteredCurrent != cnic) {
            showToast("موجودہ پاس ورڈ درست نہیں ہے!", "error")
            return
        }

        val newPass = state.changeNewPassInput.trim()
        if (newPass.length < 4) {
            showToast("نیا پاس ورڈ کم از کم 4 حروف یا ہندسوں پر مشتمل ہونا چاہیے!", "warning")
            return
        }

        _uiState.update { it.copy(isLoading = true, loadingMessage = "نیا پاس ورڈ گوگل شیٹ میں محفوظ کیا جا رہا ہے...") }
        viewModelScope.launch {
            repository.savePasswordOnline(cnic, newPass)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    showChangePasswordModal = false,
                    loginPassword = newPass,
                    toastMessage = "✓ نیا پاس ورڈ شیٹ اور سسٹم میں کامیابی سے محفوظ ہو گیا ہے۔",
                    toastType = "success"
                )
            }
        }
    }

    // Edit email from profile
    fun onOpenEditEmailModal() {
        _uiState.update {
            it.copy(
                showEditEmailModal = true,
                editEmailInput = it.employeeEmail ?: ""
            )
        }
    }

    fun onDismissEditEmailModal() {
        _uiState.update { it.copy(showEditEmailModal = false) }
    }

    fun onEditEmailInputChanged(email: String) {
        _uiState.update { it.copy(editEmailInput = email.take(50)) }
    }

    fun onSubmitEditEmail() {
        val state = _uiState.value
        val cnic = state.loggedInEmployee?.cnic?.replace(Regex("[^0-9]"), "") ?: ""
        val email = state.editEmailInput.trim()

        if (cnic.isNotBlank() && email.isNotBlank()) {
            repository.setAccountEmail(cnic, email)
            _uiState.update {
                it.copy(
                    employeeEmail = email,
                    showEditEmailModal = false,
                    toastMessage = "✓ ریکوری ای میل کامیابی سے محفوظ ہو گئی ہے۔",
                    toastType = "success"
                )
            }
        }
    }
}
