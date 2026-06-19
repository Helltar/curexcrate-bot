package currency.parser

import java.util.*

/** Currency symbols that may appear glued to amounts, e.g. "$100", "100€". */
internal const val CURRENCY_SYMBOLS = "$€£₴₽¥"

private val symbolRegex = Regex("[$CURRENCY_SYMBOLS]")

/**
 * Normalizes the whole query before tokenization: lowercases, unifies quotes and
 * pads currency symbols with spaces so that "$100" / "100€" split into separate tokens.
 */
internal fun normalizeCurrencyInput(input: String): String =
    input.normalizeCommon()
        .replace(symbolRegex) { " ${it.value} " }

/** Normalizes a single token: lowercases, unifies quotes, strips surrounding punctuation. */
internal fun normalizeCurrencyToken(input: String): String =
    input.normalizeCommon()
        .trim()
        .trim('.', ',', '!', '?', ':', ';', '"', '(', ')')

private fun String.normalizeCommon(): String =
    lowercase(Locale.US)
        .replace('’', '\'')
        .replace('`', '\'')
