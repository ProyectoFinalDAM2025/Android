package leo.rios.officium.userProfile.data

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class UserProfileResponse(
    @SerializedName("StatusCode") val statusCode: Int,
    @SerializedName("ReasonPhrase") val reasonPhrase: String? = null,
    @SerializedName("Message") val message: String? = null,
    @SerializedName("Data") val data: JsonObject? = null,
    @SerializedName("Rol") val rol: String? = null
)
