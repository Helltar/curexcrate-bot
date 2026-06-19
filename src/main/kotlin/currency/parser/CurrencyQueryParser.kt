package currency.parser

import Config.CURRENCY_RESOURCES_PATH
import currency.data.CurrencyDictionary
import currency.data.CurrencyResourceLoader
import currency.models.CurrencyConversionQuery
import java.math.BigDecimal

internal object CurrencyQueryParser {

    private val queryRegex =
        """^\s*([0-9][0-9.,]*)\s+([\p{L}$CURRENCY_SYMBOLS]{1,20})\s+(?:to|in|->|в|у|во|на|до)\s+([\p{L}$CURRENCY_SYMBOLS]{1,20})\s*$"""
            .toRegex(RegexOption.IGNORE_CASE)

    private val compactQueryRegex =
        """^\s*([0-9][0-9.,]*)\s+([\p{L}$CURRENCY_SYMBOLS]{1,20})\s+([\p{L}$CURRENCY_SYMBOLS]{1,20})\s*$"""
            .toRegex(RegexOption.IGNORE_CASE)

    private val tokenRegex = """[\p{L}\d$CURRENCY_SYMBOLS.,']+""".toRegex()

    // Number formats: plain (10 / 10.5), comma-decimal (10,5), grouped US (1,234.5), grouped EU (1.234,5).
    private val plainNumberRegex = """\d+(\.\d+)?""".toRegex()
    private val commaDecimalRegex = """\d+,\d+""".toRegex()
    private val groupedUsRegex = """\d{1,3}(,\d{3})+(\.\d+)?""".toRegex()
    private val groupedEuRegex = """\d{1,3}(\.\d{3})+(,\d+)?""".toRegex()

    private val targetMarkers = CurrencyResourceLoader.loadSet("$CURRENCY_RESOURCES_PATH/target-markers.json")
    private val fillerWords = CurrencyResourceLoader.loadSet("$CURRENCY_RESOURCES_PATH/filler-words.json")
    private val numberWords = CurrencyResourceLoader.loadIntMap("$CURRENCY_RESOURCES_PATH/number-words.json")
    private val multiplierWords = CurrencyResourceLoader.loadIntMap("$CURRENCY_RESOURCES_PATH/multiplier-words.json")

    fun parse(query: String): CurrencyConversionQuery? {
        val normalizedQuery = normalizeCurrencyInput(query)
        parseStructuredQuery(normalizedQuery)?.let { return it }
        return parseNaturalLanguageQuery(normalizedQuery)
    }

    private fun parseStructuredQuery(query: String): CurrencyConversionQuery? {
        val match = queryRegex.matchEntire(query) ?: compactQueryRegex.matchEntire(query) ?: return null

        val amount = parseDecimal(match.groupValues[1]) ?: return null
        val from = CurrencyDictionary.resolveCurrency(match.groupValues[2]) ?: return null
        val to = CurrencyDictionary.resolveCurrency(match.groupValues[3]) ?: return null

        return CurrencyConversionQuery(amount = amount, from = from, to = to)
    }

    private fun parseNaturalLanguageQuery(query: String): CurrencyConversionQuery? {
        val tokens = tokenRegex.findAll(query).map { normalizeCurrencyToken(it.value) }.toList()

        if (tokens.isEmpty()) return null

        val mentions = findCurrencyMentions(tokens)
        if (mentions.size < 2) return null

        val targetMarkerIndex =
            tokens.indexOfFirst { it in targetMarkers }
                .takeIf { index ->
                    index >= 0 &&
                            mentions.any { it.startIndex < index } &&
                            mentions.any { it.startIndex > index }
                }

        val fromMention =
            (if (targetMarkerIndex != null)
                mentions.lastOrNull { it.startIndex < targetMarkerIndex }
            else
                mentions.firstOrNull())
                ?: return null

        val toMention =
            (if (targetMarkerIndex != null)
                mentions.firstOrNull { it.startIndex > targetMarkerIndex }
            else
                mentions.firstOrNull { it.startIndex > fromMention.startIndex })
                ?: return null

        val amount = parseAmount(residualTokens(tokens, mentions, targetMarkerIndex)) ?: BigDecimal.ONE

        return CurrencyConversionQuery(
            amount = amount,
            from = fromMention.code,
            to = toMention.code,
        )
    }

    private fun findCurrencyMentions(tokens: List<String>): List<CurrencyMention> {
        val mentions = mutableListOf<CurrencyMention>()
        var index = 0

        while (index < tokens.size) {
            val phrase = CurrencyDictionary.resolvePhrase(tokens, index)

            if (phrase != null) {
                mentions += CurrencyMention(index, index + phrase.length, phrase.code)
                index += phrase.length
                continue
            }

            CurrencyDictionary.resolveCurrency(tokens[index])?.let {
                mentions += CurrencyMention(index, index + 1, it)
            }

            index++
        }

        return mentions
    }

    /** Tokens that are neither part of a currency mention nor the target marker — candidates for the amount. */
    private fun residualTokens(tokens: List<String>, mentions: List<CurrencyMention>, targetMarkerIndex: Int?): List<String> {
        val consumed = BooleanArray(tokens.size)

        mentions.forEach { mention ->
            for (i in mention.startIndex until mention.endIndex) consumed[i] = true
        }

        targetMarkerIndex?.let { consumed[it] = true }

        return tokens.filterIndexed { index, _ -> !consumed[index] }
    }

    private fun parseAmount(tokens: List<String>): BigDecimal? {
        tokens.firstNotNullOfOrNull { parseDecimal(it) }?.let { return it }

        var total = 0L
        var current = 0L
        var seenNumberWord = false

        for (token in tokens) {
            when {
                token in fillerWords && !seenNumberWord -> continue

                numberWords.containsKey(token) -> {
                    current += numberWords.getValue(token)
                    seenNumberWord = true
                }

                multiplierWords.containsKey(token) -> {
                    val multiplier = multiplierWords.getValue(token)

                    if (multiplier == 100)
                        current = (if (current == 0L) 1 else current) * multiplier
                    else {
                        total += (if (current == 0L) 1 else current) * multiplier
                        current = 0
                    }

                    seenNumberWord = true
                }

                seenNumberWord -> break
            }
        }

        return (total + current).takeIf { seenNumberWord }?.toBigDecimal()
    }

    /** Parses a numeric literal, tolerating decimal commas and thousands separators. */
    private fun parseDecimal(raw: String): BigDecimal? {
        val value = raw.trim()

        return when {
            value.isEmpty() -> null
            groupedUsRegex.matches(value) -> value.replace(",", "").toBigDecimalOrNull()
            groupedEuRegex.matches(value) -> value.replace(".", "").replace(',', '.').toBigDecimalOrNull()
            commaDecimalRegex.matches(value) -> value.replace(',', '.').toBigDecimalOrNull()
            plainNumberRegex.matches(value) -> value.toBigDecimalOrNull()
            else -> null
        }
    }

    private data class CurrencyMention(
        val startIndex: Int,
        val endIndex: Int,
        val code: String,
    )
}
