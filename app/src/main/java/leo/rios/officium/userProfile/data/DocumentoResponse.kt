package leo.rios.officium.userProfile.data

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class DocumentoListResponse(
    @SerializedName("StatusCode") val statusCode: Int,
    @SerializedName("ReasonPhrase") val reasonPhrase: String? = null,
    @SerializedName(value = "Data", alternate = ["data"]) val data: List<DocumentoDto> = emptyList()
)

data class DocumentoDto(
    @SerializedName("IDDocumento") val idDocumento: Int,
    @SerializedName("IDUsuario") val idUsuario: Int? = null,
    @SerializedName("IDPublicacion") val idPublicacion: Int? = null,
    @SerializedName("Tipo") val tipo: String,
    @SerializedName("NombreArchivo") val nombreArchivo: String,
    @SerializedName("URL") val url: String,
    @SerializedName("Thumbnail") val thumbnail: String? = null,
    @SerializedName("Preview") val preview: String? = null,
    @SerializedName("FechaSubida") val fechaSubida: String? = null,
    @SerializedName("Descripcion") val descripcion: String? = null
)

data class ProfileUpdateResponse(
    @SerializedName("StatusCode") val statusCode: Int,
    @SerializedName("ReasonPhrase") val reasonPhrase: String? = null,
    @SerializedName("Message") val message: String? = null,
    @SerializedName("Data") val data: ProfileUpdateData? = null
)

data class ProfileUpdateData(
    val profile: JsonObject?
)
