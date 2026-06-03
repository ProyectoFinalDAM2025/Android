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
import leo.rios.officium.userProfile.data.ComentarioRequest
import leo.rios.officium.userProfile.data.ComentarioUpdateRequest
import leo.rios.officium.userProfile.data.DocumentoDto
import leo.rios.officium.userProfile.data.PublicacionDto
import leo.rios.officium.userProfile.data.ReportePerfilRequest
import leo.rios.officium.userProfile.data.ReportePublicacionRequest
import leo.rios.officium.userProfile.data.UserProfileResponse
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

    suspend fun getPhotosByUser(userId: Int): Result<List<DocumentoDto>> = loadDocuments(
        fallbackMessage = "No se pudieron cargar las fotos",
        request = { apiService.apiGetPhotosByUser(userId) }
    )

    suspend fun getMyVideos(): Result<List<DocumentoDto>> = loadDocuments(
        fallbackMessage = "No se pudieron cargar los videos",
        request = { apiService.apiGetMyVideos() }
    )

    suspend fun getVideosByUser(userId: Int): Result<List<DocumentoDto>> = loadDocuments(
        fallbackMessage = "No se pudieron cargar los videos",
        request = { apiService.apiGetVideosByUser(userId) }
    )

    suspend fun getMyPdfs(): Result<List<DocumentoDto>> = loadDocuments(
        fallbackMessage = "No se pudieron cargar los PDF",
        request = { apiService.apiGetMyPdfs() }
    )

    suspend fun getPdfsByUser(userId: Int): Result<List<DocumentoDto>> = loadDocuments(
        fallbackMessage = "No se pudieron cargar los PDF",
        request = { apiService.apiGetPdfsByUser(userId) }
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

    suspend fun getPublicationsByUser(userId: Int): Result<List<PublicacionDto>> {
        return try {
            val response = apiService.apiGetPublicationsByUser(userId)
            val body = response.body()

            if (response.isSuccessful && body != null) {
                Result.success(body.data)
            } else if (response.code() == 404) {
                Result.success(emptyList())
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody.toApiMessage() ?: "No se pudieron cargar las publicaciones"))
            }
        } catch (e: Exception) {
            Log.e("UserProfile", "Error obteniendo publicaciones de usuario: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(userId: Int): Result<UserProfileResponse> {
        return try {
            val response = apiService.apiGetUserProfile(userId)
            val body = response.body()

            if (response.isSuccessful && body?.data != null) {
                Result.success(body)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody.toApiMessage() ?: body?.message ?: "No se pudo cargar el perfil"))
            }
        } catch (e: Exception) {
            Log.e("UserProfile", "Error obteniendo perfil de usuario: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getPublications(page: Int): Result<Pair<List<PublicacionDto>, Boolean>> {
        return try {
            val response = apiService.apiGetPublications(page)
            val body = response.body()
            val pageData = body?.data

            if (response.isSuccessful && pageData != null) {
                Result.success(pageData.data to (pageData.currentPage < pageData.lastPage))
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody.toApiMessage() ?: "No se pudieron cargar las publicaciones"))
            }
        } catch (e: Exception) {
            Log.e("UserProfile", "Error obteniendo feed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getPublication(id: Int): Result<PublicacionDto> {
        return try {
            val response = apiService.apiGetPublication(id)
            val body = response.body()

            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody.toApiMessage() ?: body?.message ?: "No se pudo cargar la publicacion"))
            }
        } catch (e: Exception) {
            Log.e("UserProfile", "Error obteniendo publicacion: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun createPublication(
        content: String,
        fileType: String?,
        file: MultipartBody.Part?,
        thumbnail: MultipartBody.Part? = null
    ): Result<Unit> {
        return try {
            val response = apiService.apiCreatePublication(
                contenido = content.toPlainRequestBody(),
                tipoArchivo = fileType?.toPlainRequestBody(),
                archivo = file,
                thumbnail = thumbnail
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
        file: MultipartBody.Part,
        thumbnail: MultipartBody.Part? = null,
        targetUserId: Int? = null
    ): Result<DocumentoDto> {
        return try {
            val response = apiService.apiCreateDocument(
                idUsuario = targetUserId?.toString()?.toPlainRequestBody(),
                tipo = type.toPlainRequestBody(),
                descripcion = description.takeIf { it.isNotBlank() }?.toPlainRequestBody(),
                archivo = file,
                thumbnail = thumbnail
            )
            response.toDocumentResult("No se pudo subir el documento")
        } catch (e: Exception) {
            Log.e("UserProfile", "Error subiendo documento: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateDocument(
        id: Int,
        description: String,
        file: MultipartBody.Part?,
        thumbnail: MultipartBody.Part? = null
    ): Result<DocumentoDto> {
        return try {
            val response = apiService.apiUpdateDocument(
                id = id,
                method = "PUT".toPlainRequestBody(),
                descripcion = description.takeIf { it.isNotBlank() }?.toPlainRequestBody(),
                archivo = file,
                thumbnail = thumbnail
            )
            response.toDocumentResult("No se pudo actualizar el documento")
        } catch (e: Exception) {
            Log.e("UserProfile", "Error actualizando documento: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteDocument(id: Int): Result<Unit> {
        return try {
            val response = apiService.apiDeleteDocument(id)
            response.toUnitResult("No se pudo eliminar el documento")
        } catch (e: Exception) {
            Log.e("UserProfile", "Error eliminando documento: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteProfile(role: String?, profileId: String): Result<Unit> {
        return try {
            val response = when (role) {
                "Empresa" -> apiService.apiDeleteEmpresaProfile(profileId)
                "Desempleado" -> apiService.apiDeleteDesempleadoProfile(profileId)
                "Administrador" -> apiService.apiDeleteAdministradorProfile(profileId)
                else -> return Result.failure(Exception("Tipo de perfil no valido"))
            }

            response.toUnitResult("No se pudo eliminar el usuario")
        } catch (e: Exception) {
            Log.e("UserProfile", "Error eliminando perfil: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun likePublication(id: Int): Result<Unit> =
        safeUnitCall("No se pudo dar like") { apiService.apiLikePublication(id) }

    suspend fun unlikePublication(id: Int): Result<Unit> =
        safeUnitCall("No se pudo quitar el like") { apiService.apiUnlikePublication(id) }

    suspend fun addComment(publicationId: Int, content: String): Result<Unit> =
        safeUnitCall("No se pudo comentar") {
            apiService.apiCreateComment(ComentarioRequest(publicationId, content))
        }

    suspend fun updateComment(commentId: Int, content: String): Result<Unit> =
        safeUnitCall("No se pudo actualizar el comentario") {
            apiService.apiUpdateComment(commentId, ComentarioUpdateRequest(content))
        }

    suspend fun deleteComment(commentId: Int): Result<Unit> =
        safeUnitCall("No se pudo eliminar el comentario") {
            apiService.apiDeleteComment(commentId)
        }

    suspend fun updatePublication(
        id: Int,
        content: String,
        fileType: String?,
        file: MultipartBody.Part?,
        thumbnail: MultipartBody.Part? = null
    ): Result<Unit> {
        return try {
            val response = apiService.apiUpdatePublication(
                id = id,
                method = "PUT".toPlainRequestBody(),
                contenido = content.toPlainRequestBody(),
                tipoArchivo = fileType?.toPlainRequestBody(),
                archivo = file,
                thumbnail = thumbnail
            )
            response.toUnitResult("No se pudo actualizar la publicacion")
        } catch (e: Exception) {
            Log.e("UserProfile", "Error actualizando publicacion: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deletePublication(id: Int): Result<Unit> =
        safeUnitCall("No se pudo eliminar la publicacion") { apiService.apiDeletePublication(id) }

    suspend fun reportPublication(
        id: Int,
        reason: String,
        description: String
    ): Result<Unit> =
        safeUnitCall("No se pudo reportar la publicacion") {
            apiService.apiReportPublication(
                ReportePublicacionRequest(
                    idPublicacion = id,
                    motivo = reason,
                    descripcion = description.takeIf { it.isNotBlank() }
                )
            )
        }

    suspend fun reportProfile(
        idUsuario: Int,
        reason: String,
        description: String
    ): Result<Unit> =
        safeUnitCall("No se pudo reportar el perfil") {
            apiService.apiReportProfile(
                ReportePerfilRequest(
                    idUsuarioReportado = idUsuario,
                    motivo = reason,
                    descripcion = description.takeIf { it.isNotBlank() }
                )
            )
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
        ubicacion: String,
        foto: MultipartBody.Part? = null
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
                ubicacion = ubicacion.toPlainRequestBody(),
                foto = foto
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
        sitioWeb: String,
        foto: MultipartBody.Part? = null
    ): Result<JsonObject> {
        return try {
            val response = apiService.apiUpdateEmpresaProfile(
                id = idProfile,
                method = "PUT".toPlainRequestBody(),
                nombreEmpresa = nombreEmpresa.toPlainRequestBody(),
                cif = cif.toPlainRequestBody(),
                idSector = idSector.toPlainRequestBody(),
                ubicacion = ubicacion.toPlainRequestBody(),
                sitioWeb = sitioWeb.toPlainRequestBody(),
                foto = foto
            )
            response.toProfileResult("No se pudo actualizar el perfil de empresa")
        } catch (e: Exception) {
            Log.e("UserProfile", "Error actualizando empresa: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateAdministrador(
        idProfile: String,
        nombre: String,
        apellido: String,
        fotoPerfil: MultipartBody.Part? = null
    ): Result<JsonObject> {
        return try {
            val response = apiService.apiUpdateAdministradorProfile(
                id = idProfile,
                method = "PUT".toPlainRequestBody(),
                nombre = nombre.toPlainRequestBody(),
                apellido = apellido.toPlainRequestBody(),
                fotoPerfil = fotoPerfil
            )
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("UserProfile", "Error servidor: $errorBody")
                Result.failure(Exception(errorBody.toApiMessage() ?: body?.message ?: "No se pudo actualizar el perfil administrador"))
            }
        } catch (e: Exception) {
            Log.e("UserProfile", "Error actualizando administrador: ${e.message}", e)
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

    private fun retrofit2.Response<leo.rios.officium.userProfile.data.DocumentoResponse>.toDocumentResult(
        fallbackMessage: String
    ): Result<DocumentoDto> {
        val body = body()
        val document = body?.data
        return if (isSuccessful && document != null) {
            Result.success(document)
        } else {
            val errorBody = errorBody()?.string()
            Result.failure(Exception(errorBody.toApiMessage() ?: body?.message ?: fallbackMessage))
        }
    }

    private suspend fun safeUnitCall(
        fallbackMessage: String,
        call: suspend () -> retrofit2.Response<*>
    ): Result<Unit> {
        return try {
            call().toUnitResult(fallbackMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
