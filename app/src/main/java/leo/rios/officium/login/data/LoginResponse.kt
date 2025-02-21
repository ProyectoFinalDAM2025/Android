package leo.rios.officium.login.data

import kotlinx.serialization.SerialName
import java.util.Date

data class LoginResponse(
    @SerialName("status")val status: String, //@SerialName : Como se resivira al dato, por lo que la variable puede llamar de otra manera.
    @SerialName("message")val message: String,
    @SerialName("data")val data : UserData
)

data class UserData (
    @SerialName("token")val token: String,
    @SerialName("appToken")val appToken: String,
    @SerialName("user")val user: User
)

data class User (
    @SerialName("_id")val _id: String,
    @SerialName("idUsuario")val idUsuario: String,
    @SerialName("nombre")val nombre: String,
    @SerialName("apellido")val apellido: String,
    @SerialName("dni")val dni: String,
    @SerialName("rol")val rol: String,
    @SerialName("date")val date: String,
    @SerialName("sexo")val sexo: String,
    @SerialName("rutaFoto")val rutaFoto: String,
    @SerialName("registro")val registro: Date,
)
