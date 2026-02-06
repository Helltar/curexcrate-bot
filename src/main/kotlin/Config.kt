import io.github.cdimascio.dotenv.dotenv

object Config {

    private val dotenv = dotenv { ignoreIfMissing = true }

    val creatorId = readEnv("CREATOR_ID").toLongOrNull() ?: throw IllegalArgumentException("Invalid value for CREATOR_ID")
    val telegramBotToken = readEnv("BOT_TOKEN")
    val telegramBotUsername = readEnv("BOT_USERNAME")

    private fun readEnv(env: String) =
        dotenv[env].ifBlank { throw IllegalArgumentException("$env environment variable is blank") }
            ?: throw IllegalArgumentException("$env environment variable is missing")
}
