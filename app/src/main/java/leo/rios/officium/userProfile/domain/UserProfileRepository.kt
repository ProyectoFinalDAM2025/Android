package leo.rios.officium.userProfile.domain

import android.util.Log
import com.google.gson.JsonObject
import leo.rios.officium.core.api.ApiService
import leo.rios.officium.core.api.toApiMessage
import leo.rios.officium.core.database.dao.ProvinciaDao
import leo.rios.officium.core.database.dao.SectorDao
import leo.rios.officium.core.database.entity.ProvinciaEntity
import leo.rios.officium.core.database.entity.SectorEntity
import leo.rios.officium.empresaProfile.data.ProvinciaData
import leo.rios.officium.empresaProfile.data.SectorData
import leo.rios.officium.userProfile.data.DocumentoDto
import leo.rios.officium.userProfile.data.PublicacionDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class UserProfileRepository @Inject constructor(
    private val apiService: ApiService,
    private val sectorDao: SectorDao,
    private val provinciaDao: ProvinciaDao
) {

    suspend fun getMyPhotos(): Result<List<DocumentoDto>> = loadDocuments(
        fallbackMessage = "No se pudieron cargar las fotos",
        request = { apiService.apiGetMyPhotos() }
    )

    suspend fun getMyVideos(): Result<List<DocumentoDto>> = loadDocuments(
        fallbackMessage = "No se pudieron cargar los videos",
        request = { apiService.apiGetMyVideos() }
    )

    suspend fun getMyPdfs(): Result<List<DocumentoDto>> = loadDocuments(
        fallbackMessage = "No se pudieron cargar los PDF",
        request = { apiService.apiGetMyPdfs() }
    )

    suspend fun getMyPublications(): Result<List<PublicacionDto>> {
        return try {
            val response = apiService.apiGetMyPublications()
            val body = response.body()

            if (response.isSuccessful && body != null) {
                Result.success(body.data)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody.toApiMessage() ?: "No se pudieron cargar las publicaciones"))
            }
        } catch (e: Exception) {
            Log.e("UserProfile", "Error obteniendo publicaciones: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun createPublication(
        content: String,
        fileType: String?,
        file: MultipartBody.Part?
    ): Result<Unit> {
        return try {
            val response = apiService.apiCreatePublication(
                contenido = content.toPlainRequestBody(),
                tipoArchivo = fileType?.toPlainRequestBody(),
                archivo = file
            )
            response.toUnitResult("No se pudo crear la publicacion")
        } catch (e: Exception) {
            Log.e("UserProfile", "Error creando publicacion: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun createDocument(
        type: String,
        description: String,
        file: MultipartBody.Part
    ): Result<Unit> {
        return try {
            val response = apiService.apiCreateDocument(
                tipo = type.toPlainRequestBody(),
                descripcion = description.takeIf { it.isNotBlank() }?.toPlainRequestBody(),
                archivo = file
            )
            response.toUnitResult("No se pudo subir el documento")
        } catch (e: Exception) {
            Log.e("UserProfile", "Error subiendo documento: ${e.message}", e)
            Result.failure(e)
        }
    }

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
            Log.e("UserProfile", "Error obteniendo sectores: ${e.message}", e)
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
            Log.e("UserProfile", "Error obteniendo provincias: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateDesempleado(
        idProfile: String,
        nombre: String,
        apellido: String,
        dni: String,
        porfolios: String,
        disponibilidad: String,
        ubicacion: String
    ): Result<JsonObject> {
        return try {
            val response = apiService.apiUpdateDesempleadoProfile(
                id = idProfile,
                method = "PUT".toPlainRequestBody(),
                nombre = nombre.toPlainRequestBody(),
                apellido = apellido.toPlainRequestBody(),
                dni = dni.toPlainRequestBody(),
                porfolios = porfolios.toPlainRequestBody(),
                disponibilidad = disponibilidad.toPlainRequestBody(),
                ubicacion = ubicacion.toPlainRequestBody()
            )
            response.toProfileResult("No se pudo actualizar el perfil de desempleado")
        } catch (e: Exception) {
            Log.e("UserProfile", "Error actualizando desempleado: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateEmpresa(
        idProfile: String,
        nombreEmpresa: String,
        cif: String,
        idSector: String,
        ubicacion: String,
        sitioWeb: String
    ): Result<JsonObject> {
        return try {
            val response = apiService.apiUpdateEmpresaProfile(
                id = idProfile,
                method = "PUT".toPlainRequestBody(),
                nombreEmpresa = nombreEmpresa.toPlainRequestBody(),
                cif = cif.toPlainRequestBody(),
                idSector = idSector.toPlainRequestBody(),
                ubicacion = ubicacion.toPlainRequestBody(),
                sitioWeb = sitioWeb.toPlainRequestBody()
            )
            response.toProfileResult("No se pudo actualizar el perfil de empresa")
        } catch (e: Exception) {
            Log.e("UserProfile", "Error actualizando empresa: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun loadDocuments(
        fallbackMessage: String,
        request: suspend () -> retrofit2.Response<leo.rios.officium.userProfile.data.DocumentoListResponse>
    ): Result<List<DocumentoDto>> {
        return try {
            val response = request()
            val body = response.body()

            if (response.isSuccessful && body != null) {
                Result.success(body.data)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("UserProfile", "Error servidor: $errorBody")
                Result.failure(Exception(errorBody.toApiMessage() ?: fallbackMessage))
            }
        } catch (e: Exception) {
            Log.e("UserProfile", "Error en la llamada a la API: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun String.toPlainRequestBody(): RequestBody =
        toRequestBody("text/plain".toMediaTypeOrNull())

    private fun SectorData.toEntity(): SectorEntity =
        SectorEntity(idSector = idSector, nombre = nombre)

    private fun SectorEntity.toData(): SectorData =
        SectorData(idSector = idSector, nombre = nombre)

    private fun ProvinciaData.toEntity(): ProvinciaEntity =
        ProvinciaEntity(id = id, ine = ine, name = name)

    private fun ProvinciaEntity.toData(): ProvinciaData =
        ProvinciaData(id = id, ine = ine, name = name)

    private fun retrofit2.Response<leo.rios.officium.userProfile.data.ProfileUpdateResponse>.toProfileResult(
        fallbackMessage: String
    ): Result<JsonObject> {
        val body = body()
        return if (isSuccessful && body?.data?.profile != null) {
            Result.success(body.data.profile)
        } else {
            val errorBody = errorBody()?.string()
            Log.e("UserProfile", "Error servidor: $errorBody")
            Result.failure(Exception(errorBody.toApiMessage() ?: body?.message ?: fallbackMessage))
        }
    }

    private fun retrofit2.Response<*>.toUnitResult(fallbackMessage: String): Result<Unit> {
        return if (isSuccessful) {
            Result.success(Unit)
        } else {
            val errorBody = errorBody()?.string()
            Result.failure(Exception(errorBody.toApiMessage() ?: fallbackMessage))
        }
    }
}
