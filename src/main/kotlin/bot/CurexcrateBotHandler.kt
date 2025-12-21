package bot

import Config.creatorId
import Config.telegramBotUsername
import com.annimon.tgbotsmodule.BotHandler
import com.annimon.tgbotsmodule.BotModuleOptions
import com.annimon.tgbotsmodule.commands.CommandRegistry
import com.annimon.tgbotsmodule.commands.authority.SimpleAuthority
import commands.CurCommand
import commands.StartCommand
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update

class CurexcrateBotHandler(botModuleOptions: BotModuleOptions) : BotHandler(botModuleOptions) {

    private val commandRegistry = CommandRegistry(telegramBotUsername, SimpleAuthority(creatorId))

    init {
        commandRegistry.registerBundle(StartCommand())
        commandRegistry.registerBundle(CurCommand())
    }

    override fun onUpdate(update: Update): BotApiMethod<*>? {
        commandRegistry.handleUpdate(this, update)
        return null
    }
}
