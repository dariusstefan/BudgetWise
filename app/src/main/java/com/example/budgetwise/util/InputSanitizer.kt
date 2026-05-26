package com.example.budgetwise.util

object InputSanitizer {

    private const val MAX_AMOUNT = 1_000_000.0
    private const val MAX_LABEL_LENGTH = 100

    // Strips characters that could be used in injection attacks (defense-in-depth
    // on top of Room's parameterized queries).
    private val DANGEROUS_CHARS = Regex("[;'\"\\\\]")

    fun sanitizeAmount(input: String): Double? {
        val value = input.trim().toDoubleOrNull() ?: return null
        if (value <= 0 || value > MAX_AMOUNT) return null
        return value
    }

    fun sanitizeLabel(input: String): String =
        input.trim()
            .replace(DANGEROUS_CHARS, "")
            .take(MAX_LABEL_LENGTH)

    fun isValidLabel(input: String): Boolean {
        val sanitized = sanitizeLabel(input)
        return sanitized.isNotEmpty()
    }
}
