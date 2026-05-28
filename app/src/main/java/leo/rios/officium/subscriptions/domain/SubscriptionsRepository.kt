package leo.rios.officium.subscriptions.domain

import leo.rios.officium.core.api.ApiService
import leo.rios.officium.core.api.toApiMessage
import leo.rios.officium.jobOffers.data.JobOfferDto
import leo.rios.officium.subscriptions.data.CategoriaDto
import leo.rios.officium.subscriptions.data.CategoriaResponse
import leo.rios.officium.subscriptions.data.SubscriptionRequest
import javax.inject.Inject

class SubscriptionsRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getAvailableCategories(): Result<List<CategoriaDto>> =
        loadCategories("No se pudieron cargar las categorias disponibles") {
            apiService.apiGetAvailableCategorias()
        }

    suspend fun getSubscribedCategories(): Result<List<CategoriaDto>> =
        loadCategories("No se pudieron cargar tus suscripciones") {
            apiService.apiGetMySubscriptions()
        }

    suspend fun getMyApplications(): Result<List<JobOfferDto>> {
        return try {
            val response = apiService.apiGetMyApplications()
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data.data.flatMap { it.appliedOffers })
            } else {
                Result.failure(Exception(response.errorBody()?.string().toApiMessage() ?: body?.message ?: "No se pudieron cargar tus aplicaciones"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun subscribe(categoryId: Int): Result<Unit> =
        safeUnitCall("No se pudo crear la suscripcion") {
            apiService.apiAddSubscription(SubscriptionRequest(categoryId))
        }

    suspend fun unsubscribe(categoryId: Int): Result<Unit> =
        safeUnitCall("No se pudo eliminar la suscripcion") {
            apiService.apiDeleteSubscription(SubscriptionRequest(categoryId))
        }

    suspend fun deleteApplication(applicationId: Int): Result<Unit> =
        safeUnitCall("No se pudo eliminar la aplicacion") {
            apiService.apiDeleteJobApplication(applicationId)
        }

    private suspend fun loadCategories(
        fallbackMessage: String,
        request: suspend () -> retrofit2.Response<CategoriaResponse>
    ): Result<List<CategoriaDto>> {
        return try {
            val response = request()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(response.errorBody()?.string().toApiMessage() ?: body?.message ?: fallbackMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun safeUnitCall(
        fallbackMessage: String,
        request: suspend () -> retrofit2.Response<*>
    ): Result<Unit> {
        return try {
            val response = request()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string().toApiMessage() ?: fallbackMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
