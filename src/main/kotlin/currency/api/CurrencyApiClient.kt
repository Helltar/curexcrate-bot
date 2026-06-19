package currency.api

import currency.data.CurrencyDictionary
import currency.models.CoinGeckoSimplePriceResponse
import currency.models.FrankfurterRateResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

internal object CurrencyApiClient {

    private const val FIAT_API_URL = "https://api.frankfurter.dev/v2/rate"
    private const val FIAT_CURRENCIES_URL = "https://api.frankfurter.dev/v2/currencies"
    private const val CRYPTO_API_URL = "https://api.coingecko.com/api/v3/simple/price"
    private const val USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36"

    private val httpClient =
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

    private val json = Json { ignoreUnknownKeys = true }

    fun fetchFiatRate(baseCode: String, quoteCode: String): FrankfurterRateResponse =
        json.decodeFromString(httpGet("$FIAT_API_URL/${baseCode.uppercase(Locale.US)}/${quoteCode.uppercase(Locale.US)}"))

    /** Codes supported by Frankfurter, e.g. ["usd", "eur", ...]. Response is an array of currency objects. */
    fun fetchSupportedFiatCurrencies(): Set<String> =
        json.parseToJsonElement(httpGet(FIAT_CURRENCIES_URL))
            .jsonArray
            .mapNotNull { it.jsonObject["iso_code"]?.jsonPrimitive?.contentOrNull }
            .map { it.lowercase(Locale.US) }
            .toSet()

    fun fetchCryptoRates(codes: List<String>, quoteCurrency: String): CoinGeckoSimplePriceResponse {
        val ids =
            codes
                .mapNotNull(CurrencyDictionary::cryptoId)
                .distinct()
                .joinToString(",")

        return CoinGeckoSimplePriceResponse.fromJson(
            json =
                httpGet(
                    CRYPTO_API_URL,
                    listOf(
                        "ids" to ids,
                        "vs_currencies" to quoteCurrency.lowercase(Locale.US),
                    ),
                ), jsonDecoder = json
        )
    }

    private fun httpGet(url: String, parameters: List<Pair<String, String>> = emptyList()): String {
        val httpUrl =
            parameters
                .fold(url.toHttpUrl().newBuilder()) { builder, (key, value) ->
                    builder.addQueryParameter(key, value)
                }
                .build()

        val request =
            Request.Builder()
                .url(httpUrl)
                .header("Accept", "application/json")
                .header("User-Agent", USER_AGENT)
                .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP request failed: ${response.code}")
            return response.body?.string() ?: throw IOException("Empty response body")
        }
    }
}
