package bot

import com.annimon.tgbotsmodule.BotModule
import com.annimon.tgbotsmodule.BotModuleOptions
import com.annimon.tgbotsmodule.beans.Config

class CurExcRateBot(private val botToken: String, private val botUsername: String, private val creatorId: Long) :
    BotModule {

    override fun botHandler(unusedConfig: Config) =
        CurExcRateBotHandler(BotModuleOptions.createDefault(botToken), botUsername, creatorId)
}
