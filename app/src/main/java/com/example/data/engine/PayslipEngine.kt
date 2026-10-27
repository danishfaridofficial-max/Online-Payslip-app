package com.example.data.engine

import com.example.data.model.IncomeTaxData
import com.example.data.model.PayItem
import com.example.data.model.PayslipData
import java.util.regex.Pattern

object PayslipEngine {

    // Master Sheet configuration extracted directly from the user's spreadsheet screenshot
    data class MasterSheetRow(
        val month: String,
        val year: String,
        val spreadsheetId: String,
        val status: String,
        val district: String
    )

    val MASTER_SHEET_ROWS = listOf(
        MasterSheetRow("Sep", "2026", "1s2nMWFKNMXKb_0X4nNczCjHnJ79F047poHyrScbCeLw", "Active", "Bannu"),
        MasterSheetRow("Aug", "2026", "1s2nMWFKNMXKb_0X4nNczCjHnJ79F047poHyrScbCeLw", "Active", "Bannu"),
        MasterSheetRow("Jul", "2026", "1ACP8Ww4ktRxy_bkoOo2iL3jRg6ocRImhVsmM7BEHxJk", "Active", "Bannu"),
        MasterSheetRow("Jun", "2026", "1zGbZaGHfztpiX_w4SXD4Q1G-UHV8pHxQBNd8O0q4Bvs", "Active", "Bannu"),
        MasterSheetRow("May", "2026", "1N75jej9__4rXxiY4gdcbd-WbAmPIFEQZHMbCcSKnD8w", "Active", "Bannu")
    )

    data class Candidate(val row: Int, val col: Int)
    data class ParsedCandidate(val block: List<List<String>>, val parsed: PayslipData, val score: Int)

    /**
     * Executes the exact multi-tab dynamic search & merge logic from Code.gs
     */
    fun searchAndParse(
        data: List<List<String>>,
        district: String,
        category: String,
        month: String,
        year: String,
        searchQuery: String
    ): PayslipData? {
        val cleanQuery = searchQuery.replace(Regex("[-\\s]"), "").trim().lowercase()
        if (cleanQuery.isEmpty() || data.isEmpty()) return null

        val candidates = mutableListOf<Candidate>()
        for (i in data.indices) {
            val row = data[i]
            val colA = row.getOrElse(0) { "" }.lowercase().replace(Regex("[-\\s]"), "")
            val colB = row.getOrElse(1) { "" }.lowercase().replace(Regex("[-\\s]"), "")

            if (colA.contains(cleanQuery)) candidates.add(Candidate(i, 0))
            if (colB.contains(cleanQuery)) candidates.add(Candidate(i, 1))
        }

        if (candidates.isEmpty()) return null

        val numRows = data.size
        val allParsed = mutableListOf<ParsedCandidate>()
        var bestScore = -1
        var bestCandidate: PayslipData? = null

        candidates.forEach { cand ->
            val matchRow = cand.row
            val matchCol = cand.col

            // Upward boundary
            var startRow = matchRow
            for (i in matchRow downTo 0) {
                val r = data[i]
                val colA = r.getOrElse(0) { "" }.trim()
                val colB = r.getOrElse(1) { "" }.trim()
                if (colA.startsWith("S#:") || colA.startsWith("Month:") ||
                    colB.startsWith("S#:") || colB.startsWith("Month:")
                ) {
                    startRow = if (i > 0) i - 1 else i
                    break
                }
            }

            // Downward boundary
            val sPattern = Pattern.compile("S#:\\s*\\S+")
            var endRow = numRows - 1
            for (i in (matchRow + 1) until numRows) {
                val r = data[i]
                val colA = r.getOrElse(0) { "" }
                val colB = r.getOrElse(1) { "" }
                if (sPattern.matcher(colA).find() || sPattern.matcher(colB).find()) {
                    endRow = maxOf(i - 2, matchRow)
                    break
                }
            }

            if (endRow - startRow > 100) {
                endRow = minOf(matchRow + 30, numRows - 1)
            }

            val block = mutableListOf<List<String>>()
            for (r in startRow..endRow) {
                block.add(data[r])
            }

            val parsed = parsePayslipBlock(block, matchCol)
            var score = parsed.payItems.size
            if (parsed.payItems.any { it.code == "0001" }) {
                score += 100
            }

            allParsed.add(ParsedCandidate(block, parsed, score))
            if (score > bestScore) {
                bestScore = score
                bestCandidate = parsed
            }
        }

        if (bestCandidate == null) return null

        // Merge wage items across all candidate blocks (Code.gs step 5)
        val sortedCandidates = allParsed.sortedByDescending { it.score }
        val mergedPayItems = mutableMapOf<String, PayItem>()
        val mergedDeductions = mutableMapOf<String, PayItem>()

        sortedCandidates.forEach { c ->
            c.parsed.payItems.forEach { item ->
                if (item.code !in mergedPayItems) {
                    mergedPayItems[item.code] = item
                }
            }
            c.parsed.deductions.forEach { item ->
                if (item.code !in mergedDeductions) {
                    mergedDeductions[item.code] = item
                }
            }
        }

        return bestCandidate.copy(
            payItems = mergedPayItems.values.toList(),
            deductions = mergedDeductions.values.toList(),
            monthYear = if (bestCandidate.monthYear.isEmpty()) "$month-$year" else bestCandidate.monthYear,
            officeSubTitle = if (bestCandidate.officeSubTitle.isEmpty()) "District Accounts Office $district" else bestCandidate.officeSubTitle
        )
    }

