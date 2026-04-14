package currency.parser

import Config.CURRENCY_RESOURCES_PATH
import currency.data.CurrencyDictionary
import currency.data.CurrencyResourceLoader
import currency.models.CurrencyConversionQuery
import java.math.BigDecimal

internal object CurrencyQueryParser {

    private val queryRegex =
        """^\s*([0-9]+(?:[.,][0-9]+)?)\s+([\p{L}$]{1,20})\s+(?:to|in|->|в|у|во|на|до)\s+([\p{L}$]{1,20})\s*$"""
            .toRegex(RegexOption.IGNORE_CASE)

    private val compactQueryRegex =
        """^\s*([0-9]+(?:[.,][0-9]+)?)\s+([\p{L}$]{1,20})\s+([\p{L}$]{1,20})\s*$"""
            .toRegex(RegexOption.IGNORE_CASE)

    private val tokenRegex = """[\p{L}\d$.,']+""".toRegex()
    private val numericTokenRegex = """^\d+(?:[.,]\d+)?$""".toRegex()

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

        val amount =
            match.groupValues[1]
                .replace(',', '.')
                .toBigDecimalOrNull()
                ?: return null

        val from = CurrencyDictionary.resolveCurrency(match.groupValues[2]) ?: return null
        val to = CurrencyDictionary.resolveCurrency(match.groupValues[3]) ?: return null

        return CurrencyConversionQuery(
            amount = amount,
            from = from,
            to = to,
        )
    }

    private fun parseNaturalLanguageQuery(query: String): CurrencyConversionQuery? {
        val tokens = tokenRegex.findAll(query).map { normalizeCurrencyToken(it.value) }.toList()

        if (tokens.isEmpty()) return null

        val currencyMentions =
            tokens.mapIndexedNotNull { index, token ->
                CurrencyDictionary.resolveCurrency(token)?.let { CurrencyMention(index, it) }
            }

        if (currencyMentions.size < 2) return null

        val targetMarkerIndex =
            tokens.indexOfFirst { it in targetMarkers }
                .takeIf { index ->
                    index >= 0 &&
                            currencyMentions.any { mention -> mention.index < index } &&
                            currencyMentions.any { mention -> mention.index > index }
                }

        val fromMention =
            if (targetMarkerIndex != null)
                currencyMentions.lastOrNull { it.index < targetMarkerIndex }
            else {
                currencyMentions.firstOrNull()
            }
                ?: return null

        val toMention =
            if (targetMarkerIndex != null)
                currencyMentions.firstOrNull { it.index > targetMarkerIndex }
            else {
                currencyMentions.firstOrNull { it.index > fromMention.index }
            }
                ?: return null

        val amount = parseAmount(tokens.take(fromMention.index)) ?: BigDecimal.ONE

        return CurrencyConversionQuery(
            amount = amount,
            from = fromMention.code,
            to = toMention.code,
        )
    }

    private fun parseAmount(tokens: List<String>): BigDecimal? {
        tokens.firstNotNullOfOrNull { token ->
            token.takeIf { it.matches(numericTokenRegex) }?.replace(',', '.')?.toBigDecimalOrNull()
        }
            ?.let { return it }

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

    private data class CurrencyMention(
        val index: Int,
        val code: String,
    )
}
