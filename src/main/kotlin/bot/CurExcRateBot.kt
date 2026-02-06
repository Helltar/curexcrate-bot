package bot

import Config.telegramBotToken
import com.annimon.tgbotsmodule.BotModule
import com.annimon.tgbotsmodule.BotModuleOptions
import com.annimon.tgbotsmodule.Runner
import com.annimon.tgbotsmodule.beans.Config

class CurExcRateBot : BotModule {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Runner.run("", listOf(CurExcRateBot()))
        }
    }

    override fun botHandler(config: Config) =
        CurExcRateBotHandler(BotModuleOptions.createDefault(telegramBotToken))
}