    /**
     * Parses the fixed-width text report matching parsePayslipBlock in Code.gs
     */
    fun parsePayslipBlock(rows: List<List<String>>, preferCol: Int?): PayslipData {
        val lines = rows.map { r ->
            val cell = when (preferCol) {
                0, 1 -> r.getOrElse(preferCol) { "" }
                else -> {
                    val b = r.getOrElse(1) { "" }
                    if (b.isNotEmpty()) b else r.getOrElse(0) { "" }
                }
            }
            cell
        }

        var officeTitle = "Dist. Govt. KP-Provincial"
        var officeSubTitle = ""
        var monthYear = ""
        var name = ""
        var personnelNo = ""
        var cnic = ""
        var dob = ""
        var lengthOfService = ""
        var employmentCategory = ""
        var designation = ""
        var ddoTitle = ""
        var ddoCode = ""
        var payrollSection = ""
        var gpfSection = ""
        var gpfAccountNo = ""
        var gpfInterestApplied = ""
        var gpfBalance = ""
        var payScale = ""
        var payScaleType = ""
        var bps = ""
        val payItems = mutableListOf<PayItem>()
        val deductions = mutableListOf<PayItem>()
        var incomeTax = IncomeTaxData()
        var grossPay = ""
        var totalDeductions = ""
        var netPay = ""
        var accountNo = ""
        var bankDetails = ""

        var section = "" // "PAY" or "DED"

        for (i in lines.indices) {
            val line = lines[i]
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            // District/Office header
            if (i == 0 || (officeSubTitle.isEmpty() && !trimmed.contains(":") && !trimmed.contains("#") && trimmed.split(" ").size <= 3 && section.isEmpty())) {
                officeSubTitle = "District Accounts Office $trimmed"
                continue
            }

            // S#:19086   P Sec:001  Month:May 2026
            val sSecMatcher = Pattern.compile("S#:\\s*(\\S+)\\s+P Sec:\\s*(\\S+)\\s+Month:\\s*(.+)", Pattern.CASE_INSENSITIVE).matcher(line)
            if (sSecMatcher.find()) {
                payrollSection = sSecMatcher.group(2) ?: ""
                gpfSection = sSecMatcher.group(2) ?: ""
                monthYear = (sSecMatcher.group(3) ?: "").trim().replace(Regex("\\s+"), "-")
                continue
            }

            // Section headers
            if (trimmed.startsWith("PAYS AND ALLOWANCES:", ignoreCase = true)) {
                section = "PAY"
                continue
            }
            if (trimmed.startsWith("DEDUCTIONS:", ignoreCase = true)) {
                section = "DED"
                continue
            }

            // DDO line
            val ddoMatcher = Pattern.compile("^\\s*([A-Z0-9]+)\\s*-\\s*(.+)").matcher(line)
            if (section.isEmpty() && ddoCode.isEmpty() && ddoMatcher.find()) {
                ddoCode = ddoMatcher.group(1) ?: ""
                ddoTitle = (ddoMatcher.group(2) ?: "").trim()
                continue
            }

            // Personnel No
            val persMatcher = Pattern.compile("Pers\\s*#:\\s*(\\S*)", Pattern.CASE_INSENSITIVE).matcher(line)
            if (persMatcher.find()) {
                personnelNo = persMatcher.group(1) ?: ""
                continue
            }

            // Name & NTN
            val nameMatcher = Pattern.compile("Name:\\s*(.*?)\\s{2,}NTN:\\s*(\\S*)", Pattern.CASE_INSENSITIVE).matcher(line)
            if (nameMatcher.find()) {
                name = (nameMatcher.group(1) ?: "").trim()
                continue
            }

            // Designation & GPF #
            val desigMatcher = Pattern.compile("^(.+?)\\s{2,}GPF\\s*#:\\s*(.+)", Pattern.CASE_INSENSITIVE).matcher(line)
            if (desigMatcher.find()) {
                designation = (desigMatcher.group(1) ?: "").trim()
                gpfAccountNo = (desigMatcher.group(2) ?: "").trim()
                continue
            }

            // CNIC
            val cnicMatcher = Pattern.compile("CNIC No\\.?\\s*(\\d+)", Pattern.CASE_INSENSITIVE).matcher(line)
            if (cnicMatcher.find()) {
                cnic = cnicMatcher.group(1) ?: ""
                continue
            }

            if (trimmed.contains("GPF Interest Applied", ignoreCase = true)) {
                gpfInterestApplied = "Applied"
                continue
            }

            // BPS & Category
            val bpsMatcher = Pattern.compile("^\\s*(\\d{1,2})\\s+(Active\\s+\\S+)", Pattern.CASE_INSENSITIVE).matcher(line)
            if (bpsMatcher.find()) {
                bps = bpsMatcher.group(1) ?: ""
                payScale = "BPS For - 2026"
                payScaleType = "Civil"
                employmentCategory = (bpsMatcher.group(2) ?: "").trim()
                continue
            }

            // Gross Pay
            val grossMatcher = Pattern.compile("Gross Pay and Allowances\\s+([\\d,]+\\.\\d{2})", Pattern.CASE_INSENSITIVE).matcher(line)
            if (grossMatcher.find()) {
                grossPay = grossMatcher.group(1) ?: ""
                continue
            }

            // Income Tax line
            val itMatcher = Pattern.compile("IT Payable\\s+([\\d,]+\\.\\d{2})\\s+Deducted\\s+([\\d,]+\\.\\d{2})(?:\\s+TAX:\\((\\d+)\\)\\s+([\\d,]+\\.\\d{2}))?", Pattern.CASE_INSENSITIVE).matcher(line)
            if (itMatcher.find()) {
                val rec = itMatcher.group(1) ?: ""
                val recTill = itMatcher.group(2) ?: ""
                incomeTax = incomeTax.copy(recoverable = rec, recoveredTill = recTill)
                val taxCode = itMatcher.group(3)
                val taxAmount = itMatcher.group(4)
                if (!taxCode.isNullOrEmpty() && !taxAmount.isNullOrEmpty()) {
                    deductions.add(PayItem(taxCode, "Income Tax", taxAmount))
                }
                continue
            }

            // GPF Balance
            val gpfMatcher = Pattern.compile("GPF Balance\\s+([\\d,]+\\.\\d{2})\\s+DCPS Balanc\\s+([\\d,]+\\.\\d{2})\\s+Subrc:\\s*([\\d,]+\\.\\d{2})?", Pattern.CASE_INSENSITIVE).matcher(line)
            if (gpfMatcher.find()) {
                gpfBalance = gpfMatcher.group(1) ?: ""
                val subrc = gpfMatcher.group(3)
                if (!subrc.isNullOrEmpty()) {
                    deductions.add(PayItem("3012", "GPF Subscription", subrc))
                }
                continue
            }

            // Total Deductions
            val totDedMatcher = Pattern.compile("Total Deductions\\s+([\\d,]+\\.\\d{2})", Pattern.CASE_INSENSITIVE).matcher(line)
            if (totDedMatcher.find()) {
                totalDeductions = totDedMatcher.group(1) ?: ""
                continue
            }

            // Wage items (0001-Basic Pay ...)
            val wageMatcher = Pattern.compile("^(\\d{3,4})-(.+?)\\s{2,}([\\d,]+\\.\\d{2})\\s*$").matcher(trimmed)
            if (wageMatcher.find()) {
                val code = wageMatcher.group(1) ?: ""
                val label = (wageMatcher.group(2) ?: "").trim()
                val amount = wageMatcher.group(3) ?: ""
                val item = PayItem(code, label, amount)
                if (section == "PAY") payItems.add(item)
                else if (section == "DED") deductions.add(item)
                continue
            }

            // DOB & Bank
            val dobMatcher = Pattern.compile("^\\s*(\\d{2}\\.\\d{2}\\.\\d{4})\\s+(.+)").matcher(line)
            if (dobMatcher.find()) {
                dob = dobMatcher.group(1) ?: ""
                bankDetails = (dobMatcher.group(2) ?: "").trim()
                continue
            }

            // Length of service & account number
            val tenureMatcher = Pattern.compile("^\\s*(\\d+\\s+Years?\\s+\\d+\\s+Months?\\s+\\d+\\s+Days?)\\s+(\\d+)", Pattern.CASE_INSENSITIVE).matcher(line)
            if (tenureMatcher.find()) {
                lengthOfService = tenureMatcher.group(1) ?: ""
                accountNo = tenureMatcher.group(2) ?: ""
                continue
            }

            // Account number standalone fallback
            if (accountNo.isEmpty()) {
                val accMatcher = Pattern.compile("^(\\d{8,20})$").matcher(trimmed)
                if (accMatcher.find()) {
                    accountNo = accMatcher.group(1) ?: ""
                    continue
                }
            }

            // Standalone net pay number
            if (netPay.isEmpty() && Pattern.compile("^[\\d,]+(\\.\\d{2})?$").matcher(trimmed).matches()) {
                netPay = trimmed
                continue
            }
        }

        return PayslipData(
            officeTitle = officeTitle,
            officeSubTitle = officeSubTitle,
            monthYear = monthYear,
            name = name,
            personnelNo = personnelNo,
            cnic = cnic,
            dob = dob,
            lengthOfService = lengthOfService,
            employmentCategory = employmentCategory,
            designation = designation,
            ddoTitle = ddoTitle,
            ddoCode = ddoCode,
            payrollSection = payrollSection,
            gpfSection = gpfSection,
            gpfAccountNo = gpfAccountNo,
            gpfInterestApplied = gpfInterestApplied,
            gpfBalance = gpfBalance,
            payScale = payScale,
            payScaleType = payScaleType,
            bps = bps,
            payItems = payItems,
            deductions = deductions,
            incomeTax = incomeTax,
            grossPay = grossPay,
            totalDeductions = totalDeductions,
            netPay = netPay,
            payeeName = name,
            accountNo = accountNo,
            bankDetails = bankDetails
        )
    }

