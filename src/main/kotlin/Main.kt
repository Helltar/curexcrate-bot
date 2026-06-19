import bot.CurExcRateBot
import com.annimon.tgbotsmodule.Runner

fun main() {
    val bot = CurExcRateBot(Config.telegramBotToken, Config.telegramBotUsername, Config.creatorId)
    Runner.run("", listOf(bot))
}
