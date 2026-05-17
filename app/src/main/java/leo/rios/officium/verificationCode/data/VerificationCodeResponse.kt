package leo.rios.officium.verificationCode.data


import com.google.gson.annotations.SerializedName

data class VerificationCodeResponse(
    @SerializedName("StatusCode") val statusCode: Int,
    @SerializedName("ReasonPhrase") val reasonPhrase: String,
    @SerializedName("Message") val message: String
)