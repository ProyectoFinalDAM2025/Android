package leo.rios.officium.recover.data

import com.google.gson.annotations.SerializedName

data class RecoverResponse(
    @SerializedName("StatusCode") val statusCode: Int,
    @SerializedName("ReasonPhrase") val reasonPhrase: String,
    @SerializedName("Message") val message: String
)
