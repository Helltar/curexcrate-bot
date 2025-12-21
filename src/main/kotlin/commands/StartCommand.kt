package commands

import Strings.HOW_TO_USE
import com.annimon.tgbotsmodule.commands.CommandBundle
import com.annimon.tgbotsmodule.commands.CommandRegistry
import com.annimon.tgbotsmodule.commands.SimpleCommand
import com.annimon.tgbotsmodule.commands.authority.For
import org.telegram.telegrambots.meta.api.methods.ParseMode

class StartCommand : CommandBundle<For> {

    override fun register(registry: CommandRegistry<For>) {
        registry.register(SimpleCommand("/start") { ctx ->
            ctx.replyToMessage(HOW_TO_USE)
                .setParseMode(ParseMode.HTML)
                .callAsync(ctx.sender)
        })
    }
}
