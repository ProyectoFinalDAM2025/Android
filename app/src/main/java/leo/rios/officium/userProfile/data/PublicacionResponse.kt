package leo.rios.officium.userProfile.data

import com.google.gson.annotations.SerializedName

data class PublicacionListResponse(
    @SerializedName("StatusCode") val statusCode: Int,
    @SerializedName("ReasonPhrase") val reasonPhrase: String? = null,
    @SerializedName("Message") val message: String? = null,
    @SerializedName(value = "Data", alternate = ["data"]) val data: List<PublicacionDto> = emptyList()
)

data class PublicacionResponse(
    @SerializedName("StatusCode") val statusCode: Int,
    @SerializedName("ReasonPhrase") val reasonPhrase: String? = null,
    @SerializedName("Message") val message: String? = null,
    @SerializedName(value = "Data", alternate = ["data"]) val data: PublicacionDto? = null
)

data class DocumentoResponse(
    @SerializedName("StatusCode") val statusCode: Int,
    @SerializedName("ReasonPhrase") val reasonPhrase: String? = null,
    @SerializedName("Message") val message: String? = null,
    @SerializedName(value = "Data", alternate = ["data"]) val data: DocumentoDto? = null
)

data class PublicacionDto(
    @SerializedName("IDPublicacion") val idPublicacion: Int,
    @SerializedName("IDUsuario") val idUsuario: Int? = null,
    @SerializedName("Contenido") val contenido: String,
    @SerializedName("FechaPublicacion") val fechaPublicacion: String? = null,
    @SerializedName("Archivo") val archivo: String? = null,
    @SerializedName("Thumbnail") val thumbnail: String? = null,
    @SerializedName("TipoArchivo") val tipoArchivo: String? = null,
    @SerializedName(value = "documentos", alternate = ["Documentos"]) val documentos: List<DocumentoDto> = emptyList()
)
