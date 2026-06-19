package bot

import Config.telegramBotToken
import com.annimon.tgbotsmodule.BotModule
import com.annimon.tgbotsmodule.BotModuleOptions
import com.annimon.tgbotsmodule.beans.Config

class CurExcRateBot : BotModule {

    override fun botHandler(config: Config) =
        CurExcRateBotHandler(BotModuleOptions.createDefault(telegramBotToken))
}
