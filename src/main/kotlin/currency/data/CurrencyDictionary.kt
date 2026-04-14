package currency.data

import Config.CURRENCY_RESOURCES_PATH
import currency.parser.normalizeCurrencyToken

internal object CurrencyDictionary {

    private val currencyAliases = CurrencyResourceLoader.loadStringMap("$CURRENCY_RESOURCES_PATH/aliases.json")
    private val cryptoIds = CurrencyResourceLoader.loadStringMap("$CURRENCY_RESOURCES_PATH/crypto-ids.json")
    private val isoCodeRegex = Regex("^[a-z]{3}$")

    fun resolveCurrency(raw: String): String? {
        val token = normalizeCurrencyToken(raw)

        return currencyAliases[token]
            ?: token.takeIf { it.matches(isoCodeRegex) }
            ?: token.takeIf { it.length in 3..5 && (it in currencyAliases.values || it in cryptoIds.keys) }
    }

    fun isCrypto(code: String): Boolean =
        cryptoIds.containsKey(code)

    fun cryptoId(code: String): String? =
        cryptoIds[normalizeCurrencyToken(code)]
}
