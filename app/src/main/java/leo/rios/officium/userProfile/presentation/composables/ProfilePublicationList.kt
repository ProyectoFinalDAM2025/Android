package leo.rios.officium.userProfile.presentation.composables

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import leo.rios.officium.R
import leo.rios.officium.core.api.toStorageUrl
import leo.rios.officium.core.presentation.components.OfficiumVideoPlayer
import leo.rios.officium.core.presentation.share.ShareOptionsDialog
import leo.rios.officium.core.presentation.share.buildPublicationShareLink
import leo.rios.officium.userProfile.data.ComentarioDto
import leo.rios.officium.userProfile.data.PublicacionDto
import leo.rios.officium.userProfile.data.displayName
import leo.rios.officium.userProfile.data.photo
import java.io.File
import java.net.URL

@Composable
fun ProfilePublicationList(
    publications: List<PublicacionDto>,
    currentUserId: Int?,
    canManageContent: Boolean = false,
    modifier: Modifier = Modifier,
    scrollToTopRequest: Int = 0,
    onLoadMore: () -> Unit = {},
    onLikeClick: (PublicacionDto) -> Unit = {},
    onCommentSubmit: (PublicacionDto, String) -> Unit = { _, _ -> },
    onPublicationEdit: (PublicacionDto, String, Uri?) -> Unit = { _, _, _ -> },
    onPublicationDelete: (PublicacionDto) -> Unit = {},
    onCommentEdit: (ComentarioDto, String) -> Unit = { _, _ -> },
    onCommentDelete: (ComentarioDto) -> Unit = {},
    onReport: (PublicacionDto, String, String) -> Unit = { _, _, _ -> },
    onShare: (PublicacionDto) -> Unit = {},
    onAuthorClick: (Int) -> Unit = {}
) {
    if (publications.isEmpty()) {
        EmptyPublications(modifier = modifier)
        return
    }

    val listState = rememberLazyListState()
    val currentVisibleVideoId = remember(publications, listState) {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo
                .mapNotNull { item ->
                    publications.getOrNull(item.index)
                        ?.takeIf { it.tipoArchivo == "Video" && !it.archivo.isNullOrBlank() }
                }
                .maxByOrNull { publication ->
                    listState.layoutInfo.visibleItemsInfo
                        .firstOrNull { publications.getOrNull(it.index)?.idPublicacion == publication.idPublicacion }
                        ?.let { item ->
                            minOf(item.offset + item.size, listState.layoutInfo.viewportEndOffset) -
                                maxOf(item.offset, listState.layoutInfo.viewportStartOffset)
                        } ?: 0
                }
                ?.idPublicacion
        }
    }

    val shouldLoadMore = remember(listState, publications) {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= publications.lastIndex - 2
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) onLoadMore()
    }

    LaunchedEffect(scrollToTopRequest) {
        if (scrollToTopRequest > 0 && publications.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(publications, key = { it.idPublicacion }) { publication ->
            PublicationItem(
                publication = publication,
                currentUserId = currentUserId,
                canManageContent = canManageContent,
                isVideoPlaying = publication.idPublicacion == currentVisibleVideoId.value,
                onLikeClick = { onLikeClick(publication) },
                onCommentSubmit = { onCommentSubmit(publication, it) },
                onPublicationEdit = { content, uri -> onPublicationEdit(publication, content, uri) },
                onPublicationDelete = { onPublicationDelete(publication) },
                onCommentEdit = onCommentEdit,
                onCommentDelete = onCommentDelete,
                onReport = { reason, description -> onReport(publication, reason, description) },
                onShare = { onShare(publication) },
                onAuthorClick = onAuthorClick
            )
        }
    }
}

