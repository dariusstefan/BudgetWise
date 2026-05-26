package com.example.budgetwise

import com.example.budgetwise.util.categoryEmoji
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryEmojiTest {

    @Test
    fun `food and groceries returns cart emoji`() {
        assertEquals("🛒", categoryEmoji("Food & Groceries"))
    }

    @Test
    fun `groceries keyword matches`() {
        assertEquals("🛒", categoryEmoji("groceries"))
    }

    @Test
    fun `housing returns house emoji`() {
        assertEquals("🏠", categoryEmoji("Housing"))
    }

    @Test
    fun `rent keyword matches`() {
        assertEquals("🏠", categoryEmoji("Rent"))
    }

    @Test
    fun `salary returns briefcase emoji`() {
        assertEquals("💼", categoryEmoji("Salary"))
    }

    @Test
    fun `entertainment returns cinema emoji`() {
        assertEquals("🎬", categoryEmoji("Entertainment"))
    }

    @Test
    fun `transport returns car emoji`() {
        assertEquals("🚗", categoryEmoji("Transport"))
    }

    @Test
    fun `shopping returns bag emoji`() {
        assertEquals("🛍️", categoryEmoji("Shopping"))
    }

    @Test
    fun `utilities returns lightning emoji`() {
        assertEquals("⚡", categoryEmoji("Utilities"))
    }

    @Test
    fun `matching is case insensitive`() {
        assertEquals("💼", categoryEmoji("SALARY"))
        assertEquals("🚗", categoryEmoji("transport"))
        assertEquals("🏠", categoryEmoji("HOUSING"))
    }

    @Test
    fun `unknown category returns money bag`() {
        assertEquals("💰", categoryEmoji("Others"))
        assertEquals("💰", categoryEmoji("RandomCategory"))
        assertEquals("💰", categoryEmoji(""))
    }
}
