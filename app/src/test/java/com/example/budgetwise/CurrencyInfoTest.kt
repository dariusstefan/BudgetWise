package com.example.budgetwise

import com.example.budgetwise.util.CurrencyInfo
import com.example.budgetwise.util.EUR_DEFAULT
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyInfoTest {

    @Test
    fun `EUR symbol is euro sign`() {
        assertEquals("€", CurrencyInfo("EUR", 1.0).symbol)
    }

    @Test
    fun `GBP symbol is pound sign`() {
        assertEquals("£", CurrencyInfo("GBP", 0.85).symbol)
    }

    @Test
    fun `RON symbol is RON with trailing space`() {
        assertEquals("RON ", CurrencyInfo("RON", 5.0).symbol)
    }

    @Test
    fun `USD symbol falls back to dollar sign`() {
        assertEquals("$", CurrencyInfo("USD", 1.08).symbol)
    }

    @Test
    fun `unknown currency code falls back to dollar sign`() {
        assertEquals("$", CurrencyInfo("JPY", 160.0).symbol)
        assertEquals("$", CurrencyInfo("CHF", 0.95).symbol)
        assertEquals("$", CurrencyInfo("", 1.0).symbol)
    }

    @Test
    fun `EUR_DEFAULT has code EUR and rate 1`() {
        assertEquals("EUR", EUR_DEFAULT.code)
        assertEquals(1.0, EUR_DEFAULT.rate, 0.0001)
    }

    @Test
    fun `EUR_DEFAULT symbol is euro sign`() {
        assertEquals("€", EUR_DEFAULT.symbol)
    }

    @Test
    fun `CurrencyInfo preserves the rate passed to it`() {
        val info = CurrencyInfo("USD", 1.0825)
        assertEquals(1.0825, info.rate, 0.00001)
    }
}