@Composable
private fun PublicationItem(
    publication: PublicacionDto,
    currentUserId: Int?,
    canManageContent: Boolean,
    isVideoPlaying: Boolean,
    onLikeClick: () -> Unit,
    onCommentSubmit: (String) -> Unit,
    onPublicationEdit: (String, Uri?) -> Unit,
    onPublicationDelete: () -> Unit,
    onCommentEdit: (ComentarioDto, String) -> Unit,
    onCommentDelete: (ComentarioDto) -> Unit,
    onReport: (String, String) -> Unit,
    onShare: () -> Unit,
    onAuthorClick: (Int) -> Unit
) {
    val isOwner = publication.idUsuario != null && publication.idUsuario == currentUserId
    val canEditContent = isOwner || canManageContent
    var showMenu by remember { mutableStateOf(false) }
    var showComments by remember { mutableStateOf(false) }
    var showEditPublication by remember { mutableStateOf(false) }
    var showDeletePublication by remember { mutableStateOf(false) }
    var showReport by remember { mutableStateOf(false) }
    var showShare by remember { mutableStateOf(false) }
    var selectedPhotoPublication by remember { mutableStateOf<PublicacionDto?>(null) }
    var selectedPdfUrl by remember { mutableStateOf<String?>(null) }
    var commentText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = publication.user.photo().toStorageUrl(),
                contentDescription = "Foto autor",
                placeholder = painterResource(id = R.drawable.acount2),
                error = painterResource(id = R.drawable.acount2),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                    .clickable {
                        publication.idUsuario?.let(onAuthorClick)
                    }
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        publication.idUsuario?.let(onAuthorClick)
                    }
            ) {
                Text(publication.user.displayName(), fontWeight = FontWeight.Bold, color = Color.Black)
                publication.fechaPublicacion?.let {
                    Text(it, color = Color(0xFF6F7782), fontSize = 12.sp)
                }
            }
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Opciones")
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                if (canEditContent) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            showEditPublication = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            showDeletePublication = true
                        }
                    )
                }
                if (!isOwner || canManageContent) {
                    DropdownMenuItem(
                        text = { Text("Reportar") },
                        leadingIcon = { Icon(Icons.Filled.Flag, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            showReport = true
                        }
                    )
                }
            }
        }

        Text(
            text = publication.contenido,
            color = Color(0xFF25313B),
            fontSize = 15.sp
        )

        publication.archivo?.takeIf { it.isNotBlank() }?.let { archivo ->
            PublicationAttachment(
                url = archivo,
                thumbnail = publication.thumbnail,
                type = publication.tipoArchivo,
                isVideoPlaying = isVideoPlaying,
                onPhotoClick = { selectedPhotoPublication = publication },
                onPdfClick = { selectedPdfUrl = archivo }
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onLikeClick) {
                Icon(
                    imageVector = if (publication.likedByCurrentUser) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (publication.likedByCurrentUser) Color(0xFFE91E63) else Color.Black
                )
            }
            Text(publication.likesCount.toString(), fontWeight = FontWeight.SemiBold)

            IconButton(onClick = { showComments = !showComments }) {
                Icon(Icons.Filled.ChatBubbleOutline, contentDescription = "Comentarios")
            }
            Text((publication.comentariosCount.takeIf { it > 0 } ?: publication.comentarios.size).toString())

            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    showShare = true
                    onShare()
                }
            ) {
                Icon(Icons.Filled.Share, contentDescription = "Compartir")
            }
        }

        if (showComments) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                publication.comentarios.forEach { comment ->
                    CommentItem(
                        comment = comment,
                        currentUserId = currentUserId,
                        canManageContent = canManageContent,
                        onAuthorClick = onAuthorClick,
                        onCommentEdit = onCommentEdit,
                        onCommentDelete = onCommentDelete
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        label = { Text("Comentar") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            onCommentSubmit(commentText)
                            commentText = ""
                        }
                    ) {
                        Text("Enviar")
                    }
                }
            }
        }
    }

    if (showEditPublication) {
        EditPublicationDialog(
            initialContent = publication.contenido,
            onDismiss = { showEditPublication = false },
            onSave = { content, uri ->
                showEditPublication = false
                onPublicationEdit(content, uri)
            }
        )
    }

    if (showDeletePublication) {
        ConfirmDialog(
            title = "Eliminar publicacion",
            text = "Esta accion eliminara la publicacion.",
            confirmText = "Eliminar",
            onDismiss = { showDeletePublication = false },
            onConfirm = {
                showDeletePublication = false
                onPublicationDelete()
            }
        )
    }

    if (showReport) {
        ReportDialog(
            onDismiss = { showReport = false },
            onReport = { reason, description ->
                showReport = false
                onReport(reason, description)
            }
        )
    }

    if (showShare) {
        ShareOptionsDialog(
            title = publication.contenido.take(80).ifBlank { "Publicacion Officium" },
            link = buildPublicationShareLink(publication.idPublicacion),
            onDismiss = { showShare = false }
        )
    }

    selectedPhotoPublication?.let { photoPublication ->
        PublicationPhotoDialog(
            photoUrl = photoPublication.archivo.orEmpty(),
            thumbnailUrl = photoPublication.thumbnail,
            previewUrl = photoPublication.preview,
            publicationId = photoPublication.idPublicacion,
            description = photoPublication.contenido,
            onDismiss = { selectedPhotoPublication = null }
        )
    }

    selectedPdfUrl?.let { pdfUrl ->
        PublicationPdfDialog(
            pdfUrl = pdfUrl,
            description = publication.contenido,
            onDismiss = { selectedPdfUrl = null }
        )
    }
}

