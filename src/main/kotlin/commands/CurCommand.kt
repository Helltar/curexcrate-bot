package commands

import Google.extractCryptoInfo
import Google.extractCurrencyInfo
import Google.extractGoogleFinanceLink
import Google.searchGoogle
import Strings
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
                replyToMessage(ctx, Strings.HOW_TO_USE)
                return@SimpleCommand
            }

            val query = ctx.argumentsAsString()

            if (query.length !in 8..64) {
                replyToMessage(ctx, Strings.INVALID_QUERY)
                return@SimpleCommand
            }

            val languageCode = ctx.message().from.languageCode ?: "en"

            try {
                val html = searchGoogle(query, languageCode)

                val result =
                    extractCurrencyInfo(html)
                        ?: extractCryptoInfo(html)
                        ?: run {
                            log.error { "Invalid google response: $html" }
                            Strings.PARSING_ERROR
                        }

                val googleFinanceLink = extractGoogleFinanceLink(html)

                replyToMessage(ctx, "$result\n\n$googleFinanceLink")
            } catch (e: Exception) {
                log.error(e) { "Google search failed for query: $query" }
                replyToMessage(ctx, Strings.REQUEST_FAILED)
            }
        })
    }

    private fun replyToMessage(ctx: MessageContext, text: String) =
        ctx.replyToMessage(text)
            .setWebPagePreviewEnabled(false)
            .setParseMode(ParseMode.HTML)
            .callAsync(ctx.sender)
}
