package currency.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.math.BigDecimal

@Serializable
internal data class CoinGeckoSimplePriceResponse(val prices: Map<String, Map<String, Double>>) {

    fun rate(coinId: String, quoteCurrency: String): BigDecimal? =
        prices[coinId]?.get(quoteCurrency)?.let(BigDecimal::valueOf)

    companion object {
        private val serializer = MapSerializer(String.serializer(), MapSerializer(String.serializer(), Double.serializer()))

        fun fromJson(json: String, jsonDecoder: Json): CoinGeckoSimplePriceResponse =
            CoinGeckoSimplePriceResponse(prices = jsonDecoder.decodeFromString(serializer, json))
    }
}