@Composable
private fun CommentItem(
    comment: ComentarioDto,
    currentUserId: Int?,
    canManageContent: Boolean,
    onAuthorClick: (Int) -> Unit,
    onCommentEdit: (ComentarioDto, String) -> Unit,
    onCommentDelete: (ComentarioDto) -> Unit
) {
    val isOwner = comment.idUsuario != null && comment.idUsuario == currentUserId
    val canEditContent = isOwner || canManageContent
    var showMenu by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.Top) {
        AsyncImage(
            model = comment.user.photo().toStorageUrl(),
            contentDescription = "Foto autor comentario",
            placeholder = painterResource(id = R.drawable.acount2),
            error = painterResource(id = R.drawable.acount2),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                .clickable {
                    comment.idUsuario?.let(onAuthorClick)
                }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = comment.user.displayName(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.clickable {
                    comment.idUsuario?.let(onAuthorClick)
                }
            )
            Text(comment.contenido, color = Color(0xFF25313B), fontSize = 13.sp)
        }
        if (canEditContent) {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Opciones comentario")
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Editar") },
                    onClick = {
                        showMenu = false
                        showEdit = true
                    }
                )
                DropdownMenuItem(
                    text = { Text("Eliminar") },
                    onClick = {
                        showMenu = false
                        showDelete = true
                    }
                )
            }
        }
    }

    if (showEdit) {
        EditTextDialog(
            title = "Editar comentario",
            initialText = comment.contenido,
            onDismiss = { showEdit = false },
            onSave = {
                showEdit = false
                onCommentEdit(comment, it)
            }
        )
    }

    if (showDelete) {
        ConfirmDialog(
            title = "Eliminar comentario",
            text = "Esta accion eliminara el comentario.",
            confirmText = "Eliminar",
            onDismiss = { showDelete = false },
            onConfirm = {
                showDelete = false
                onCommentDelete(comment)
            }
        )
    }
}

