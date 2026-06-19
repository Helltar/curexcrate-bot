package currency.data

import Config.CURRENCY_RESOURCES_PATH
import currency.parser.normalizeCurrencyToken

internal object CurrencyDictionary {

    private val cryptoIds = CurrencyResourceLoader.loadStringMap("$CURRENCY_RESOURCES_PATH/crypto-ids.json")

    // Machine-generated English names from the provider, plus hand-curated aliases (curated wins on conflict).
    private val rawAliases =
        CurrencyResourceLoader.loadStringMap("$CURRENCY_RESOURCES_PATH/currency-names.json") +
                CurrencyResourceLoader.loadStringMap("$CURRENCY_RESOURCES_PATH/aliases.json")

    /** Single-word aliases: normalized token -> ISO/crypto code. */
    private val singleAliases: Map<String, String> =
        rawAliases
            .filterKeys { !it.trim().contains(' ') }
            .mapKeys { (key, _) -> normalizeCurrencyToken(key) }

    /** Multi-word aliases: list of normalized tokens -> code. */
    private val phraseAliases: Map<List<String>, String> =
        rawAliases
            .filterKeys { it.trim().contains(' ') }
            .entries
            .associate { (key, code) -> key.trim().split(Regex("\\s+")).map(::normalizeCurrencyToken) to code }

    private val maxPhraseLength: Int = phraseAliases.keys.maxOfOrNull { it.size } ?: 0

    /** Fiat codes supported by the rate provider (Frankfurter). Refreshable at runtime. */
    @Volatile
    private var supportedFiat: Set<String> =
        CurrencyResourceLoader.loadSet("$CURRENCY_RESOURCES_PATH/fiat-currencies.json")
            .map { it.lowercase() }
            .toSet()

    /** Every code we recognize as a bare code (without an alias word): real currencies only. */
    @Volatile
    private var knownCodes: Set<String> = buildKnownCodes()

    private fun buildKnownCodes(): Set<String> =
        supportedFiat + rawAliases.values + cryptoIds.keys

    /** Resolves a single token to a currency code, or null if it is not a currency. */
    fun resolveCurrency(raw: String): String? {
        val token = normalizeCurrencyToken(raw)

        return singleAliases[token]
            ?: token.takeIf { it in knownCodes }
    }

    /**
     * Tries to match a multi-word currency alias starting at [start] in [tokens]
     * (tokens must already be normalized). Returns the matched code and its length, longest first.
     */
    fun resolvePhrase(tokens: List<String>, start: Int): PhraseMatch? {
        val maxLen = minOf(maxPhraseLength, tokens.size - start)

        for (length in maxLen downTo 2) {
            val code = phraseAliases[tokens.subList(start, start + length)]
            if (code != null) return PhraseMatch(code, length)
        }

        return null
    }

    fun isCrypto(code: String): Boolean =
        cryptoIds.containsKey(code)

    fun isSupportedFiat(code: String): Boolean =
        code.lowercase() in supportedFiat

    fun cryptoId(code: String): String? =
        cryptoIds[normalizeCurrencyToken(code)]

    /** Replaces the supported-fiat whitelist (e.g. fetched from the provider at startup). */
    fun updateSupportedFiat(codes: Collection<String>) {
        if (codes.isEmpty()) return
        supportedFiat = codes.map { it.lowercase() }.toSet()
        knownCodes = buildKnownCodes()
    }

    data class PhraseMatch(val code: String, val length: Int)
}
