package currency.models

import java.math.BigDecimal
import kotlinx.serialization.Serializable

@Serializable
internal data class FrankfurterRateResponse(
    val date: String,
    val base: String,
    val quote: String,
    val rate: Double,
) {
    fun rateAsBigDecimal(): BigDecimal = BigDecimal.valueOf(rate)
}
