package leo.rios.officium.empresaProfile.data

import com.google.gson.annotations.SerializedName

data class SectorResponse(
    @SerializedName("StatusCode") val statusCode: Int,
    @SerializedName("ReasonPhrase") val reasonPhrase: String,
    @SerializedName("Message") val message: String,
    @SerializedName("Data") val data: List<SectorData>
)

data class SectorData(
    @SerializedName("IDSector") val idSector: Int,
    @SerializedName("Nombre") val nombre: String
)

data class ProvinciaResponse(
    @SerializedName("StatusCode") val statusCode: Int,
    @SerializedName("ReasonPhrase") val reasonPhrase: String,
    @SerializedName("Message") val message: String,
    @SerializedName("Data") val data: List<ProvinciaData>
)

data class ProvinciaData(
    val id: Int,
    @SerializedName("INE") val ine: String,
    @SerializedName("name") val name: String
)
