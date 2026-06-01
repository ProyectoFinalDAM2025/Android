package leo.rios.officium.userProfile.data

import com.google.gson.annotations.SerializedName

data class PublicacionListResponse(
    @SerializedName("StatusCode") val statusCode: Int,
    @SerializedName("ReasonPhrase") val reasonPhrase: String? = null,
    @SerializedName("Message") val message: String? = null,
    @SerializedName(value = "Data", alternate = ["data"]) val data: List<PublicacionDto> = emptyList()
)

data class PublicacionPageResponse(
    @SerializedName("StatusCode") val statusCode: Int,
    @SerializedName("ReasonPhrase") val reasonPhrase: String? = null,
    @SerializedName("Message") val message: String? = null,
    @SerializedName("Data") val data: PublicacionPageDto? = null
)

data class PublicacionPageDto(
    @SerializedName("current_page") val currentPage: Int = 1,
    @SerializedName("last_page") val lastPage: Int = 1,
    @SerializedName("data") val data: List<PublicacionDto> = emptyList()
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
    @SerializedName("Preview") val preview: String? = null,
    @SerializedName("TipoArchivo") val tipoArchivo: String? = null,
    @SerializedName(value = "documentos", alternate = ["Documentos"]) val documentos: List<DocumentoDto> = emptyList(),
    @SerializedName(value = "comentarios", alternate = ["Comentarios"]) val comentarios: List<ComentarioDto> = emptyList(),
    @SerializedName("comentarios_count") val comentariosCount: Int = 0,
    @SerializedName("likes_count") val likesCount: Int = 0,
    @SerializedName("likedByCurrentUser") val likedByCurrentUser: Boolean = false,
    @SerializedName("user") val user: PublicacionUserDto? = null
)

data class ComentarioDto(
    @SerializedName("IDComentario") val idComentario: Int,
    @SerializedName("IDUsuario") val idUsuario: Int? = null,
    @SerializedName("IDPublicacion") val idPublicacion: Int? = null,
    @SerializedName("Contenido") val contenido: String,
    @SerializedName("FechaComentario") val fechaComentario: String? = null,
    @SerializedName("user") val user: PublicacionUserDto? = null
)

data class PublicacionUserDto(
    @SerializedName("IDUsuario") val idUsuario: Int? = null,
    @SerializedName("rol") val rol: String? = null,
    @SerializedName("empresa") val empresa: EmpresaAuthorDto? = null,
    @SerializedName("desempleado") val desempleado: DesempleadoAuthorDto? = null
)

data class EmpresaAuthorDto(
    @SerializedName("NombreEmpresa") val nombreEmpresa: String? = null,
    @SerializedName("Foto") val foto: String? = null
)

data class DesempleadoAuthorDto(
    @SerializedName("Nombre") val nombre: String? = null,
    @SerializedName("Apellido") val apellido: String? = null,
    @SerializedName("Foto") val foto: String? = null
)

data class ComentarioRequest(
    @SerializedName("IDPublicacion") val idPublicacion: Int,
    @SerializedName("Contenido") val contenido: String
)

data class ComentarioUpdateRequest(
    @SerializedName("Contenido") val contenido: String
)

data class ReportePublicacionRequest(
    @SerializedName("IDPublicacion") val idPublicacion: Int,
    @SerializedName("Motivo") val motivo: String,
    @SerializedName("Descripcion") val descripcion: String?
)

data class ReportePerfilRequest(
    @SerializedName("IDUsuarioReportado") val idUsuarioReportado: Int,
    @SerializedName("Motivo") val motivo: String,
    @SerializedName("Descripcion") val descripcion: String?
)

fun PublicacionUserDto?.displayName(): String {
    return this?.empresa?.nombreEmpresa
        ?: listOfNotNull(this?.desempleado?.nombre, this?.desempleado?.apellido)
            .joinToString(" ")
            .takeIf { it.isNotBlank() }
        ?: "Usuario Officium"
}

fun PublicacionUserDto?.photo(): String? {
    return this?.empresa?.foto ?: this?.desempleado?.foto
}
