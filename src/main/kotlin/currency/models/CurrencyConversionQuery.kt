package currency.models

import java.math.BigDecimal

internal data class CurrencyConversionQuery(
    val amount: BigDecimal,
    val from: String,
    val to: String,
)