@Composable
private fun PublicationAttachment(
    url: String,
    thumbnail: String?,
    type: String?,
    isVideoPlaying: Boolean,
    onPhotoClick: () -> Unit,
    onPdfClick: () -> Unit
) {
    val context = LocalContext.current
    val imageUrl = if (type == "Foto") {
        thumbnail ?: url
    } else {
        url
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp, max = 300.dp)
            .background(Color(0xFFF0F2F5), RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        when (type) {
            "Foto" -> AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl.toStorageUrl())
                    .size(900, 900)
                    .build(),
                contentDescription = "Archivo de publicacion",
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.acount2),
                error = painterResource(id = R.drawable.acount2),
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onPhotoClick)
            )
            "Video" -> OfficiumVideoPlayer(
                videoUrl = url,
                isPlaying = isVideoPlaying,
                muted = false,
                showControls = true,
                modifier = Modifier.fillMaxSize()
            )
            "PDF" -> {
                thumbnail?.takeIf { it.isNotBlank() }?.let { thumbnailUrl ->
                    AsyncImage(
                        model = thumbnailUrl.toStorageUrl(),
                        contentDescription = "Miniatura PDF",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(onClick = onPdfClick)
                    )
                }
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = "PDF",
                    tint = if (thumbnail.isNullOrBlank()) Color(0xFFD32F2F) else Color.White,
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(onClick = onPdfClick)
                )
            }
            else -> Text("Archivo adjunto", color = Color(0xFF6F7782), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PublicationPhotoDialog(
    photoUrl: String,
    thumbnailUrl: String?,
    previewUrl: String?,
    publicationId: Int,
    description: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var imageLoadStep by remember(publicationId) { mutableStateOf(0) }
    val originalPhotoUrl = photoUrl.toStorageUrl()
    val previewPhotoUrl = previewUrl.toStorageUrl()
    val fallbackPhotoUrl = thumbnailUrl.toStorageUrl()
    val resolvedPhotoUrl = when (imageLoadStep) {
        0 -> originalPhotoUrl
        1 -> previewPhotoUrl ?: fallbackPhotoUrl ?: originalPhotoUrl
        else -> fallbackPhotoUrl ?: previewPhotoUrl ?: originalPhotoUrl
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black, RoundedCornerShape(12.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cerrar imagen",
                        tint = Color.White
                    )
                }
            }

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(resolvedPhotoUrl)
                    .size(1400, 1400)
                    .listener(
                        onStart = {
                            Log.d("PublicationPhotoPreview", "Loading photo $publicationId: $resolvedPhotoUrl")
                        },
                        onError = { _, result ->
                            Log.e(
                                "PublicationPhotoPreview",
                                "Error loading photo $publicationId: $resolvedPhotoUrl",
                                result.throwable
                            )
                            if (imageLoadStep == 0 && (previewPhotoUrl != null || fallbackPhotoUrl != null)) {
                                imageLoadStep = 1
                            } else if (imageLoadStep == 1 && fallbackPhotoUrl != null && resolvedPhotoUrl != fallbackPhotoUrl) {
                                imageLoadStep = 2
                            }
                        },
                        onSuccess = { _, _ ->
                            Log.d("PublicationPhotoPreview", "Loaded photo $publicationId: $resolvedPhotoUrl")
                        }
                    )
                    .build(),
                contentDescription = "Imagen de publicacion",
                contentScale = ContentScale.Fit,
                placeholder = painterResource(id = R.drawable.acount2),
                error = painterResource(id = R.drawable.acount2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp)
            )
            Text(
                text = description,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PublicationPdfDialog(
    pdfUrl: String,
    description: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var pageIndex by remember(pdfUrl) { mutableStateOf(0) }
    var preview by remember(pdfUrl, pageIndex) { mutableStateOf<PublicationPdfPagePreview?>(null) }
    var errorMessage by remember(pdfUrl) { mutableStateOf<String?>(null) }

    LaunchedEffect(pdfUrl, pageIndex) {
        preview = null
        errorMessage = null
        runCatching {
            withContext(Dispatchers.IO) {
                renderPublicationPdfPage(
                    cacheDir = context.cacheDir,
                    pdfUrl = pdfUrl,
                    pageIndex = pageIndex
                )
            }
        }.onSuccess {
            preview = it
        }.onFailure {
            errorMessage = it.localizedMessage ?: "No se pudo abrir el PDF"
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = preview?.let { "Pagina ${it.pageIndex + 1} de ${it.pageCount}" } ?: "PDF",
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar PDF")
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp)
                    .background(Color(0xFFF0F2F5)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    preview != null -> Image(
                        bitmap = preview!!.bitmap.asImageBitmap(),
                        contentDescription = "Pagina PDF",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    errorMessage != null -> Text(
                        text = errorMessage.orEmpty(),
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.SemiBold
                    )
                    else -> Text(
                        text = "Cargando PDF...",
                        color = Color(0xFF6F7782),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { pageIndex-- },
                    enabled = preview != null && pageIndex > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Anterior")
                }
                Button(
                    onClick = { pageIndex++ },
                    enabled = preview != null && pageIndex < (preview?.pageCount ?: 1) - 1,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Siguiente")
                }
            }

            Text(
                text = description,
                color = Color(0xFF25313B),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private data class PublicationPdfPagePreview(
    val bitmap: Bitmap,
    val pageIndex: Int,
    val pageCount: Int
)

private fun renderPublicationPdfPage(
    cacheDir: File,
    pdfUrl: String,
    pageIndex: Int
): PublicationPdfPagePreview {
    val pdfFile = downloadPublicationPdfToCache(cacheDir, pdfUrl)
    ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
        PdfRenderer(descriptor).use { renderer ->
            val safePageIndex = pageIndex.coerceIn(0, renderer.pageCount - 1)
            renderer.openPage(safePageIndex).use { page ->
                val scale = 2
                val bitmap = Bitmap.createBitmap(
                    page.width * scale,
                    page.height * scale,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                return PublicationPdfPagePreview(
                    bitmap = bitmap,
                    pageIndex = safePageIndex,
                    pageCount = renderer.pageCount
                )
            }
        }
    }
}

private fun downloadPublicationPdfToCache(
    cacheDir: File,
    pdfUrl: String
): File {
    val resolvedUrl = pdfUrl.toStorageUrl().orEmpty()
    val file = File(cacheDir, "publication_pdf_${resolvedUrl.hashCode()}.pdf")
    if (file.exists() && file.length() > 0) return file

    URL(resolvedUrl).openStream().use { input ->
        file.outputStream().use { output -> input.copyTo(output) }
    }

    return file
}

@Composable
private fun EditPublicationDialog(
    initialContent: String,
    onDismiss: () -> Unit,
    onSave: (String, Uri?) -> Unit
) {
    var content by remember { mutableStateOf(initialContent) }
    var selectedFile by remember { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedFile = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar publicacion") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Contenido") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                TextButton(onClick = { picker.launch("*/*") }) {
                    Text(if (selectedFile == null) "Reemplazar archivo" else "Archivo seleccionado")
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(content, selectedFile) }) { Text("Guardar") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun EditTextDialog(
    title: String,
    initialText: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialText) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        },
        confirmButton = { Button(onClick = { onSave(text) }) { Text("Guardar") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun ReportDialog(
    onDismiss: () -> Unit,
    onReport: (String, String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reportar publicacion") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripcion") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = { Button(onClick = { onReport(reason, description) }) { Text("Reportar") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun ConfirmDialog(
    title: String,
    text: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text(confirmText)
            }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun EmptyPublications(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 42.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Aun no hay publicaciones", color = Color(0xFF6F7782), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
