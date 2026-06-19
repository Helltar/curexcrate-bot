package currency.parser

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CurrencyQueryParserTest {

    private fun assertParsed(query: String, amount: String, from: String, to: String) {
        val result = CurrencyQueryParser.parse(query)
            ?: error("expected query to parse: \"$query\"")

        assertEquals(BigDecimal(amount).stripTrailingZeros(), result.amount.stripTrailingZeros(), "amount for \"$query\"")
        assertEquals(from, result.from, "from for \"$query\"")
        assertEquals(to, result.to, "to for \"$query\"")
    }

    // --- structured form ---

    @Test fun `structured with marker`() = assertParsed("1 usd to uah", "1", "usd", "uah")

    @Test fun `structured compact without marker`() = assertParsed("10 usd uah", "10", "usd", "uah")

    @Test fun `structured cyrillic marker`() = assertParsed("5 usd в uah", "5", "usd", "uah")

    @Test fun `structured arrow marker`() = assertParsed("7 eur -> usd", "7", "eur", "usd")

    @Test fun `comma decimal`() = assertParsed("10,5 usd to eur", "10.5", "usd", "eur")

    @Test fun `grouped thousands us`() = assertParsed("1,000 usd to eur", "1000", "usd", "eur")

    @Test fun `grouped thousands eu`() = assertParsed("1.000 usd to eur", "1000", "usd", "eur")

    // --- natural language: words ---

    @Test fun `english number words`() = assertParsed("one hundred dollars in euros", "100", "usd", "eur")

    @Test fun `english thousands`() = assertParsed("two thousand dollars to euros", "2000", "usd", "eur")

    @Test fun `ukrainian number words`() = assertParsed("сто доларів у євро", "100", "usd", "eur")

    @Test fun `ukrainian simple`() = assertParsed("10 гривень в євро", "10", "uah", "eur")

    @Test fun `default amount is one`() = assertParsed("долар в євро", "1", "usd", "eur")

    @Test fun `amount after currency`() = assertParsed("dollars 100 to euro", "100", "usd", "eur")

    // --- multi-word currency names ---

    @Test fun `belarusian ruble phrase`() = assertParsed("100 белорусских рублей в долларах", "100", "byn", "usd")

    @Test fun `moldovan leu phrase`() = assertParsed("50 молдавських леїв в євро", "50", "mdl", "eur")

    // --- currency symbols ---

    @Test fun `symbol prefix`() = assertParsed("\$100 to uah", "100", "usd", "uah")

    @Test fun `symbol suffix`() = assertParsed("100\$ to uah", "100", "usd", "uah")

    @Test fun `euro symbol`() = assertParsed("€50 to usd", "50", "eur", "usd")

    @Test fun `hryvnia symbol`() = assertParsed("500₴ in usd", "500", "uah", "usd")

    // --- crypto ---

    @Test fun `crypto code`() = assertParsed("1 btc to usd", "1", "btc", "usd")

    @Test fun `crypto by name`() = assertParsed("2 bitcoin to eur", "2", "btc", "eur")

    @Test fun `crypto cardano`() = assertParsed("1 cardano to usd", "1", "ada", "usd")

    @Test fun `crypto dogecoin ru`() = assertParsed("100 догикоин в usd", "100", "doge", "usd")

    @Test fun `crypto toncoin`() = assertParsed("5 toncoin to usd", "5", "ton", "usd")

    @Test fun `crypto polkadot`() = assertParsed("3 polkadot to eur", "3", "dot", "eur")

    @Test fun `crypto multiword name`() = assertParsed("1 bitcoin cash to usd", "1", "bch", "usd")

    // --- expanded fiat plain-language names ---

    @Test fun `fiat rand`() = assertParsed("10 ранд to uah", "10", "zar", "uah")

    @Test fun `fiat ringgit`() = assertParsed("100 ringgit to usd", "100", "myr", "usd")

    @Test fun `fiat won ua`() = assertParsed("1000 вона в usd", "1000", "krw", "usd")

    @Test fun `fiat canadian dollar phrase`() = assertParsed("50 канадский доллар to usd", "50", "cad", "usd")

    @Test fun `fiat english full name`() = assertParsed("1 canadian dollar to usd", "1", "cad", "usd")

    @Test fun `fiat indian rupee phrase`() = assertParsed("200 индийская рупия в eur", "200", "inr", "eur")

    // --- rejections ---

    @Test fun `random words are not currencies`() = assertNull(CurrencyQueryParser.parse("cat to dog"))

    @Test fun `single currency is not enough`() = assertNull(CurrencyQueryParser.parse("100 dollars"))

    @Test fun `gibberish`() = assertNull(CurrencyQueryParser.parse("hello world"))
}
