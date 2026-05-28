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

    @Test
    fun `label strips backslashes`() {
        assertEquals("path", InputSanitizer.sanitizeLabel("path\\"))
    }

    @Test
    fun `label strips combination of dangerous characters`() {
        assertEquals("testinput", InputSanitizer.sanitizeLabel("test;'\\\"input"))
    }

    @Test
    fun `amount just above zero is valid`() {
        assertNotNull(InputSanitizer.sanitizeAmount("0.01"))
    }

    @Test
    fun `amount with leading dot is valid`() {
        val result = InputSanitizer.sanitizeAmount(".5")
        assertNotNull(result)
        assertEquals(0.5, result!!, 0.0001)
    }

    @Test
    fun `amount with thousands separator is invalid`() {
        assertNull(InputSanitizer.sanitizeAmount("1,000"))
    }

    @Test
    fun `amount with high precision decimal is valid`() {
        val result = InputSanitizer.sanitizeAmount("99.9999")
        assertNotNull(result)
        assertEquals(99.9999, result!!, 0.00001)
    }

    @Test
    fun `amount just above limit is invalid`() {
        assertNull(InputSanitizer.sanitizeAmount("1000000.01"))
    }
}