    /**
     * Default realistic fallback data modeled on the exact Code.gs test record
     * (Babar Ali / Primary School Teacher / Bannu) so testing is instant and reliable.
     */
    fun getSamplePayslip(
        district: String = "Bannu",
        category: String = "District",
        month: String = "Sep",
        year: String = "2026",
        query: String = "1110159744485"
    ): PayslipData {
        val displayMonthYear = if (month.equals("Sep", ignoreCase = true)) "September-$year" else "$month-$year"
        return PayslipData(
            officeTitle = "Dist. Govt. KP-Provincial",
            officeSubTitle = "District Accounts Office $district",
            monthYear = displayMonthYear,
            name = "BABAR ALI",
            personnelNo = "00913211",
            cnic = if (query.length >= 13) query else "1110159744485",
            dob = "27.10.1993",
            lengthOfService = "07 Years 04 Months 007 Days",
            employmentCategory = "Active Temporary",
            designation = "PRIMARY SCHOOL TEACHER",
            ddoTitle = "Government Primary Schools",
            ddoCode = "BU6073",
            payrollSection = "001",
            gpfSection = "001",
            gpfAccountNo = "CP V 26 P 175",
            gpfInterestApplied = "Applied",
            gpfBalance = "86,362.00",
            payScale = "BPS For - 2026",
            payScaleType = "Civil",
            bps = "12",
            payItems = listOf(
                PayItem("1001", "House Rent Allowance 45%", "2,940.00"),
                PayItem("1210", "Convey Allowance  2005", "4,284.00"),
                PayItem("1300", "Medical Allowance", "1,500.00"),
                PayItem("2316", "Teaching Allowance 2021", "2,664.00"),
                PayItem("2341", "Dispr. Red All 15% 2022KP", "2,430.00"),
                PayItem("2378", "Adhoc Relief All 2023 35%", "8,921.00"),
                PayItem("2393", "Adhoc Relief All 2024 25%", "7,445.00"),
                PayItem("2431", "Dispar. Red. All-15%-2025", "2,965.00"),
                PayItem("2456", "Adhoc Relief 2026 (7%)", "2,505.00"),
                PayItem("0001", "Basic Pay", "35,790.00")
            ),
            deductions = listOf(
                PayItem("3012", "GPF Subscription", "3,300.00"),
                PayItem("3609", "Income Tax", "215.00")
            ),
            incomeTax = IncomeTaxData(
                payable = "1,929.06",
                recoveredTill = "645.00",
                exempted = "0.00",
                recoverable = "1,929.06"
            ),
            grossPay = "71,444.00",
            totalDeductions = "15,450.00",
            netPay = "55994",
            payeeName = "BABAR ALI",
            accountNo = "0082010102544609",
            bankDetails = "MEEZAN BANK LIMITED HOSPITAL ROAD BANNU",
            footerNote1 = "System generated document",
            footerNote2 = "* Verified by District Accounts Office $district"
        )
    }
}
