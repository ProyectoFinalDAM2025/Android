package leo.rios.officium.jobOffers.data

import com.google.gson.annotations.SerializedName

data class JobOfferListResponse(
    @SerializedName("StatusCode") val statusCode: Int? = null,
    @SerializedName("ReasonPhrase") val reasonPhrase: String? = null,
    @SerializedName("Message") val message: String? = null,
    @SerializedName("Data") val dataUpper: JobOfferPageDto? = null,
    @SerializedName("data") val dataLower: JobOfferPageDto? = null
) {
    val page: JobOfferPageDto?
        get() = dataUpper ?: dataLower
}

data class JobOfferResponse(
    @SerializedName("StatusCode") val statusCode: Int? = null,
    @SerializedName("ReasonPhrase") val reasonPhrase: String? = null,
    @SerializedName("Message") val message: String? = null,
    @SerializedName("Data") val dataUpper: JobOfferDto? = null,
    @SerializedName("data") val dataLower: JobOfferDto? = null
) {
    val data: JobOfferDto?
        get() = dataUpper ?: dataLower
}

data class JobOfferPageDto(
    @SerializedName("data") val data: List<JobOfferDto> = emptyList(),
    @SerializedName("current_page") val currentPage: Int = 1,
    @SerializedName("last_page") val lastPage: Int = 1
)

data class JobOfferDto(
    @SerializedName("IDOferta") val idOferta: Int,
    @SerializedName("IDEmpresa") val idEmpresa: Int? = null,
    @SerializedName("IDCategoria") val idCategoria: Int? = null,
    @SerializedName("Titulo") val titulo: String = "",
    @SerializedName("Descripcion") val descripcion: String = "",
    @SerializedName("Ubicacion") val ubicacion: String = "",
    @SerializedName("Estado") val estado: String = "",
    @SerializedName("CategoriaNombre") val categoriaNombre: String? = null,
    @SerializedName("Nombre_Categoria") val nombreCategoria: String? = null,
    @SerializedName("categoria") val categoria: JobOfferCategoryDto? = null,
    @SerializedName("empresa") val empresa: JobOfferCompanyDto? = null,
    @SerializedName("aplicaciones") val aplicaciones: List<JobApplicationDto>? = emptyList(),
    @SerializedName("desempleados_aplicados") val desempleadosAplicados: List<JobApplicantDto>? = emptyList(),
    @SerializedName("pivot") val pivot: JobApplicationPivotDto? = null
) {
    val categoryName: String
        get() = categoriaNombre ?: nombreCategoria ?: categoria?.nombre.orEmpty()

    val applicationCount: Int
        get() = when {
            aplicaciones.orEmpty().isNotEmpty() -> aplicaciones.orEmpty().size
            desempleadosAplicados.orEmpty().isNotEmpty() -> desempleadosAplicados.orEmpty().size
            pivot != null -> 1
            else -> 0
        }

    fun currentUserApplication(idDesempleado: Int?): JobApplicationDto? {
        if (idDesempleado == null) return null

        pivot
            ?.toApplication(idDesempleado = idDesempleado, idOferta = idOferta)
            ?.let { return it }

        aplicaciones.orEmpty()
            .firstOrNull { it.idDesempleado == idDesempleado }
            ?.let { return it }

        return desempleadosAplicados.orEmpty()
            .firstOrNull { it.idDesempleado == idDesempleado }
            ?.pivot
            ?.toApplication(idDesempleado = idDesempleado, idOferta = idOferta)
    }
}

data class JobOfferCategoryDto(
    @SerializedName("IDCategoria") val idCategoria: Int,
    @SerializedName("Nombre") val nombre: String
)

data class JobOfferRequest(
    @SerializedName("IDCategoria") val idCategoria: Int,
    @SerializedName("Titulo") val titulo: String,
    @SerializedName("Descripcion") val descripcion: String,
    @SerializedName("Ubicacion") val ubicacion: String,
    @SerializedName("Estado") val estado: String
)

data class JobOfferUpdateRequest(
    @SerializedName("IDCategoria") val idCategoria: Int,
    @SerializedName("Titulo") val titulo: String,
    @SerializedName("Descripcion") val descripcion: String,
    @SerializedName("Ubicacion") val ubicacion: String,
    @SerializedName("Estado") val estado: String
)

data class JobOfferCompanyDto(
    @SerializedName("IDEmpresa") val idEmpresa: Int? = null,
    @SerializedName("IDUsuario") val idUsuario: Int? = null,
    @SerializedName("NombreEmpresa") val nombreEmpresa: String? = null,
    @SerializedName("Foto") val foto: String? = null
)

data class JobApplicationRequest(
    @SerializedName("IDOferta") val idOferta: Int
)

data class JobApplicationUpdateRequest(
    @SerializedName("Estado") val estado: String
)

data class JobApplicationResponse(
    @SerializedName("StatusCode") val statusCode: Int? = null,
    @SerializedName("ReasonPhrase") val reasonPhrase: String? = null,
    @SerializedName("Message") val message: String? = null,
    @SerializedName("Data") val dataUpper: JobApplicationDto? = null,
    @SerializedName("data") val dataLower: JobApplicationDto? = null
)

data class JobApplicationListResponse(
    @SerializedName("StatusCode") val statusCode: Int? = null,
    @SerializedName("ReasonPhrase") val reasonPhrase: String? = null,
    @SerializedName("Message") val message: String? = null,
    @SerializedName("data") val data: JobApplicationPageDto? = null
)

data class JobApplicationPageDto(
    @SerializedName("data") val data: List<JobApplicationDto> = emptyList(),
    @SerializedName("current_page") val currentPage: Int = 1,
    @SerializedName("last_page") val lastPage: Int = 1
)

data class JobApplicationDto(
    @SerializedName("IDAplicacion") val idAplicacion: Int,
    @SerializedName("IDDesempleado") val idDesempleado: Int? = null,
    @SerializedName("IDOferta") val idOferta: Int? = null,
    @SerializedName("Estado") val estado: String = "",
    @SerializedName("FechaAplicacion") val fechaAplicacion: String? = null,
    @SerializedName("desempleado") val desempleado: JobApplicantDto? = null
)

data class JobApplicantDto(
    @SerializedName("IDDesempleado") val idDesempleado: Int? = null,
    @SerializedName("IDUsuario") val idUsuario: Int? = null,
    @SerializedName("Nombre") val nombre: String? = null,
    @SerializedName("Apellido") val apellido: String? = null,
    @SerializedName("Foto") val foto: String? = null,
    @SerializedName("pivot") val pivot: JobApplicationPivotDto? = null
) {
    val displayName: String
        get() = listOfNotNull(nombre, apellido).joinToString(" ").takeIf { it.isNotBlank() }
            ?: "Desempleado"
}

data class JobApplicationPivotDto(
    @SerializedName("IDAplicacion") val idAplicacion: Int? = null,
    @SerializedName("IDOferta") val idOferta: Int? = null,
    @SerializedName("IDDesempleado") val idDesempleado: Int? = null,
    @SerializedName("Estado") val estado: String = "",
    @SerializedName("FechaAplicacion") val fechaAplicacion: String? = null
) {
    fun toApplication(idDesempleado: Int?, idOferta: Int?): JobApplicationDto? {
        val applicationId = idAplicacion ?: return null
        return JobApplicationDto(
            idAplicacion = applicationId,
            idDesempleado = this.idDesempleado ?: idDesempleado,
            idOferta = this.idOferta ?: idOferta,
            estado = estado,
            fechaAplicacion = fechaAplicacion
        )
    }
}
