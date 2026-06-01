package leo.rios.officium.home.presentation.viewModel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import leo.rios.officium.userProfile.data.PublicacionDto
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.firstOrNull
import leo.rios.officium.core.dataStore.DataStoreManager
import leo.rios.officium.userProfile.domain.UserProfileRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: UserProfileRepository,
    private val dataStoreManager: DataStoreManager,
    private val application: Application
) : ViewModel() {

    private val _publications = MutableStateFlow<List<PublicacionDto>>(emptyList())
    val publications: StateFlow<List<PublicacionDto>> = _publications

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentUserId = MutableStateFlow<Int?>(null)
    val currentUserId: StateFlow<Int?> = _currentUserId

    private val _currentUserRole = MutableStateFlow<String?>(null)
    val currentUserRole: StateFlow<String?> = _currentUserRole

    private var page = 1
    private var hasMore = true

    init {
        loadCurrentUserId()
        refresh()
    }

    private fun loadCurrentUserId() = viewModelScope.launch {
        _currentUserId.value = dataStoreManager.getProfileJson().firstOrNull().extractCurrentUserId()
        _currentUserRole.value = dataStoreManager.getRole().firstOrNull()
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

    fun refresh() {
        page = 1
        hasMore = true
        _publications.value = emptyList()
        loadNextPage()
    }

    fun loadNextPage() = viewModelScope.launch {
        if (_isLoading.value || !hasMore) return@launch

        _isLoading.value = true
        repository.getPublications(page)
            .onSuccess { (items, canLoadMore) ->
                _publications.value = _publications.value + items
                hasMore = canLoadMore
                page++
            }
            .onFailure { _message.value = it.localizedMessage ?: "Error al cargar publicaciones" }
        _isLoading.value = false
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
        val publicationId = _publications.value
            .firstOrNull { publication -> publication.comentarios.any { it.idComentario == commentId } }
            ?.idPublicacion

        repository.updateComment(commentId, content)
            .onSuccess { publicationId?.let { refreshPublication(it) } }
            .onFailure { _message.value = it.localizedMessage ?: "Error al actualizar comentario" }
    }

    fun deleteComment(commentId: Int) = viewModelScope.launch {
        val publicationId = _publications.value
            .firstOrNull { publication -> publication.comentarios.any { it.idComentario == commentId } }
            ?.idPublicacion

        repository.deleteComment(commentId)
            .onSuccess { publicationId?.let { refreshPublication(it) } }
            .onFailure { _message.value = it.localizedMessage ?: "Error al eliminar comentario" }
    }

    fun updatePublication(publicationId: Int, content: String, fileUri: Uri?) = viewModelScope.launch {
        val fileType = fileUri?.inferPublicationFileType()
        val (filePart, thumbnailPart) = withContext(Dispatchers.IO) {
            val file = fileUri?.let { uriToFilePart(it) }
            val thumbnail = fileUri
                ?.takeIf { fileType == "PDF" }
                ?.let { uriToPdfThumbnailPart(it) }
            file to thumbnail
        }
        repository.updatePublication(
            id = publicationId,
            content = content,
            fileType = filePart?.let { fileType },
            file = filePart,
            thumbnail = thumbnailPart
        ).onSuccess { refreshPublication(publicationId) }
            .onFailure { _message.value = it.localizedMessage ?: "Error al actualizar publicacion" }
    }

    fun deletePublication(publicationId: Int) = viewModelScope.launch {
        repository.deletePublication(publicationId)
            .onSuccess {
                _publications.value = _publications.value.filterNot { it.idPublicacion == publicationId }
            }
            .onFailure { _message.value = it.localizedMessage ?: "Error al eliminar publicacion" }
    }

    fun reportPublication(publicationId: Int, reason: String, description: String) = viewModelScope.launch {
        repository.reportPublication(publicationId, reason, description)
            .onSuccess { _message.value = "Reporte enviado" }
            .onFailure { _message.value = it.localizedMessage ?: "Error al reportar" }
    }

    private fun uriToFilePart(uri: Uri): MultipartBody.Part? {
        val resolver = application.contentResolver
        val mimeType = resolver.getType(uri) ?: "application/octet-stream"
        val extension = mimeType.substringAfter('/', "file").substringBefore('+')
        val file = File(application.cacheDir, "home_publication_${System.currentTimeMillis()}.$extension")

        return try {
            resolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }
            MultipartBody.Part.createFormData("Archivo", file.name, file.asRequestBody(mimeType.toMediaTypeOrNull()))
        } catch (e: Exception) {
            Log.e("Home", "Error preparando archivo", e)
            null
        }
    }

    private fun uriToPdfThumbnailPart(uri: Uri): MultipartBody.Part? {
        val resolver = application.contentResolver
        val file = File(application.cacheDir, "home_pdf_thumbnail_${System.currentTimeMillis()}.png")

        return try {
            resolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    if (renderer.pageCount == 0) return null
                    renderer.openPage(0).use { page ->
                        val targetWidth = 640
                        val ratio = page.height.toFloat() / page.width.toFloat()
                        val targetHeight = (targetWidth * ratio).toInt().coerceAtLeast(1)
                        val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(android.graphics.Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        FileOutputStream(file).use { output ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 90, output)
                        }
                        bitmap.recycle()
                    }
                }
            } ?: return null

            MultipartBody.Part.createFormData(
                "Thumbnail",
                file.name,
                file.asRequestBody("image/png".toMediaTypeOrNull())
            )
        } catch (e: Exception) {
            Log.e("Home", "Error generando miniatura PDF", e)
            null
        }
    }

    private fun refreshPublication(publicationId: Int) = viewModelScope.launch {
        repository.getPublication(publicationId)
            .onSuccess { updated ->
                _publications.value = _publications.value.map { publication ->
                    if (publication.idPublicacion == updated.idPublicacion) updated else publication
                }
            }
            .onFailure { _message.value = it.localizedMessage ?: "Error al actualizar publicacion" }
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
