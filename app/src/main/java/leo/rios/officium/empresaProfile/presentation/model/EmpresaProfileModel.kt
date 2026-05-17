package leo.rios.officium.empresaProfile.presentation.model

data class EmpresaProfileModel(
    val idUsuario: String,
    val nombreEmpresa: String,
    val cif: String,
    val idSector: String,
    val ubicacion: String,
    val sitioWeb: String
)
