package commands

import Strings
import com.annimon.tgbotsmodule.commands.CommandBundle
import com.annimon.tgbotsmodule.commands.CommandRegistry
import com.annimon.tgbotsmodule.commands.SimpleCommand
import com.annimon.tgbotsmodule.commands.authority.For
import com.annimon.tgbotsmodule.commands.context.MessageContext
import io.github.oshai.kotlinlogging.KotlinLogging
import org.telegram.telegrambots.meta.api.methods.ParseMode
import currency.CurrencyConverter.convert

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

            if (query.length !in 3..100) {
                replyToMessage(ctx, Strings.INVALID_QUERY)
                return@SimpleCommand
            }

            try {
                val result = convert(query) ?: Strings.PARSING_ERROR
                replyToMessage(ctx, result)
            } catch (e: Exception) {
                log.error(e) { "Currency conversion failed for query: $query" }
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
