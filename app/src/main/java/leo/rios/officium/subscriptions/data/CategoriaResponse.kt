package leo.rios.officium.subscriptions.data

import com.google.gson.annotations.SerializedName

data class CategoriaResponse(
    @SerializedName("StatusCode") val statusCode: Int? = null,
    @SerializedName("ReasonPhrase") val reasonPhrase: String? = null,
    @SerializedName("Message") val message: String? = null,
    @SerializedName("Data") val data: List<CategoriaDto> = emptyList()
)

data class CategoriaDto(
    @SerializedName("IDCategoria") val idCategoria: Int,
    @SerializedName("Nombre") val nombre: String
)

data class SubscriptionRequest(
    @SerializedName("IDCategoria") val idCategoria: Int
)
