package leo.rios.officium.subscriptions.data

import com.google.gson.annotations.SerializedName
import leo.rios.officium.jobOffers.data.JobOfferDto

data class MyApplicationsResponse(
    @SerializedName("StatusCode") val statusCode: Int? = null,
    @SerializedName("ReasonPhrase") val reasonPhrase: String? = null,
    @SerializedName("Message") val message: String? = null,
    @SerializedName("data") val data: MyApplicationsPageDto? = null
)

data class MyApplicationsPageDto(
    @SerializedName("data") val data: List<MyApplicationsUserDto> = emptyList(),
    @SerializedName("current_page") val currentPage: Int = 1,
    @SerializedName("last_page") val lastPage: Int = 1
)

data class MyApplicationsUserDto(
    @SerializedName("IDDesempleado") val idDesempleado: Int? = null,
    @SerializedName("ofertas_aplicadas") val appliedOffers: List<JobOfferDto> = emptyList()
)
