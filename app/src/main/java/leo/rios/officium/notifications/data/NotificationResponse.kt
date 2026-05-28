package leo.rios.officium.notifications.data

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    @SerializedName("StatusCode") val statusCode: Int? = null,
    @SerializedName("ReasonPhrase") val reasonPhrase: String? = null,
    @SerializedName("Message") val message: String? = null,
    @SerializedName("Data") val data: List<NotificationDto> = emptyList(),
    @SerializedName("UnreadCount") val unreadCount: Int = 0
)

data class NotificationDto(
    @SerializedName("IDNotificacion") val idNotificacion: Int,
    @SerializedName("IDUsuario") val idUsuario: Int? = null,
    @SerializedName("Titulo") val titulo: String = "",
    @SerializedName("Mensaje") val mensaje: String = "",
    @SerializedName("Leido") private val leidoRaw: JsonElement? = null,
    @SerializedName("FechaNotificacion") val fechaNotificacion: String? = null,
    @SerializedName("Ruta") val ruta: String? = null
) {
    val leido: Boolean
        get() = when {
            leidoRaw == null || leidoRaw.isJsonNull -> false
            leidoRaw.isJsonPrimitive && leidoRaw.asJsonPrimitive.isBoolean -> leidoRaw.asBoolean
            leidoRaw.isJsonPrimitive && leidoRaw.asJsonPrimitive.isNumber -> leidoRaw.asInt != 0
            leidoRaw.isJsonPrimitive && leidoRaw.asJsonPrimitive.isString -> {
                leidoRaw.asString == "1" || leidoRaw.asString.equals("true", ignoreCase = true)
            }
            else -> false
        }
}
