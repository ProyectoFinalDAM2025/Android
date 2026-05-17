package leo.rios.officium.empresaProfile.domain

import android.util.Log
import leo.rios.officium.core.api.ApiService
import leo.rios.officium.core.api.toApiMessage
import leo.rios.officium.core.database.dao.ProvinciaDao
import leo.rios.officium.core.database.dao.SectorDao
import leo.rios.officium.core.database.entity.ProvinciaEntity
import leo.rios.officium.core.database.entity.SectorEntity
import leo.rios.officium.empresaProfile.data.ProvinciaData
import leo.rios.officium.empresaProfile.data.SectorData
import leo.rios.officium.empresaProfile.presentation.model.EmpresaProfileModel
import leo.rios.officium.verifyProfile.data.RegisterClientResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class EmpresaProfileRepository @Inject constructor(
    private val apiService: ApiService,
    private val sectorDao: SectorDao,
    private val provinciaDao: ProvinciaDao
) {
    suspend fun getSectors(): Result<List<SectorData>> {
        return try {
            val localSectors = sectorDao.getAll()
            if (localSectors.isNotEmpty()) {
                return Result.success(localSectors.map { it.toData() })
            }

            val response = apiService.apiGetSectors()
            val body = response.body()

            if (response.isSuccessful && body != null) {
                sectorDao.insertAll(body.data.map { it.toEntity() })
                Result.success(body.data)
            } else {
                val serverMessage = response.errorBody()?.string().toApiMessage()
                Result.failure(Exception(serverMessage ?: "Error al obtener sectores"))
            }
        } catch (e: Exception) {
            Log.e("EmpresaProfile", "Error obteniendo sectores: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getProvincias(): Result<List<ProvinciaData>> {
        return try {
            val localProvincias = provinciaDao.getAll()
            if (localProvincias.isNotEmpty()) {
                return Result.success(localProvincias.map { it.toData() })
            }

            val response = apiService.apiGetProvincias()
            val body = response.body()

            if (response.isSuccessful && body != null) {
                provinciaDao.insertAll(body.data.map { it.toEntity() })
                Result.success(body.data)
            } else {
                val serverMessage = response.errorBody()?.string().toApiMessage()
                Result.failure(Exception(serverMessage ?: "Error al obtener provincias"))
            }
        } catch (e: Exception) {
            Log.e("EmpresaProfile", "Error obteniendo provincias: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun SectorData.toEntity(): SectorEntity {
        return SectorEntity(
            idSector = idSector,
            nombre = nombre
        )
    }

    private fun SectorEntity.toData(): SectorData {
        return SectorData(
            idSector = idSector,
            nombre = nombre
        )
    }

    private fun ProvinciaData.toEntity(): ProvinciaEntity {
        return ProvinciaEntity(
            id = id,
            ine = ine,
            name = name
        )
    }

    private fun ProvinciaEntity.toData(): ProvinciaData {
        return ProvinciaData(
            id = id,
            ine = ine,
            name = name
        )
    }

    suspend fun sendEmpresaProfileRepository(
        profile: EmpresaProfileModel,
        foto: MultipartBody.Part?
    ): Result<RegisterClientResponse> {
        return try {
            val response = apiService.apiRegisterCompanyProfile(
                idUsuario = profile.idUsuario.toRequestBody("text/plain".toMediaTypeOrNull()),
                nombreEmpresa = profile.nombreEmpresa.toRequestBody("text/plain".toMediaTypeOrNull()),
                cif = profile.cif.toRequestBody("text/plain".toMediaTypeOrNull()),
                idSector = profile.idSector.toRequestBody("text/plain".toMediaTypeOrNull()),
                ubicacion = profile.ubicacion.toRequestBody("text/plain".toMediaTypeOrNull()),
                sitioWeb = profile.sitioWeb.toRequestBody("text/plain".toMediaTypeOrNull()),
                picture = foto
            )

            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("EmpresaProfile", "Error ${response.code()} ${response.message()} $errorBody")
                val serverMessage = errorBody.toApiMessage()

                Result.failure(Exception(serverMessage ?: "Error al crear perfil de empresa"))
            }
        } catch (e: Exception) {
            Log.e("EmpresaProfile", "Error en la llamada a la API: ${e.message}", e)
            Result.failure(e)
        }
    }
}
