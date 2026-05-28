package leo.rios.officium.publicationDetail.presentation.viewModel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import leo.rios.officium.core.dataStore.DataStoreManager
import leo.rios.officium.userProfile.data.PublicacionDto
import leo.rios.officium.userProfile.domain.UserProfileRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class PublicationDetailViewModel @Inject constructor(
    private val repository: UserProfileRepository,
    private val dataStoreManager: DataStoreManager,
    private val application: Application
) : ViewModel() {
    private val _publication = MutableStateFlow<PublicacionDto?>(null)
    val publication: StateFlow<PublicacionDto?> = _publication

    private val _currentUserId = MutableStateFlow<Int?>(null)
    val currentUserId: StateFlow<Int?> = _currentUserId

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun load(publicationId: Int) = viewModelScope.launch {
        _currentUserId.value = dataStoreManager.getProfileJson().firstOrNull().extractCurrentUserId()
        refreshPublication(publicationId)
    }

    fun likePublication(publicationId: Int, liked: Boolean) = viewModelScope.launch {
        val result = if (liked) repository.unlikePublication(publicationId) else repository.likePublication(publicationId)
        result.onSuccess { refreshPublication(publicationId) }
            .onFailure { _message.value = it.localizedMessage ?: "Error al actualizar like" }
    }

    fun addComment(publicationId: Int, content: String) = viewModelScope.launch {
        repository.addComment(publicationId, content)
            .onSuccess { refreshPublication(publicationId) }
            .onFailure { _message.value = it.localizedMessage ?: "Error al comentar" }
    }

    fun updateComment(commentId: Int, content: String) = viewModelScope.launch {
        val publicationId = _publication.value?.idPublicacion ?: return@launch
        repository.updateComment(commentId, content)
            .onSuccess { refreshPublication(publicationId) }
            .onFailure { _message.value = it.localizedMessage ?: "Error al actualizar comentario" }
    }

    fun deleteComment(commentId: Int) = viewModelScope.launch {
        val publicationId = _publication.value?.idPublicacion ?: return@launch
        repository.deleteComment(commentId)
            .onSuccess { refreshPublication(publicationId) }
            .onFailure { _message.value = it.localizedMessage ?: "Error al eliminar comentario" }
    }

    fun updatePublication(publicationId: Int, content: String, fileUri: Uri?) = viewModelScope.launch {
        val filePart = withContext(Dispatchers.IO) { fileUri?.let { uriToFilePart(it) } }
        repository.updatePublication(
            id = publicationId,
            content = content,
            fileType = filePart?.let { fileUri?.inferPublicationFileType() },
            file = filePart
        ).onSuccess { refreshPublication(publicationId) }
            .onFailure { _message.value = it.localizedMessage ?: "Error al actualizar publicacion" }
    }

    fun deletePublication(publicationId: Int) = viewModelScope.launch {
        repository.deletePublication(publicationId)
            .onSuccess {
                _publication.value = null
                _message.value = "Publicacion eliminada"
            }
            .onFailure { _message.value = it.localizedMessage ?: "Error al eliminar publicacion" }
    }

    fun reportPublication(publicationId: Int, reason: String, description: String) = viewModelScope.launch {
        repository.reportPublication(publicationId, reason, description)
            .onSuccess { _message.value = "Reporte enviado" }
            .onFailure { _message.value = it.localizedMessage ?: "Error al reportar" }
    }

    private suspend fun refreshPublication(publicationId: Int) {
        repository.getPublication(publicationId)
            .onSuccess { _publication.value = it }
            .onFailure { _message.value = it.localizedMessage ?: "No se pudo cargar la publicacion" }
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

    private fun uriToFilePart(uri: Uri): MultipartBody.Part? {
        val resolver = application.contentResolver
        val mimeType = resolver.getType(uri) ?: "application/octet-stream"
        val extension = mimeType.substringAfter('/', "file").substringBefore('+')
        val file = File(application.cacheDir, "detail_publication_${System.currentTimeMillis()}.$extension")

        return try {
            resolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }
            MultipartBody.Part.createFormData("Archivo", file.name, file.asRequestBody(mimeType.toMediaTypeOrNull()))
        } catch (e: Exception) {
            Log.e("PublicationDetail", "Error preparando archivo", e)
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
