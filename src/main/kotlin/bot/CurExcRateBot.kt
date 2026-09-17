package bot

import com.annimon.tgbotsmodule.BotModule
import com.annimon.tgbotsmodule.BotModuleOptions
import com.annimon.tgbotsmodule.beans.Config
import com.helltar.heartbeat.Heartbeat
import org.telegram.telegrambots.longpolling.util.DefaultGetUpdatesGenerator

class CurExcRateBot(
    private val botToken: String,
    private val botUsername: String,
    private val creatorId: Long,
    private val heartbeat: Heartbeat
) : BotModule {

    override fun botHandler(unusedConfig: Config): CurExcRateBotHandler {
        // the generator is where the heartbeat hooks in, because the session calls it once per poll
        // cycle before every request. the update consumer would not do: the session skips it entirely
        // on an empty batch, so a bot nobody writes to would look dead within minutes.
        val options =
            BotModuleOptions.create(botToken)
                .getUpdatesGenerator(heartbeat.beatOn(DefaultGetUpdatesGenerator()))
                .build()

        return CurExcRateBotHandler(options, botUsername, creatorId)
    }
}
