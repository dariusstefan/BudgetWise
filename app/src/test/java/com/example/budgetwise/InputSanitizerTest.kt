package com.example.budgetwise

import com.example.budgetwise.util.InputSanitizer
import org.junit.Assert.*
import org.junit.Test

class InputSanitizerTest {

    // --- sanitizeAmount ---

    @Test
    fun `valid amount returns parsed double`() {
        assertEquals(100.0, InputSanitizer.sanitizeAmount("100.0")!!, 0.0001)
    }

    @Test
    fun `amount with whitespace is trimmed and parsed`() {
        assertEquals(42.5, InputSanitizer.sanitizeAmount("  42.5  ")!!, 0.0001)
    }

    @Test
    fun `empty amount returns null`() {
        assertNull(InputSanitizer.sanitizeAmount(""))
    }

    @Test
    fun `non-numeric amount returns null`() {
        assertNull(InputSanitizer.sanitizeAmount("abc"))
    }

    @Test
    fun `zero amount returns null`() {
        assertNull(InputSanitizer.sanitizeAmount("0"))
    }

    @Test
    fun `negative amount returns null`() {
        assertNull(InputSanitizer.sanitizeAmount("-50"))
    }

    @Test
    fun `amount above limit returns null`() {
        assertNull(InputSanitizer.sanitizeAmount("2000000"))
    }

    @Test
    fun `amount at upper boundary is valid`() {
        assertNotNull(InputSanitizer.sanitizeAmount("1000000"))
    }

    // --- sanitizeLabel ---

    @Test
    fun `label is trimmed`() {
        assertEquals("Rent", InputSanitizer.sanitizeLabel("  Rent  "))
    }

    @Test
    fun `label strips single quotes`() {
        assertEquals("Robert DROP TABLE", InputSanitizer.sanitizeLabel("Robert'; DROP TABLE"))
    }

    @Test
    fun `label strips double quotes`() {
        assertEquals("test", InputSanitizer.sanitizeLabel("te\"st"))
    }

    @Test
    fun `label strips semicolons`() {
        assertEquals("DROP TABLE", InputSanitizer.sanitizeLabel("DROP TABLE;"))
    }

    @Test
    fun `label is limited to 100 characters`() {
        val long = "a".repeat(150)
        assertEquals(100, InputSanitizer.sanitizeLabel(long).length)
    }

    @Test
    fun `normal label is unchanged`() {
        assertEquals("Monthly Salary", InputSanitizer.sanitizeLabel("Monthly Salary"))
    }

    // --- isValidLabel ---

    @Test
    fun `non-empty label is valid`() {
        assertTrue(InputSanitizer.isValidLabel("Rent"))
    }

    @Test
    fun `empty label is invalid`() {
        assertFalse(InputSanitizer.isValidLabel(""))
    }

    @Test
    fun `whitespace-only label is invalid`() {
        assertFalse(InputSanitizer.isValidLabel("   "))
    }

    @Test
    fun `label that becomes empty after sanitization is invalid`() {
        assertFalse(InputSanitizer.isValidLabel(";;;"))
    }
}
