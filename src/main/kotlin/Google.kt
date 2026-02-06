import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.io.IOException
import java.util.concurrent.TimeUnit

object Google {

    private const val GOOGLE_SEARCH_URL = "https://www.google.com/search"
    private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 12; 22011119UY Build/SP1A.210812.016; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/131.0.6778.41 Mobile Safari/537.36 GoogleApp/15.45.39.ve.arm64"

    private val GOOGLE_FINANCE_REGEX = """<a class="jRKCUd" href="(.*?)">""".toRegex()
    private val CURRENCY_REGEX = """<span class="DFlfde (.*?)<span>""".toRegex()
    private val CRYPTO_REGEX = """<span class="pclqee">(.*?)</span>""".toRegex()
    private val NUMBER_HIGHLIGHT_REGEX = """([0-9,.]+)""".toRegex()

    private val httpClient =
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

    fun searchGoogle(query: String, languageCode: String): String {
        val parameters =
            listOf(
                "hl" to languageCode,
                "q" to query,
                "client" to "ms-android-xiaomi-rvo3",
                "source" to "and.gsa.launcher.icon",
            )

        return httpGet(GOOGLE_SEARCH_URL, parameters)
    }

    fun extractCurrencyInfo(html: String): String? =
        extractAndHighlightNumbers(html, CURRENCY_REGEX)

    fun extractCryptoInfo(html: String): String? =
        extractAndHighlightNumbers(html, CRYPTO_REGEX)

    fun extractGoogleFinanceLink(html: String): String =
        findFirstMatchGroup(html, GOOGLE_FINANCE_REGEX)?.let { url -> """<a href="$url">Google Finance 📈</a>""" } ?: ""

    private fun extractAndHighlightNumbers(html: String, contentRegex: Regex): String? =
        findFirstMatchGroup(html, contentRegex, groupIndex = 0)?.let { rawHtml ->
            Jsoup.parse(rawHtml).text().replace(NUMBER_HIGHLIGHT_REGEX, "<b>$1</b>")
        }

    private fun findFirstMatchGroup(input: String, regex: Regex, groupIndex: Int = 1): String? =
        regex.find(input)?.groupValues?.getOrNull(groupIndex)

    private fun httpGet(url: String, parameters: List<Pair<String, String>>): String {
        val httpUrl =
            parameters
                .fold(url.toHttpUrl().newBuilder()) { builder, (key, value) ->
                    builder.addQueryParameter(key, value)
                }
                .build()

        val request =
            Request.Builder()
                .url(httpUrl)
                .header("User-Agent", USER_AGENT)
                .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP request failed: ${response.code}")
            return response.body?.string() ?: throw IOException("Empty response body")
        }
    }
}
