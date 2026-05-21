package leo.rios.officium.desempleadoProfile.presentation.model

data class DesempleadoProfileModel(
    val idUsuario: String,
    val nombre: String,
    val apellido: String,
    val dni: String,
    val porfolios: String,
    val disponibilidad: String,
    val ubicacion: String
)
