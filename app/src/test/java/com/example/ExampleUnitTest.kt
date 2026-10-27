package com.example

import com.example.data.engine.PayslipEngine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testPayslipEngineSampleData() {
    val payslip = PayslipEngine.getSamplePayslip(
      district = "Bannu",
      category = "District",
      month = "May",
      year = "2026",
      query = "1110159744485"
    )

    assertEquals("Dist. Govt. KP-Provincial", payslip.officeTitle)
    assertEquals("District Accounts Office Bannu", payslip.officeSubTitle)
    assertEquals("BABAR ALI", payslip.name)
    assertEquals("00913211", payslip.personnelNo)
    assertEquals("1110159744485", payslip.cnic)
    assertEquals("71,444.00", payslip.grossPay)
    assertEquals("15,450.00", payslip.totalDeductions)
    assertEquals("55994", payslip.netPay)
    assertTrue(payslip.payItems.any { it.code == "0001" && it.label == "Basic Pay" })
    assertTrue(payslip.deductions.any { it.code == "3012" && it.label == "GPF Subscription" })
  }

  @Test
  fun testMasterSheetRowsLoaded() {
    val rows = PayslipEngine.MASTER_SHEET_ROWS
    assertTrue(rows.isNotEmpty())
    assertTrue(rows.any { it.month == "May" && it.district == "Bannu" })
  }
}

