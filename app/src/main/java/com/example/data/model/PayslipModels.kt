package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PayItem(
    val code: String = "",
    val label: String = "",
    val amount: String = ""
)

@JsonClass(generateAdapter = true)
data class LoanItem(
    val loanNo: String = "",
    val description: String = "",
    val principal: String = "",
    val deduction: String = "",
    val balance: String = ""
)

@JsonClass(generateAdapter = true)
data class IncomeTaxData(
    val payable: String = "",
    val recoveredTill: String = "",
    val exempted: String = "",
    val recoverable: String = ""
)

@JsonClass(generateAdapter = true)
data class PayslipData(
    val officeTitle: String = "Dist. Govt. KP-Provincial",
    val officeSubTitle: String = "District Accounts Office Bannu",
    val monthYear: String = "",
    val name: String = "",
    val guardianName: String? = null,
    val guardianRelation: String? = null,
    val personnelNo: String = "",
    val cnic: String = "",
    val dob: String = "",
    val lengthOfService: String = "",
    val employmentCategory: String = "",
    val designation: String = "",
    val ddoTitle: String = "",
    val ddoCode: String = "",
    val payrollSection: String = "",
    val gpfSection: String = "",
    val gpfAccountNo: String = "",
    val gpfInterestApplied: String = "",
    val gpfBalance: String = "",
    val payScale: String = "",
    val payScaleType: String = "",
    val bps: String = "",
    val payStage: String? = null,
    val payItems: List<PayItem> = emptyList(),
    val deductions: List<PayItem> = emptyList(),
    val loans: List<LoanItem> = emptyList(),
    val incomeTax: IncomeTaxData = IncomeTaxData(),
    val grossPay: String = "",
    val totalDeductions: String = "",
    val netPay: String = "",
    val payeeName: String = "",
    val accountNo: String = "",
    val bankDetails: String = "",
    val footerNote1: String = "System generated document",
    val footerNote2: String = ""
)

@JsonClass(generateAdapter = true)
data class PayslipResponse(
    val success: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val category: String? = null,
    val month: String? = null,
    val year: String? = null,
    val data: PayslipData? = null
)

@JsonClass(generateAdapter = true)
data class DistrictsResponse(
    val success: Boolean = false,
    val districts: List<String> = emptyList(),
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class TrafficResponse(
    val success: Boolean = false,
    val userId: String? = null,
    val isApp: Boolean? = null,
    val appLiveCount: Int? = null,
    val app_live_count: Int? = null,
    val appTotalCount: Long? = null,
    val app_total_count: Long? = null,
    val liveCount: Int? = null,
    val live_count: Int? = null,
    val onlineCount: Int? = null,
    val online_count: Int? = null,
    val live: Int? = null,
    val online: Int? = null,
    val totalCount: Long? = null,
    val total_count: Long? = null,
    val totalVisits: Long? = null,
    val total_visits: Long? = null,
    val total: Long? = null,
    val visits: Long? = null,
    val count: Long? = null,
    val error: String? = null
) {
    val isAppTraffic: Boolean
        get() = isApp == true || appLiveCount != null || appTotalCount != null || app_live_count != null || app_total_count != null

    val actualAppLiveCount: Int?
        get() = appLiveCount ?: app_live_count

    val actualAppTotalCount: Long?
        get() = appTotalCount ?: app_total_count

    val actualLiveCount: Int
        get() = appLiveCount ?: app_live_count ?: liveCount ?: live_count ?: onlineCount ?: online_count ?: live ?: online ?: 1

    val actualTotalCount: Long
        get() = appTotalCount ?: app_total_count ?: totalCount ?: total_count ?: totalVisits ?: total_visits ?: total ?: visits ?: count ?: 0L
}

@JsonClass(generateAdapter = true)
data class BannerResponse(
    val success: Boolean = false,
    val bannerHeading: String? = null,
    val bannerText: String? = null,
    val bannerLink: String? = null,
    val popupStatus: String? = null,
    val popupHeading: String? = null,
    val popupText: String? = null,
    val popupLink: String? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class PasswordResponse(
    val success: Boolean = false,
    val match: Boolean? = null,
    val isDefault: Boolean? = null,
    val message: String? = null,
    val error: String? = null
)

data class SavedPayslipRecord(
    val id: String,
    val district: String,
    val category: String,
    val month: String,
    val year: String,
    val query: String,
    val timestamp: Long,
    val data: PayslipData
)
