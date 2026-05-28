package leo.rios.officium.userProfile.presentation.viewModel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import leo.rios.officium.core.dataStore.DataStoreManager
import leo.rios.officium.login.data.getProfileId
import leo.rios.officium.login.data.getProfileName
import leo.rios.officium.login.data.getProfilePhoto
import leo.rios.officium.empresaProfile.data.ProvinciaData
import leo.rios.officium.empresaProfile.data.SectorData
import leo.rios.officium.userProfile.data.DocumentoDto
import leo.rios.officium.userProfile.data.PublicacionDto
import leo.rios.officium.userProfile.domain.UserProfileRepository
import leo.rios.officium.userProfile.presentation.model.ProfileUploadType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val repository: UserProfileRepository,
    private val application: Application
) : ViewModel() {

    private val _profileName = MutableStateFlow<String?>(null)
    val profileName: StateFlow<String?> = _profileName

    private val _profileRole = MutableStateFlow<String?>(null)
    val profileRole: StateFlow<String?> = _profileRole

    private val _profilePhoto = MutableStateFlow<String?>(null)
    val profilePhoto: StateFlow<String?> = _profilePhoto

    private val _currentUserPhoto = MutableStateFlow<String?>(null)
    val currentUserPhoto: StateFlow<String?> = _currentUserPhoto

    private val _profileDescription = MutableStateFlow<String?>(null)
    val profileDescription: StateFlow<String?> = _profileDescription

    private val _profileJson = MutableStateFlow<String?>(null)
    val profileJson: StateFlow<String?> = _profileJson

    private val _currentUserId = MutableStateFlow<Int?>(null)
    val currentUserId: StateFlow<Int?> = _currentUserId

    private val _currentUserRole = MutableStateFlow<String?>(null)
    val currentUserRole: StateFlow<String?> = _currentUserRole

    private val _viewedUserId = MutableStateFlow<Int?>(null)
    val viewedUserId: StateFlow<Int?> = _viewedUserId

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating

    private val _sectors = MutableStateFlow<List<SectorData>>(emptyList())
    val sectors: StateFlow<List<SectorData>> = _sectors

    private val _provincias = MutableStateFlow<List<ProvinciaData>>(emptyList())
    val provincias: StateFlow<List<ProvinciaData>> = _provincias

    private val _photos = MutableStateFlow<List<DocumentoDto>>(emptyList())
    val photos: StateFlow<List<DocumentoDto>> = _photos

    private val _videos = MutableStateFlow<List<DocumentoDto>>(emptyList())
    val videos: StateFlow<List<DocumentoDto>> = _videos

    private val _pdfs = MutableStateFlow<List<DocumentoDto>>(emptyList())
    val pdfs: StateFlow<List<DocumentoDto>> = _pdfs

    private val _publications = MutableStateFlow<List<PublicacionDto>>(emptyList())
    val publications: StateFlow<List<PublicacionDto>> = _publications

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private var requestedUserId: Int? = null

    init {
        loadProfile()
        loadCatalogs()
        loadDocuments()
    }

    fun openProfile(userId: Int?) {
        requestedUserId = userId
        loadProfile()
        loadDocuments()
    }

    fun refresh() {
        loadProfile()
        loadCatalogs()
        loadDocuments()
    }

    private fun loadProfile() = viewModelScope.launch {
        val localProfileJson = dataStoreManager.getProfileJson().firstOrNull()
        _currentUserRole.value = dataStoreManager.getRole().firstOrNull()
        _currentUserPhoto.value = dataStoreManager.getProfilePhoto().firstOrNull()
        val localUserId = localProfileJson.extractCurrentUserId()
        _currentUserId.value = localUserId

        val targetUserId = requestedUserId
        if (targetUserId == null || targetUserId == localUserId) {
            _profileName.value = dataStoreManager.getProfileName().firstOrNull()
            _profileRole.value = dataStoreManager.getRole().firstOrNull()
            _profilePhoto.value = dataStoreManager.getProfilePhoto().firstOrNull()
            _profileJson.value = localProfileJson
            _viewedUserId.value = localUserId
        } else {
            repository.getUserProfile(targetUserId)
                .onSuccess { response ->
                    val profile = response.data
                    val normalizedRole = response.rol.normalizeProfileRole()

                    _profileName.value = profile.getDisplayName(normalizedRole)
                    _profileRole.value = normalizedRole
                    _profilePhoto.value = profile?.getStringOrNull("Foto")
                    _profileJson.value = profile?.toString()
                    _viewedUserId.value = targetUserId
                }
                .onFailure { _message.value = it.localizedMessage ?: "Error al cargar perfil" }
        }
        updateProfileDescription()
    }

    private fun String?.extractCurrentUserId(): Int? {
        if (isNullOrBlank()) return null
        return runCatching {
            JsonParser.parseString(this).asJsonObject
                .get("IDUsuario")
                ?.takeIf { !it.isJsonNull }
                ?.asInt
        }.getOrNull()
    }

    private fun updateProfileDescription() {
        _profileDescription.value = _profileJson.value.toProfileDescription(
            role = _profileRole.value,
            sectors = _sectors.value
        )
    }

    private fun String?.toProfileDescription(
        role: String?,
        sectors: List<SectorData>
    ): String? {
        if (isNullOrBlank()) return null

        return try {
            val profile = JsonParser.parseString(this).asJsonObject
            val details = when (role) {
                "Empresa" -> {
                    val sectorName = profile.getStringOrNull("IDSector")
                        ?.let { idSector ->
                            sectors.firstOrNull { it.idSector.toString() == idSector }?.nombre
                        }

                    listOfNotNull(
                        sectorName,
                        profile.getStringOrNull("SitioWeb"),
                        profile.getStringOrNull("Ubicacion")
                    )
                }
                "Desempleado" -> listOfNotNull(
                    profile.getStringOrNull("Porfolios"),
                    profile.getStringOrNull("Disponibilidad"),
                    profile.getStringOrNull("Ubicacion")
                )
                else -> emptyList()
            }

            details.joinToString(" · ").takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }

    private fun com.google.gson.JsonObject.getStringOrNull(key: String): String? {
        return get(key)
            ?.takeIf { !it.isJsonNull }
            ?.asString
            ?.takeIf { it.isNotBlank() }
    }

    private fun String?.normalizeProfileRole(): String? {
        return when (this) {
            "Usuario" -> "Desempleado"
            "Empresa" -> "Empresa"
            "Desempleado" -> "Desempleado"
            else -> this
        }
    }

    private fun JsonObject?.getDisplayName(role: String?): String? {
        if (this == null) return null
        return when (role) {
            "Empresa" -> getStringOrNull("NombreEmpresa")
            "Desempleado" -> listOfNotNull(
                getStringOrNull("Nombre"),
                getStringOrNull("Apellido")
            ).joinToString(" ").takeIf { it.isNotBlank() }
            else -> null
        }
    }

    private fun loadDocuments() = viewModelScope.launch {
        val localUserId = dataStoreManager.getProfileJson().firstOrNull().extractCurrentUserId()
        val targetUserId = requestedUserId
        val isOwnerProfile = targetUserId == null || targetUserId == localUserId

        val publicationsResult = if (isOwnerProfile) {
            repository.getMyPublications()
        } else {
            repository.getPublicationsByUser(targetUserId)
        }
        val photosResult = if (isOwnerProfile) {
            repository.getMyPhotos()
        } else {
            repository.getPhotosByUser(targetUserId)
        }
        val videosResult = if (isOwnerProfile) {
            repository.getMyVideos()
        } else {
            repository.getVideosByUser(targetUserId)
        }
        val pdfsResult = if (isOwnerProfile) {
            repository.getMyPdfs()
        } else {
            repository.getPdfsByUser(targetUserId)
        }

        _publications.value = publicationsResult.getOrDefault(emptyList())
        _photos.value = photosResult.getOrDefault(emptyList())
        _videos.value = videosResult.getOrDefault(emptyList())
        _pdfs.value = pdfsResult.getOrDefault(emptyList())

        _message.value = listOf(publicationsResult, photosResult, videosResult, pdfsResult)
            .firstOrNull { it.isFailure }
            ?.exceptionOrNull()
            ?.localizedMessage
    }

    private fun loadCatalogs() = viewModelScope.launch {
        repository.getSectors()
            .onSuccess {
                _sectors.value = it
                updateProfileDescription()
            }
            .onFailure { _message.value = it.localizedMessage ?: "Error al obtener sectores" }

        repository.getProvincias()
            .onSuccess { _provincias.value = it }
            .onFailure { _message.value = it.localizedMessage ?: "Error al obtener provincias" }
    }

    fun updateDesempleado(
        nombre: String,
        apellido: String,
        dni: String,
        porfolios: String,
        disponibilidad: String,
        ubicacion: String
    ) = viewModelScope.launch {
        val idProfile = dataStoreManager.getIdProfile().firstOrNull()
        if (idProfile.isNullOrBlank()) {
            _message.value = "No se encontro el perfil local"
            return@launch
        }

        _isUpdating.value = true
        repository.updateDesempleado(
            idProfile = idProfile,
            nombre = nombre,
            apellido = apellido,
            dni = dni,
            porfolios = porfolios,
            disponibilidad = disponibilidad,
            ubicacion = ubicacion
        )
            .onSuccess { saveUpdatedProfile(it) }
            .onFailure { _message.value = it.localizedMessage ?: "Error al actualizar perfil" }
        _isUpdating.value = false
    }

    fun updateEmpresa(
        nombreEmpresa: String,
        cif: String,
        idSector: String,
        ubicacion: String,
        sitioWeb: String
    ) = viewModelScope.launch {
        val idProfile = dataStoreManager.getIdProfile().firstOrNull()
        if (idProfile.isNullOrBlank()) {
            _message.value = "No se encontro el perfil local"
            return@launch
        }

        _isUpdating.value = true
        repository.updateEmpresa(
            idProfile = idProfile,
            nombreEmpresa = nombreEmpresa,
            cif = cif,
            idSector = idSector,
            ubicacion = ubicacion,
            sitioWeb = sitioWeb
        )
            .onSuccess { saveUpdatedProfile(it) }
            .onFailure { _message.value = it.localizedMessage ?: "Error al actualizar perfil" }
        _isUpdating.value = false
    }

    fun createProfileContent(
        uploadType: ProfileUploadType,
        content: String,
        description: String,
        fileUri: Uri?
    ) = viewModelScope.launch {
        if (uploadType == ProfileUploadType.Publication && content.isBlank()) {
            _message.value = "Escribe el contenido de la publicacion"
            return@launch
        }

        if (uploadType != ProfileUploadType.Publication && fileUri == null) {
            _message.value = "Selecciona un archivo"
            return@launch
        }

        _isUpdating.value = true
        try {
            val filePart = withContext(Dispatchers.IO) {
                fileUri?.let { uriToFilePart(it) }
            }

            val result = if (uploadType == ProfileUploadType.Publication) {
                repository.createPublication(
                    content = content,
                    fileType = filePart?.let { fileUri?.inferPublicationFileType() },
                    file = filePart
                )
            } else {
                val documentFile = filePart
                if (documentFile == null) {
                    Result.failure(Exception("Selecciona un archivo"))
                } else {
                    repository.createDocument(
                        type = uploadType.documentType.orEmpty(),
                        description = description,
                        file = documentFile
                    )
                }
            }

            result
                .onSuccess {
                    _message.value = "${uploadType.title} subida correctamente"
                    loadDocuments()
                }
                .onFailure { _message.value = it.localizedMessage ?: "Error al subir contenido" }
        } finally {
            _isUpdating.value = false
        }
    }

    fun updateDocument(
        documentId: Int,
        description: String,
        fileUri: Uri?
    ) = viewModelScope.launch {
        _isUpdating.value = true
        try {
            val filePart = withContext(Dispatchers.IO) {
                fileUri?.let { uriToFilePart(it) }
            }

            repository.updateDocument(
                id = documentId,
                description = description,
                file = filePart
            )
                .onSuccess {
                    _message.value = "Contenido actualizado"
                    loadDocuments()
                }
                .onFailure { _message.value = it.localizedMessage ?: "Error al actualizar contenido" }
        } finally {
            _isUpdating.value = false
        }
    }

    fun deleteDocument(documentId: Int) = viewModelScope.launch {
        _isUpdating.value = true
        try {
            repository.deleteDocument(documentId)
                .onSuccess {
                    _message.value = "Contenido eliminado"
                    loadDocuments()
                }
                .onFailure { _message.value = it.localizedMessage ?: "Error al eliminar contenido" }
        } finally {
            _isUpdating.value = false
        }
    }

    fun likePublication(publicationId: Int, liked: Boolean) = viewModelScope.launch {
        val result = if (liked) {
            repository.unlikePublication(publicationId)
        } else {
            repository.likePublication(publicationId)
        }
        result
            .onSuccess { loadDocuments() }
            .onFailure { _message.value = it.localizedMessage ?: "Error al actualizar like" }
    }

    fun addComment(publicationId: Int, content: String) = viewModelScope.launch {
        if (content.isBlank()) {
            _message.value = "Escribe un comentario"
            return@launch
        }
        repository.addComment(publicationId, content)
            .onSuccess {
                _message.value = "Comentario publicado"
                loadDocuments()
            }
            .onFailure { _message.value = it.localizedMessage ?: "Error al comentar" }
    }

    fun updateComment(commentId: Int, content: String) = viewModelScope.launch {
        repository.updateComment(commentId, content)
            .onSuccess {
                _message.value = "Comentario actualizado"
                loadDocuments()
            }
            .onFailure { _message.value = it.localizedMessage ?: "Error al actualizar comentario" }
    }

    fun deleteComment(commentId: Int) = viewModelScope.launch {
        repository.deleteComment(commentId)
            .onSuccess {
                _message.value = "Comentario eliminado"
                loadDocuments()
            }
            .onFailure { _message.value = it.localizedMessage ?: "Error al eliminar comentario" }
    }

    fun updatePublication(
        publicationId: Int,
        content: String,
        fileUri: Uri?
    ) = viewModelScope.launch {
        _isUpdating.value = true
        try {
            val filePart = withContext(Dispatchers.IO) {
                fileUri?.let { uriToFilePart(it) }
            }
            repository.updatePublication(
                id = publicationId,
                content = content,
                fileType = filePart?.let { fileUri?.inferPublicationFileType() },
                file = filePart
            )
                .onSuccess {
                    _message.value = "Publicacion actualizada"
                    loadDocuments()
                }
                .onFailure { _message.value = it.localizedMessage ?: "Error al actualizar publicacion" }
        } finally {
            _isUpdating.value = false
        }
    }

    fun deletePublication(publicationId: Int) = viewModelScope.launch {
        repository.deletePublication(publicationId)
            .onSuccess {
                _message.value = "Publicacion eliminada"
                loadDocuments()
            }
            .onFailure { _message.value = it.localizedMessage ?: "Error al eliminar publicacion" }
    }

    fun reportPublication(publicationId: Int, reason: String, description: String) = viewModelScope.launch {
        if (reason.isBlank()) {
            _message.value = "Indica un motivo"
            return@launch
        }
        repository.reportPublication(publicationId, reason, description)
            .onSuccess { _message.value = "Reporte enviado" }
            .onFailure { _message.value = it.localizedMessage ?: "Error al reportar" }
    }

    private suspend fun saveUpdatedProfile(profile: JsonObject) {
        dataStoreManager.saveProfileBasicData(
            idProfile = profile.getProfileId(),
            profileName = profile.getProfileName(),
            profilePhoto = profile.getProfilePhoto(),
            profileJson = profile.toString()
        )
        _message.value = "Perfil actualizado"
        loadProfile()
    }

    private fun uriToFilePart(uri: Uri): MultipartBody.Part? {
        val resolver = application.contentResolver
        val mimeType = resolver.getType(uri) ?: "application/octet-stream"
        val extension = mimeType.substringAfter('/', "file").substringBefore('+')
        val file = File(application.cacheDir, "profile_content_${System.currentTimeMillis()}.$extension")

        return try {
            resolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }
            MultipartBody.Part.createFormData(
                "Archivo",
                file.name,
                file.asRequestBody(mimeType.toMediaTypeOrNull())
            )
        } catch (e: Exception) {
            Log.e("UserProfile", "Error preparando archivo", e)
            null
        }
    }

    private fun Uri.inferPublicationFileType(): String? {
        val mimeType = application.contentResolver.getType(this) ?: return null
        return when {
            mimeType.startsWith("image/") -> "Foto"
            mimeType.startsWith("video/") -> "Video"
            mimeType == "application/pdf" -> "PDF"
            else -> null
        }
    }
}
