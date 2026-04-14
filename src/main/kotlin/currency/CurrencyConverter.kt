package currency

import currency.api.CurrencyApiClient
import currency.data.CurrencyDictionary
import currency.models.CoinGeckoSimplePriceResponse
import currency.models.CurrencyConversionQuery
import currency.parser.CurrencyQueryParser
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.*

object CurrencyConverter {

    fun convert(query: String): String? {
        val parsedQuery = CurrencyQueryParser.parse(query) ?: return null

        if (parsedQuery.from == parsedQuery.to) {
            return formatResult(
                amount = parsedQuery.amount,
                from = parsedQuery.from,
                convertedAmount = parsedQuery.amount,
                to = parsedQuery.to,
                decimalScale = if (CurrencyDictionary.isCrypto(parsedQuery.from)) 8 else 4,
            )
        }

        return if (CurrencyDictionary.isCrypto(parsedQuery.from) || CurrencyDictionary.isCrypto(parsedQuery.to))
            convertCrypto(parsedQuery)
        else
            convertFiat(parsedQuery)
    }

    private fun convertFiat(query: CurrencyConversionQuery): String {
        val response = CurrencyApiClient.fetchFiatRate(query.from, query.to)
        val rate = response.rateAsBigDecimal()
        val convertedAmount = query.amount.multiply(rate, MathContext.DECIMAL64)

        return formatResult(
            amount = query.amount,
            from = query.from,
            convertedAmount = convertedAmount,
            to = query.to,
            decimalScale = 4,
        )
    }

    private fun convertCrypto(query: CurrencyConversionQuery): String? {
        val fromIsCrypto = CurrencyDictionary.isCrypto(query.from)
        val toIsCrypto = CurrencyDictionary.isCrypto(query.to)

        val convertedAmount =
            when {
                fromIsCrypto && toIsCrypto -> {
                    val response = CurrencyApiClient.fetchCryptoRates(listOf(query.from, query.to), "usd")
                    val fromUsd = extractCoinGeckoRate(response, query.from, "usd") ?: return null
                    val toUsd = extractCoinGeckoRate(response, query.to, "usd") ?: return null

                    query.amount
                        .multiply(fromUsd, MathContext.DECIMAL64)
                        .divide(toUsd, 8, RoundingMode.HALF_UP)
                }

                fromIsCrypto -> {
                    val response = CurrencyApiClient.fetchCryptoRates(listOf(query.from), query.to)
                    val rate = extractCoinGeckoRate(response, query.from, query.to) ?: return null
                    query.amount.multiply(rate, MathContext.DECIMAL64)
                }

                toIsCrypto -> {
                    val response = CurrencyApiClient.fetchCryptoRates(listOf(query.to), query.from)
                    val rate = extractCoinGeckoRate(response, query.to, query.from) ?: return null
                    query.amount.divide(rate, 8, RoundingMode.HALF_UP)
                }

                else -> return null
            }

        return formatResult(
            amount = query.amount,
            from = query.from,
            convertedAmount = convertedAmount,
            to = query.to,
            decimalScale = 8,
        )
    }

    private fun extractCoinGeckoRate(response: CoinGeckoSimplePriceResponse, code: String, quoteCurrency: String): BigDecimal? {
        val id = CurrencyDictionary.cryptoId(code) ?: return null
        return response.rate(id, quoteCurrency.lowercase(Locale.US))
    }

    private fun formatResult(amount: BigDecimal, from: String, convertedAmount: BigDecimal, to: String, decimalScale: Int): String =
        "<b>${formatNumber(amount, decimalScale)} ${from.uppercase(Locale.US)}</b> = <b>${formatNumber(convertedAmount, decimalScale)} ${to.uppercase(Locale.US)}</b>"

    private fun formatNumber(number: BigDecimal, scale: Int): String =
        number
            .setScale(scale, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
}
