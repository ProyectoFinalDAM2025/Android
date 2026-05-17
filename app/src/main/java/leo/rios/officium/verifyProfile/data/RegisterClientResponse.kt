package leo.rios.officium.verifyProfile.data

data class RegisterClientResponse(
    val status : String,
    val message : String,
    val data : RegisterClientData
)

data class RegisterClientData(
    val user : SaverUser
)

data class SaverUser (
    val _id: String,
    val idUsuario: String,
    val nombre: String,
    val apellido: String,
    val dni: String,
    val rol: String,
    val date: String,
    val ciudad: String,
    val sexo: String,
    val registro: String,
    val rutaFoto: String,
    val baja: String,
)