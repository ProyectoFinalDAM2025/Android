package leo.rios.officium.login.data

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("StatusCode") val statusCode: Int,
    @SerializedName("ReasonPhrase") val reasonPhrase: String,
    @SerializedName("Message") val message: String,
    @SerializedName("Data") val data: AuthData?
)

data class AuthData(
    val token: String,
    val profile: JsonObject?,
    val rol: String
)



data class ApiMessageResponse(
    @SerializedName("StatusCode") val statusCode: Int? = null,
    @SerializedName("ReasonPhrase") val reasonPhrase: String? = null,
    @SerializedName("Message") val message: String? = null
)

data class AuthenticatedUserResponse(
    @SerializedName("IDUsuario") val idUsuario: Int,
    val email: String,
    @SerializedName("email_verified_at") val emailVerifiedAt: String?
)
