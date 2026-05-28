package leo.rios.officium.jobOffers.presentation.model

data class JobOfferFormState(
    val title: String = "",
    val description: String = "",
    val categoryId: Int? = null,
    val categoryName: String = "",
    val location: String = "",
    val status: String = "Abierta"
)

val jobOfferStatusOptions = listOf("Abierta", "Cerrada", "En Proceso")

val jobApplicationStatusOptions = listOf("Abierta", "Pendiente", "Rechazada")
