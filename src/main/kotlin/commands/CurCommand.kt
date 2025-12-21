package commands

import Google.extractCoinInfo
import Google.extractCurrencyInfo
import Google.extractGoogleFinanceLink
import Google.fetchGoogleHtml
import Strings.HOW_TO_USE
import Strings.INVALID_QUERY
import Strings.PARSING_ERROR
import com.annimon.tgbotsmodule.commands.CommandBundle
import com.annimon.tgbotsmodule.commands.CommandRegistry
import com.annimon.tgbotsmodule.commands.SimpleCommand
import com.annimon.tgbotsmodule.commands.authority.For
import com.annimon.tgbotsmodule.commands.context.MessageContext
import io.github.oshai.kotlinlogging.KotlinLogging
import org.telegram.telegrambots.meta.api.methods.ParseMode

class CurCommand : CommandBundle<For> {

    private companion object {
        val log = KotlinLogging.logger {}
    }

    override fun register(registry: CommandRegistry<For>) {
        registry.register(SimpleCommand("/cur") { ctx ->
            if (ctx.arguments().isEmpty()) {
                replyToMessage(ctx, HOW_TO_USE)
                return@SimpleCommand
            }

            val query = ctx.argumentsAsString()

            if (query.length !in 8..64) {
                replyToMessage(ctx, INVALID_QUERY)
                return@SimpleCommand
            }

            val languageCode = ctx.message().from.languageCode ?: "en"

            try {
                val googleHtml = fetchGoogleHtml(query, languageCode)
                var result = extractCurrencyInfo(googleHtml) ?: extractCoinInfo(googleHtml)

                if (result == null) {
                    result = PARSING_ERROR
                    log.error { "Invalid google response: $googleHtml" }
                }

                val googleFinanceLink = extractGoogleFinanceLink(googleHtml)

                replyToMessage(ctx, "$result\n\n$googleFinanceLink")
            } catch (e: Exception) {
                log.error { e.message }
            }
        })
    }

    private fun replyToMessage(ctx: MessageContext, text: String) =
        ctx.replyToMessage(text)
            .setWebPagePreviewEnabled(false)
            .setParseMode(ParseMode.HTML)
            .callAsync(ctx.sender)
}
