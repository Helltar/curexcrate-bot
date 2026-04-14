package currency.data

import kotlinx.serialization.json.*

internal object CurrencyResourceLoader {

    private val json = Json { ignoreUnknownKeys = false }

    fun loadStringMap(resourcePath: String): Map<String, String> =
        loadJsonObject(resourcePath).mapValues { (_, value) ->
            value.jsonPrimitive.content
        }

    fun loadIntMap(resourcePath: String): Map<String, Int> =
        loadJsonObject(resourcePath).mapValues { (_, value) ->
            value.jsonPrimitive.int
        }

    fun loadSet(resourcePath: String): Set<String> =
        loadJsonArray(resourcePath)
            .map { it.jsonPrimitive.content }
            .toSet()

    private fun loadJsonObject(resourcePath: String): JsonObject =
        json.parseToJsonElement(loadResource(resourcePath)) as? JsonObject
            ?: error("Expected JSON object in: $resourcePath")

    private fun loadJsonArray(resourcePath: String): JsonArray =
        json.parseToJsonElement(loadResource(resourcePath)) as? JsonArray
            ?: error("Expected JSON array in: $resourcePath")

    private fun loadResource(resourcePath: String): String {
        val stream =
            CurrencyResourceLoader::class.java.classLoader.getResourceAsStream(resourcePath)
                ?: error("Resource not found: $resourcePath")

        return stream.bufferedReader().use { it.readText() }
    }
}
