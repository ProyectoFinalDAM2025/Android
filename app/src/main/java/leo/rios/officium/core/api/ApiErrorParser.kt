package leo.rios.officium.core.api

import com.google.gson.JsonElement
import com.google.gson.JsonParser

fun String?.toApiMessage(): String? {
    if (this.isNullOrBlank()) return null

    return try {
        val json = JsonParser.parseString(this).asJsonObject
        val message = json.get("Message")?.toReadableMessage()

        message ?: json.get("ReasonPhrase")?.toReadableMessage()
    } catch (e: Exception) {
        null
    }
}

private fun JsonElement.toReadableMessage(): String? {
    return when {
        isJsonPrimitive -> asString
        isJsonArray -> asJsonArray
            .mapNotNull { it.toReadableMessage() }
            .joinToString(separator = "\n")
            .takeIf { it.isNotBlank() }
        isJsonObject -> asJsonObject.entrySet()
            .flatMap { entry ->
                val value = entry.value
                when {
                    value.isJsonArray -> value.asJsonArray.mapNotNull { it.toReadableMessage() }
                    else -> listOfNotNull(value.toReadableMessage())
                }
            }
            .joinToString(separator = "\n")
            .takeIf { it.isNotBlank() }
        else -> null
    }
}
