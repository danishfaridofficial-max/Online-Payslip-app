package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.PayslipData
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

object PdfUtil {

    fun formatAmount(valStr: String?): String {
        if (valStr.isNullOrBlank()) return ""
        val clean = valStr.replace(",", "").trim()
        val num = clean.toDoubleOrNull() ?: return valStr
        val nf = NumberFormat.getNumberInstance(Locale.US)
        nf.minimumFractionDigits = 2
        nf.maximumFractionDigits = 2
        return nf.format(num)
    }

    /**
     * Generates a high-resolution, print-ready PDF matching the official District Accounts Office salary statement.
     */
    fun generatePayslipPdf(context: Context, data: PayslipData): File? {
        val pageWidth = 595 // A4 standard width in points
        val pageHeight = 842 // A4 standard height in points
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }
        val paintBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val paintHeaderTitle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 12.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val paintSubTitle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val paintSectionHeader = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val paintLine = Paint().apply {
            color = Color.BLACK
            strokeWidth = 0.9f
            style = Paint.Style.STROKE
        }
        val paintThickLine = Paint().apply {
            color = Color.BLACK
            strokeWidth = 1.3f
            style = Paint.Style.STROKE
        }
        val paintTableHeaderBg = Paint().apply {
            color = Color.rgb(243, 244, 246)
            style = Paint.Style.FILL
        }
        val paintOuterBorder = Paint().apply {
            color = Color.rgb(220, 224, 230) // light gray outer sheet frame
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        // 1. Draw outer page frame (matching screenshot)
        val frameL = 34f
        val frameT = 40f
        val frameR = 561f
        val frameB = 802f
        canvas.drawRect(frameL, frameT, frameR, frameB, paintOuterBorder)

        val margin = 52f
        var y = 68f
        val contentWidth = pageWidth - (margin * 2)

        // 2. Header
        canvas.drawText(data.officeTitle.ifEmpty { "Dist. Govt. KP-Provincial" }, pageWidth / 2f, y, paintHeaderTitle)
        y += 16f
        canvas.drawText(data.officeSubTitle.ifEmpty { "District Accounts Office Bannu" }, pageWidth / 2f, y, paintSubTitle)
        y += 15f
        canvas.drawText("Monthly Salary Statement (${data.monthYear})", pageWidth / 2f, y, paintSubTitle)
        y += 14f
        canvas.drawLine(margin, y, pageWidth - margin, y, paintThickLine)
        y += 18f

        // 3. Personal Information Section
        val persHeader = "Personal Information of ${data.name}" +
                if (!data.guardianName.isNullOrEmpty()) " ${data.guardianRelation ?: "s/o"} ${data.guardianName}" else ""
        canvas.drawText(persHeader, margin, y, paintSectionHeader)
        y += 16f

        // Info Table 1
        canvas.drawText("Personnel Number:", margin, y, paintText)
        canvas.drawText(data.personnelNo, margin + 125f, y, paintBold)
        canvas.drawText("CNIC:", margin + 255f, y, paintText)
        canvas.drawText(data.cnic, margin + 310f, y, paintBold)
        y += 15f

        canvas.drawText("Date of Birth:", margin, y, paintText)
        canvas.drawText(data.dob, margin + 125f, y, paintBold)
        canvas.drawText("Length of Service:", margin + 255f, y, paintText)
        canvas.drawText(data.lengthOfService, margin + 375f, y, paintBold)
        y += 20f

        // 4. Employment Category
        canvas.drawText("Employment Category: ${data.employmentCategory}", margin, y, paintSectionHeader)
        y += 15f

        canvas.drawText("Designation:", margin, y, paintText)
        canvas.drawText(data.designation, margin + 85f, y, paintBold)
        canvas.drawText("DDO:", margin + 275f, y, paintText)
        canvas.drawText(data.ddoTitle, margin + 325f, y, paintBold)
        y += 15f

        canvas.drawText("DDO Code:", margin, y, paintText)
        canvas.drawText(data.ddoCode, margin + 85f, y, paintBold)
        y += 15f

        canvas.drawText("Payroll Section:", margin, y, paintText)
        canvas.drawText(data.payrollSection, margin + 85f, y, paintBold)
        canvas.drawText("GPF Section:", margin + 205f, y, paintText)
        canvas.drawText(data.gpfSection, margin + 275f, y, paintBold)
        y += 15f

        canvas.drawText("GPF A/C No:", margin, y, paintText)
        canvas.drawText(data.gpfAccountNo, margin + 85f, y, paintBold)
        canvas.drawText("GPF Interest:", margin + 185f, y, paintText)
        canvas.drawText(data.gpfInterestApplied, margin + 255f, y, paintBold)
        canvas.drawText("GPF Balance:", margin + 325f, y, paintText)
        canvas.drawText(formatAmount(data.gpfBalance), margin + 400f, y, paintBold)
        y += 20f

        // 5. Pay and Allowances Header
        val scaleInfo = "Pay and Allowances: Pay scale: ${data.payScale} Pay Scale Type: ${data.payScaleType} BPS: ${data.bps} Pay Stage:"
        canvas.drawText(scaleInfo, margin, y, paintSectionHeader)
        y += 14f

        // 6. Allowances Table with Full Vertical & Horizontal Grid
        val colW = contentWidth / 2f
        val codeW = 38f
        val amountW = 68f
        val labelW = colW - codeW - amountW
        val rowH = 17f

        // Table Header: "Wage type" on both sides
        val headerTop = y - 11f
        val headerBottom = y + 6f
        canvas.drawRect(margin, headerTop, pageWidth - margin, headerBottom, paintTableHeaderBg)
        canvas.drawRect(margin, headerTop, pageWidth - margin, headerBottom, paintLine)
        canvas.drawLine(margin + colW, headerTop, margin + colW, headerBottom, paintLine)
        canvas.drawText("Wage type", margin + 6f, y + 2f, paintBold)
        canvas.drawText("Wage type", margin + colW + 6f, y + 2f, paintBold)
        y += rowH

        val payItems = data.payItems
        val totalAllowances = payItems.size
        val leftCount = (totalAllowances + 1) / 2
        val maxAllowRows = maxOf(leftCount, 1)

        for (rowIndex in 0 until maxAllowRows) {
            val leftItem = payItems.getOrNull(rowIndex)
            val rightItem = payItems.getOrNull(rowIndex + leftCount)

            val rTop = y - 11f
            val rBottom = y + 6f

            // Outer row box
            canvas.drawRect(margin, rTop, pageWidth - margin, rBottom, paintLine)

            // Vertical line dividing Left & Right half
            canvas.drawLine(margin + colW, rTop, margin + colW, rBottom, paintLine)

            // Left side vertical dividers: Code | Label | Amount
            canvas.drawLine(margin + codeW, rTop, margin + codeW, rBottom, paintLine)
            canvas.drawLine(margin + codeW + labelW, rTop, margin + codeW + labelW, rBottom, paintLine)

            // Right side vertical dividers: Code | Label | Amount
            canvas.drawLine(margin + colW + codeW, rTop, margin + colW + codeW, rBottom, paintLine)
            canvas.drawLine(margin + colW + codeW + labelW, rTop, margin + colW + codeW + labelW, rBottom, paintLine)

            // Left content
            if (leftItem != null) {
                canvas.drawText(leftItem.code, margin + 4f, y + 2f, paintText)
                canvas.drawText(leftItem.label.take(24), margin + codeW + 4f, y + 2f, paintText)
                val amtL = formatAmount(leftItem.amount)
                canvas.drawText(amtL, margin + colW - paintText.measureText(amtL) - 4f, y + 2f, paintText)
            }

            // Right content
            if (rightItem != null) {
                canvas.drawText(rightItem.code, margin + colW + 4f, y + 2f, paintText)
                canvas.drawText(rightItem.label.take(24), margin + colW + codeW + 4f, y + 2f, paintText)
                val amtR = formatAmount(rightItem.amount)
                canvas.drawText(amtR, pageWidth - margin - paintText.measureText(amtR) - 4f, y + 2f, paintText)
            }

            y += rowH
        }

        y += 8f

        // 7. Deductions - General Section (No header row, direct data grid matching screenshot)
        canvas.drawText("Deductions - General", margin, y, paintSectionHeader)
        y += 14f

        val dedItems = data.deductions
        val totalDeds = dedItems.size
        val leftDedCount = (totalDeds + 1) / 2
        val maxDedRows = maxOf(leftDedCount, 1)

        for (rowIndex in 0 until maxDedRows) {
            val leftItem = dedItems.getOrNull(rowIndex)
            val rightItem = dedItems.getOrNull(rowIndex + leftDedCount)

            val rTop = y - 11f
            val rBottom = y + 6f

            // Outer row box
            canvas.drawRect(margin, rTop, pageWidth - margin, rBottom, paintLine)

            // Vertical line dividing Left & Right half
            canvas.drawLine(margin + colW, rTop, margin + colW, rBottom, paintLine)

            // Left side vertical dividers: Code | Label | Amount
            canvas.drawLine(margin + codeW, rTop, margin + codeW, rBottom, paintLine)
            canvas.drawLine(margin + codeW + labelW, rTop, margin + codeW + labelW, rBottom, paintLine)

            // Right side vertical dividers: Code | Label | Amount
            canvas.drawLine(margin + colW + codeW, rTop, margin + colW + codeW, rBottom, paintLine)
            canvas.drawLine(margin + colW + codeW + labelW, rTop, margin + colW + codeW + labelW, rBottom, paintLine)

            // Left content
            if (leftItem != null) {
                canvas.drawText(leftItem.code, margin + 4f, y + 2f, paintText)
                canvas.drawText(leftItem.label.take(24), margin + codeW + 4f, y + 2f, paintText)
                val amtL = formatAmount(leftItem.amount)
                canvas.drawText(amtL, margin + colW - paintText.measureText(amtL) - 4f, y + 2f, paintText)
            }

            // Right content
            if (rightItem != null) {
                canvas.drawText(rightItem.code, margin + colW + 4f, y + 2f, paintText)
                canvas.drawText(rightItem.label.take(24), margin + colW + codeW + 4f, y + 2f, paintText)
                val amtR = formatAmount(rightItem.amount)
                canvas.drawText(amtR, pageWidth - margin - paintText.measureText(amtR) - 4f, y + 2f, paintText)
            }

            y += rowH
        }

        y += 8f

        // 8. Deductions - Income Tax
        canvas.drawText("Deductions - Income Tax", margin, y, paintSectionHeader)
        y += 15f
        canvas.drawText("Payable:", margin, y, paintText)
        canvas.drawText("Recovered:", margin + 80f, y, paintText)
        val recVal = formatAmount(data.incomeTax.recoveredTill)
        canvas.drawText(recVal, margin + 190f - paintText.measureText(recVal), y, paintText)

        canvas.drawText("Exempted:", margin + 220f, y, paintText)
        canvas.drawText("Recoverable:", margin + 310f, y, paintText)
        val recovVal = formatAmount(data.incomeTax.recoverable)
        canvas.drawText(recovVal, margin + 430f - paintText.measureText(recovVal), y, paintText)
        y += 18f

        // 9. Summary Box (Gross Pay, Deductions, Net Pay) with clear borders & right-aligned values
        val boxH = 26f
        canvas.drawRect(margin, y, pageWidth - margin, y + boxH, paintLine)
        val third = contentWidth / 3f
        canvas.drawLine(margin + third, y, margin + third, y + boxH, paintLine)
        canvas.drawLine(margin + (third * 2), y, margin + (third * 2), y + boxH, paintLine)

        val midBoxY = y + 17f
        // Section 1: Gross Pay
        canvas.drawText("Gross Pay (Rs.):", margin + 8f, midBoxY, paintBold)
        val grossVal = formatAmount(data.grossPay)
        canvas.drawText(grossVal, margin + third - paintBold.measureText(grossVal) - 8f, midBoxY, paintBold)

        // Section 2: Deductions
        canvas.drawText("Deductions (Rs.):", margin + third + 8f, midBoxY, paintBold)
        val dedVal = formatAmount(data.totalDeductions)
        canvas.drawText(dedVal, margin + (third * 2) - paintBold.measureText(dedVal) - 8f, midBoxY, paintBold)

        // Section 3: Net Pay
        canvas.drawText("Net Pay (Rs.):", margin + (third * 2) + 8f, midBoxY, paintBold)
        val netVal = formatAmount(data.netPay)
        canvas.drawText(netVal, pageWidth - margin - paintBold.measureText(netVal) - 8f, midBoxY, paintBold)

        y += boxH + 18f

        // 10. Bank Info
        canvas.drawText("Payee Name:", margin, y, paintText)
        canvas.drawText(data.payeeName.ifEmpty { data.name }, margin + 110f, y, paintBold)
        y += 15f
        canvas.drawText("Account Number:", margin, y, paintText)
        canvas.drawText(data.accountNo, margin + 110f, y, paintBold)
        y += 15f
        canvas.drawText("Bank Details:", margin, y, paintText)
        canvas.drawText(data.bankDetails, margin + 110f, y, paintBold)
        y += 26f

        // 11. Footer (Italic, matching screenshot)
        val paintFooter = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        }
        canvas.drawText(data.footerNote1.ifEmpty { "System generated document" }, margin, y, paintFooter)
        y += 12f
        canvas.drawText("* All amounts are in Pak Rupees", margin, y, paintFooter)
        if (data.footerNote2.isNotEmpty()) {
            y += 12f
            canvas.drawText(data.footerNote2, margin, y, paintFooter)
        }

