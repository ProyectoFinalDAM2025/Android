package leo.rios.officium.jobOffers.domain

import leo.rios.officium.core.api.ApiService
import leo.rios.officium.core.api.toApiMessage
import leo.rios.officium.core.database.dao.ProvinciaDao
import leo.rios.officium.core.database.entity.ProvinciaEntity
import leo.rios.officium.empresaProfile.data.ProvinciaData
import leo.rios.officium.jobOffers.data.JobApplicationDto
import leo.rios.officium.jobOffers.data.JobApplicationRequest
import leo.rios.officium.jobOffers.data.JobApplicationUpdateRequest
import leo.rios.officium.jobOffers.data.JobOfferDto
import leo.rios.officium.jobOffers.data.JobOfferRequest
import leo.rios.officium.jobOffers.data.JobOfferUpdateRequest
import leo.rios.officium.subscriptions.data.CategoriaDto
import javax.inject.Inject

class JobOffersRepository @Inject constructor(
    private val apiService: ApiService,
    private val provinciaDao: ProvinciaDao
) {
    suspend fun getMyOffers(page: Int): Result<Pair<List<JobOfferDto>, Boolean>> {
        return try {
            val response = apiService.apiGetMyJobOffers(page)
            val body = response.body()
            val pageData = body?.page
            if (response.isSuccessful && pageData != null) {
                Result.success(pageData.data to (pageData.currentPage < pageData.lastPage))
            } else {
                Result.failure(Exception(response.errorBody()?.string().toApiMessage() ?: body?.message ?: "No se pudieron cargar tus ofertas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOffer(offerId: Int): Result<JobOfferDto> {
        return try {
            val response = apiService.apiGetJobOffer(offerId)
            val body = response.body()
            val offer = body?.data
            if (response.isSuccessful && offer != null) {
                Result.success(offer)
            } else {
                Result.failure(Exception(response.errorBody()?.string().toApiMessage() ?: body?.message ?: "No se pudo cargar la oferta"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchOffers(
        title: String?,
        location: String?,
        category: String?,
        status: String?,
        page: Int
    ): Result<Pair<List<JobOfferDto>, Boolean>> {
        return try {
            val response = apiService.apiSearchJobOffers(
                title = title.takeUnless { it.isNullOrBlank() },
                location = location.takeUnless { it.isNullOrBlank() },
                category = category.takeUnless { it.isNullOrBlank() },
                status = status.takeUnless { it.isNullOrBlank() },
                page = page
            )
            val body = response.body()
            val pageData = body?.page
            if (response.isSuccessful && pageData != null) {
                Result.success(pageData.data to (pageData.currentPage < pageData.lastPage))
            } else {
                Result.failure(Exception(response.errorBody()?.string().toApiMessage() ?: body?.message ?: "No se pudieron buscar ofertas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<CategoriaDto>> {
        return try {
            val response = apiService.apiGetCategorias()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(response.errorBody()?.string().toApiMessage() ?: body?.message ?: "No se pudieron cargar categorias"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProvincias(): Result<List<ProvinciaData>> {
        return try {
            val localProvincias = provinciaDao.getAll()
            if (localProvincias.isNotEmpty()) {
                Result.success(localProvincias.map { it.toData() })
            } else {
                val response = apiService.apiGetProvincias()
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    provinciaDao.insertAll(body.data.map { it.toEntity() })
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(response.errorBody()?.string().toApiMessage() ?: body?.message ?: "No se pudieron cargar provincias"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createOffer(
        categoryId: Int,
        title: String,
        description: String,
        location: String,
        status: String
    ): Result<JobOfferDto> {
        return try {
            val response = apiService.apiCreateJobOffer(
                JobOfferRequest(
                    idCategoria = categoryId,
                    titulo = title,
                    descripcion = description,
                    ubicacion = location,
                    estado = status
                )
            )
            val offer = response.body()?.data
            if (response.isSuccessful && offer != null) {
                Result.success(offer)
            } else {
                Result.failure(Exception(response.errorBody()?.string().toApiMessage() ?: response.body()?.message ?: "No se pudo crear la oferta"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOffer(
        offerId: Int,
        categoryId: Int,
        title: String,
        description: String,
        location: String,
        status: String
    ): Result<JobOfferDto> {
        return try {
            val response = apiService.apiUpdateJobOffer(
                id = offerId,
                request = JobOfferUpdateRequest(
                    idCategoria = categoryId,
                    titulo = title,
                    descripcion = description,
                    ubicacion = location,
                    estado = status
                )
            )
            val offer = response.body()?.data
            if (response.isSuccessful && offer != null) {
                Result.success(offer)
            } else {
                Result.failure(Exception(response.errorBody()?.string().toApiMessage() ?: response.body()?.message ?: "No se pudo actualizar la oferta"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun applyToOffer(offerId: Int): Result<Unit> {
        return try {
            val response = apiService.apiApplyToJobOffer(JobApplicationRequest(offerId))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string().toApiMessage() ?: response.body()?.message ?: "No se pudo aplicar a la oferta"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getApplications(offerId: Int): Result<List<JobApplicationDto>> {
        return try {
            val response = apiService.apiGetJobApplications(offerId)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data.data)
            } else {
                Result.failure(Exception(response.errorBody()?.string().toApiMessage() ?: body?.message ?: "No se pudieron cargar aplicaciones"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateApplicationStatus(applicationId: Int, status: String): Result<Unit> {
        return try {
            val response = apiService.apiUpdateJobApplication(
                applicationId = applicationId,
                request = JobApplicationUpdateRequest(status)
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string().toApiMessage() ?: response.body()?.message ?: "No se pudo actualizar la aplicacion"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteApplication(applicationId: Int): Result<Unit> {
        return try {
            val response = apiService.apiDeleteJobApplication(applicationId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string().toApiMessage() ?: "No se pudo eliminar la aplicacion"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun ProvinciaEntity.toData(): ProvinciaData =
        ProvinciaData(id = id, ine = ine, name = name)

    private fun ProvinciaData.toEntity(): ProvinciaEntity =
        ProvinciaEntity(id = id, ine = ine, name = name)
}
