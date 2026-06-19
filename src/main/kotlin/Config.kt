import io.github.cdimascio.dotenv.dotenv

object Config {

    private val dotenv = dotenv { ignoreIfMissing = true }

    const val CURRENCY_RESOURCES_PATH = "currency"

    val creatorId = readEnv("CREATOR_ID").toLongOrNull() ?: throw IllegalArgumentException("Invalid value for CREATOR_ID")
    val telegramBotToken = readEnv("BOT_TOKEN")
    val telegramBotUsername = readEnv("BOT_USERNAME")

    private fun readEnv(env: String) =
        dotenv[env]?.ifBlank { throw IllegalArgumentException("environment variable $env is blank") }
            ?: throw IllegalArgumentException("environment variable $env is missing")
}
