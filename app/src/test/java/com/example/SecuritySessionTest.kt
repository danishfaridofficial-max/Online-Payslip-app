package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.PayslipRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SecuritySessionTest {

    @Test
    fun `searchPayslip blocks unauthenticated requests`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = PayslipRepository(context)
        repo.clearSession()

        val result = repo.searchPayslip(
            district = "Bannu",
            category = "District",
            month = "Sep",
            year = "2026",
            query = "1110159744485"
        )

        assertFalse(result.success)
        assertNull(result.data)
        assertFalse(repo.isSessionAuthorizedFor("1110159744485"))
    }

    @Test
    fun `session authorizes only verified CNIC and blocks cross-CNIC query`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = PayslipRepository(context)
        repo.clearSession()

        repo.setAccountPassword("1110159744485", "MySecret#123")
        val verified = repo.verifyPasswordOnline("1110159744485", "MySecret#123")
        assertTrue(verified)
        assertEquals("1110159744485", repo.getActiveSessionCnic())
        assertTrue(repo.isSessionAuthorizedFor("11101-5974448-5"))

        // Cross-CNIC query must be rejected immediately
        val crossResult = repo.searchPayslip(
            district = "Bannu",
            category = "District",
            month = "Sep",
            year = "2026",
            query = "1110199999999"
        )
        assertFalse(crossResult.success)
        assertNull(crossResult.data)

        // Logout clears session completely
        repo.clearSession()
        assertNull(repo.getActiveSessionCnic())
        assertFalse(repo.isSessionAuthorizedFor("1110159744485"))
    }
}
