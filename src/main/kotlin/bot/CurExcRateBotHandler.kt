package bot

import com.annimon.tgbotsmodule.BotHandler
import com.annimon.tgbotsmodule.BotModuleOptions
import com.annimon.tgbotsmodule.commands.CommandRegistry
import com.annimon.tgbotsmodule.commands.authority.SimpleAuthority
import commands.CurCommand
import commands.StartCommand
import currency.api.CurrencyApiClient
import currency.data.CurrencyDictionary
import io.github.oshai.kotlinlogging.KotlinLogging
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update

class CurExcRateBotHandler(botModuleOptions: BotModuleOptions, botUsername: String, creatorId: Long) :
    BotHandler(botModuleOptions) {

    private companion object {
        val log = KotlinLogging.logger {}
    }

    private val commandRegistry = CommandRegistry(botUsername, SimpleAuthority(creatorId))

    init {
        commandRegistry.registerBundle(StartCommand())
        commandRegistry.registerBundle(CurCommand())
        refreshSupportedFiatCurrencies()
    }

    private fun refreshSupportedFiatCurrencies() {
        runCatching { CurrencyApiClient.fetchSupportedFiatCurrencies() }
            .onSuccess { CurrencyDictionary.updateSupportedFiat(it) }
            .onFailure { log.warn(it) { "Failed to refresh supported fiat currencies; using bundled list" } }
    }

    override fun onUpdate(update: Update): BotApiMethod<*>? {
        commandRegistry.handleUpdate(this, update)
        return null
    }
}
