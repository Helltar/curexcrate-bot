package currency.parser

import java.util.*

internal fun normalizeCurrencyInput(input: String): String =
    input.normalizeInput()

internal fun normalizeCurrencyToken(input: String): String =
    input.normalizeInput()
        .trim('.', ',', '!', '?', ':', ';', '"', '(', ')')

private fun String.normalizeInput(): String =
    lowercase(Locale.US)
        .replace('’', '\'')
        .replace('`', '\'')
