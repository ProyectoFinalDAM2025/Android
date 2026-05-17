package leo.rios.officium.registro.data

import com.google.gson.annotations.SerializedName

data class RegisterResponse (
    @SerializedName("StatusCode") val statusCode : Int,
    @SerializedName("ReasonPhrase") val reasonPhrase : String,
    @SerializedName("Message") val message : String,
    @SerializedName("Data") val data: DataRegister?
)

data class DataRegister(
    val token : String,
    val email : String,
)