        doc.finishPage(page)

        // Save PDF to public Downloads folder so it persists on user device
        return try {
            val fileName = "PaySlip-${data.personnelNo.ifEmpty { data.cnic }}-${data.monthYear.replace("/", "-")}.pdf"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        doc.writeTo(out)
                    }
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val targetFile = File(downloadsDir, fileName)
                FileOutputStream(targetFile).use { out ->
                    doc.writeTo(out)
                }
            }

            // Also keep in cache for viewing/sharing
            val cacheFile = File(context.cacheDir, fileName)
            FileOutputStream(cacheFile).use { out ->
                doc.writeTo(out)
            }
            doc.close()
            cacheFile
        } catch (e: Exception) {
            e.printStackTrace()
            doc.close()
            null
        }
    }

    /**
     * Opens generated PDF in a PDF viewer app
     */
    fun openPdf(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "پی ڈی ایف کھولیں"))
        } catch (e: Exception) {
            Toast.makeText(context, "پی ڈی ایف کھولنے کے لیے کوئی ایپ موجود نہیں", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shares generated PDF via WhatsApp, Gmail, Drive, etc.
     */
    fun sharePdf(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Government Pay Slip")
                putExtra(Intent.EXTRA_TEXT, "Here is the official monthly salary statement.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Pay Slip PDF"))
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
