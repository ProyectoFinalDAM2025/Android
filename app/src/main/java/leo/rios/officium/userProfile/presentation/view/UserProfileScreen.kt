package leo.rios.officium.userProfile.presentation.view

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import leo.rios.officium.R
import leo.rios.officium.core.api.toStorageUrl
import leo.rios.officium.core.presentation.components.OfficiumBottomNavigation
import leo.rios.officium.core.presentation.components.OfficiumVideoPlayer
import leo.rios.officium.empresaProfile.data.ProvinciaData
import leo.rios.officium.empresaProfile.data.SectorData
import leo.rios.officium.userProfile.data.DocumentoDto
import leo.rios.officium.userProfile.presentation.composables.ProfileDocumentGrid
import leo.rios.officium.userProfile.presentation.composables.ProfilePublicationList
import leo.rios.officium.userProfile.presentation.model.ProfileTab
import leo.rios.officium.userProfile.presentation.model.ProfileUploadType
import leo.rios.officium.userProfile.presentation.viewModel.UserProfileViewModel
import com.google.gson.JsonParser
import java.io.File
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel,
    profileUserId: Int? = null,
    onHomeClick: () -> Unit,
    onSecondClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onMyProfileClick: () -> Unit,
    onUserProfileClick: (Int) -> Unit = {},
    onLogout: () -> Unit
) {
    val profileName by viewModel.profileName.collectAsState()
    val profileRole by viewModel.profileRole.collectAsState()
    val profilePhoto by viewModel.profilePhoto.collectAsState()
    val currentUserPhoto by viewModel.currentUserPhoto.collectAsState()
    val profileDescription by viewModel.profileDescription.collectAsState()
    val profileJson by viewModel.profileJson.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val currentUserRole by viewModel.currentUserRole.collectAsState()
    val viewedUserId by viewModel.viewedUserId.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val publications by viewModel.publications.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val videos by viewModel.videos.collectAsState()
    val pdfs by viewModel.pdfs.collectAsState()
    val message by viewModel.message.collectAsState()
    val sectors by viewModel.sectors.collectAsState()
    val provincias by viewModel.provincias.collectAsState()
    val context = LocalContext.current
    val isProfileOwner = currentUserId != null && viewedUserId == currentUserId
    val canManageProfile = isProfileOwner || currentUserRole == "Administrador"
    var selectedTab by remember { mutableStateOf(ProfileTab.Posts) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var selectedPhotoDocument by remember { mutableStateOf<DocumentoDto?>(null) }
    var selectedVideoDocument by remember { mutableStateOf<DocumentoDto?>(null) }
    var selectedPdfDocument by remember { mutableStateOf<DocumentoDto?>(null) }
    var selectedDocumentActions by remember { mutableStateOf<DocumentoDto?>(null) }
    var editingDocument by remember { mutableStateOf<DocumentoDto?>(null) }
    var deletingDocument by remember { mutableStateOf<DocumentoDto?>(null) }
    var showReportProfileDialog by remember { mutableStateOf(false) }
    val profilePhotoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        viewModel.updateProfilePhoto(uri)
    }

    val selectedDocuments = when (selectedTab) {
        ProfileTab.Posts -> emptyList()
        ProfileTab.Photos -> photos
        ProfileTab.Videos -> videos
        ProfileTab.Pdfs -> pdfs
    }

    LaunchedEffect(message) {
        message?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    LaunchedEffect(profileUserId) {
        viewModel.openProfile(profileUserId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = profileName ?: "Perfil",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (isProfileOwner) {
                        IconButton(onClick = { showUploadDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Subir contenido"
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Abrir opciones"
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = "Cerrar sesion") },
                            onClick = {
                                menuExpanded = false
                                onLogout()
                            }
                        )
                        if (!isProfileOwner && viewedUserId != null) {
                            DropdownMenuItem(
                                text = { Text(text = "Reportar perfil") },
                                leadingIcon = { Icon(Icons.Filled.Flag, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    showReportProfileDialog = true
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            OfficiumBottomNavigation(
                profileImageUrl = currentUserPhoto,
                profileRole = currentUserRole,
                hasNotifications = true,
                onHomeClick = onHomeClick,
                onSecondClick = onSecondClick,
                onNotificationsClick = onNotificationsClick,
                onSearchClick = onSearchClick,
                onProfileClick = onMyProfileClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            ProfileHeader(
                profileName = profileName ?: "Usuario Officium",
                profileRole = profileRole ?: "Usuario",
                profilePhoto = profilePhoto,
                postCount = publications.size,
                photoCount = photos.size,
                videoCount = videos.size,
                fileCount = pdfs.size,
                description = profileDescription ?: "Perfil profesional en Officium",
                canEditPhoto = canManageProfile,
                onPhotoClick = {
                    if (canManageProfile) {
                        profilePhotoPicker.launch("image/*")
                    }
                }
            )

            ProfileActions(
                isProfileOwner = canManageProfile,
                onEditClick = { showEditDialog = true },
                onShareClick = { }
            )

            ProfileTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            if (selectedTab == ProfileTab.Posts) {
                ProfilePublicationList(
                    publications = publications,
                    currentUserId = currentUserId,
                    canManageContent = currentUserRole == "Administrador",
                    onLikeClick = { viewModel.likePublication(it.idPublicacion, it.likedByCurrentUser) },
                    onCommentSubmit = { publication, content -> viewModel.addComment(publication.idPublicacion, content) },
                    onPublicationEdit = { publication, content, fileUri ->
                        viewModel.updatePublication(publication.idPublicacion, content, fileUri)
                    },
                    onPublicationDelete = { viewModel.deletePublication(it.idPublicacion) },
                    onCommentEdit = { comment, content -> viewModel.updateComment(comment.idComentario, content) },
                    onCommentDelete = { viewModel.deleteComment(it.idComentario) },
                    onReport = { publication, reason, description ->
                        viewModel.reportPublication(publication.idPublicacion, reason, description)
                    },
                    onAuthorClick = onUserProfileClick,
                    modifier = Modifier.height(520.dp)
                )
            } else {
                ProfileDocumentGrid(
                    selectedTab = selectedTab,
                    documents = selectedDocuments,
                    modifier = Modifier.height(520.dp),
                    onDocumentClick = { document ->
                        if (canManageProfile) {
                            selectedDocumentActions = document
                        } else {
                            openDocumentPreview(
                                document = document,
                                selectedTab = selectedTab,
                                onPhotoSelected = { selectedPhotoDocument = it },
                                onVideoSelected = { selectedVideoDocument = it },
                                onPdfSelected = { selectedPdfDocument = it }
                            )
                        }
                    }
                )
            }
        }
    }

    selectedDocumentActions?.let { document ->
        DocumentActionsDialog(
            document = document,
            selectedTab = selectedTab,
            onDismiss = { selectedDocumentActions = null },
            onPreview = {
                selectedDocumentActions = null
                openDocumentPreview(
                    document = document,
                    selectedTab = selectedTab,
                    onPhotoSelected = { selectedPhotoDocument = it },
                    onVideoSelected = { selectedVideoDocument = it },
                    onPdfSelected = { selectedPdfDocument = it }
                )
            },
            onEdit = {
                selectedDocumentActions = null
                editingDocument = document
            },
            onDelete = {
                selectedDocumentActions = null
                deletingDocument = document
            }
        )
    }

    editingDocument?.let { document ->
        EditDocumentDialog(
            document = document,
            isUpdating = isUpdating,
            onDismiss = { editingDocument = null },
            onSave = { description, fileUri ->
                viewModel.updateDocument(document.idDocumento, description, fileUri)
                editingDocument = null
            }
        )
    }

    deletingDocument?.let { document ->
        DeleteDocumentDialog(
            document = document,
            isDeleting = isUpdating,
            onDismiss = { deletingDocument = null },
            onDelete = {
                viewModel.deleteDocument(document.idDocumento)
                deletingDocument = null
            }
        )
    }

    selectedPhotoDocument?.let { document ->
        PhotoPreviewDialog(
            document = document,
            onDismiss = { selectedPhotoDocument = null }
        )
    }

    selectedVideoDocument?.let { document ->
        VideoPreviewDialog(
            document = document,
            onDismiss = { selectedVideoDocument = null }
        )
    }

    selectedPdfDocument?.let { document ->
        PdfPreviewDialog(
            document = document,
            onDismiss = { selectedPdfDocument = null }
        )
    }

    val reportProfileUserId = viewedUserId
    if (showReportProfileDialog && reportProfileUserId != null) {
        ReportProfileDialog(
            onDismiss = { showReportProfileDialog = false },
            onReport = { reason, description ->
                showReportProfileDialog = false
                viewModel.reportProfile(reportProfileUserId, reason, description)
            }
        )
    }

    if (showUploadDialog && isProfileOwner) {
        CreateProfileContentDialog(
            isUploading = isUpdating,
            onDismiss = { showUploadDialog = false },
            onCreate = { uploadType, content, description, fileUri ->
                viewModel.createProfileContent(uploadType, content, description, fileUri)
                showUploadDialog = false
            }
        )
    }

    if (showEditDialog && canManageProfile) {
        EditProfileDialog(
            role = profileRole,
            profileJson = profileJson,
            sectors = sectors,
            provincias = provincias,
            isUpdating = isUpdating,
            onDismiss = { showEditDialog = false },
            onUpdateDesempleado = { nombre, apellido, dni, porfolios, disponibilidad, ubicacion ->
                viewModel.updateDesempleado(nombre, apellido, dni, porfolios, disponibilidad, ubicacion)
                showEditDialog = false
            },
            onUpdateEmpresa = { nombreEmpresa, cif, idSector, ubicacion, sitioWeb ->
                viewModel.updateEmpresa(nombreEmpresa, cif, idSector, ubicacion, sitioWeb)
                showEditDialog = false
            },
            onUpdateAdministrador = { nombre, apellido ->
                viewModel.updateAdministrador(nombre, apellido)
                showEditDialog = false
            }
        )
    }
}

private fun openDocumentPreview(
    document: DocumentoDto,
    selectedTab: ProfileTab,
    onPhotoSelected: (DocumentoDto) -> Unit,
    onVideoSelected: (DocumentoDto) -> Unit,
    onPdfSelected: (DocumentoDto) -> Unit
) {
    when (selectedTab) {
        ProfileTab.Photos -> onPhotoSelected(document)
        ProfileTab.Videos -> onVideoSelected(document)
        ProfileTab.Pdfs -> onPdfSelected(document)
        else -> Unit
    }
}

@Composable
private fun DocumentActionsDialog(
    document: DocumentoDto,
    selectedTab: ProfileTab,
    onDismiss: () -> Unit,
    onPreview: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gestionar contenido") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = document.descripcion?.takeIf { it.isNotBlank() }
                        ?: document.nombreArchivo,
                    color = Color(0xFF25313B)
                )
                if (selectedTab == ProfileTab.Photos || selectedTab == ProfileTab.Videos || selectedTab == ProfileTab.Pdfs) {
                    Button(
                        onClick = onPreview,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Previsualizar")
                    }
                }
                Button(
                    onClick = onEdit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Editar")
                }
                Button(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Eliminar")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun EditDocumentDialog(
    document: DocumentoDto,
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, Uri?) -> Unit
) {
    var description by remember(document.idDocumento) {
        mutableStateOf(document.descripcion.orEmpty())
    }
    var selectedFileUri by remember(document.idDocumento) { mutableStateOf<Uri?>(null) }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedFileUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar contenido") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripcion") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                TextButton(
                    onClick = { filePicker.launch(document.tipo.toDocumentMimeType()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (selectedFileUri == null) {
                            "Reemplazar archivo"
                        } else {
                            "Archivo seleccionado"
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isUpdating,
                onClick = { onSave(description, selectedFileUri) }
            ) {
                Text(if (isUpdating) "Guardando..." else "Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun DeleteDocumentDialog(
    document: DocumentoDto,
    isDeleting: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar contenido") },
        text = {
            Text("Esta accion eliminara ${document.nombreArchivo}.")
        },
        confirmButton = {
            Button(
                enabled = !isDeleting,
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text(if (isDeleting) "Eliminando..." else "Eliminar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

private fun String.toDocumentMimeType(): String {
    return when (this) {
        "Foto" -> "image/*"
        "Video" -> "video/*"
        "PDF" -> "application/pdf"
        else -> "*/*"
    }
}

@Composable
private fun PhotoPreviewDialog(
    document: DocumentoDto,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var imageLoadStep by remember(document.idDocumento) { mutableStateOf(0) }
    val originalPhotoUrl = document.url.toStorageUrl()
    val previewPhotoUrl = document.preview.toStorageUrl()
    val thumbnailPhotoUrl = document.thumbnail.toStorageUrl()
    val photoUrl = when (imageLoadStep) {
        0 -> originalPhotoUrl
        1 -> previewPhotoUrl ?: thumbnailPhotoUrl ?: originalPhotoUrl
        else -> thumbnailPhotoUrl ?: previewPhotoUrl ?: originalPhotoUrl
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
                    .data(photoUrl)
                    .size(1400, 1400)
                    .listener(
                        onStart = {
                            Log.d("ProfilePhotoPreview", "Loading photo ${document.idDocumento}: $photoUrl")
                        },
                        onError = { _, result ->
                            Log.e(
                                "ProfilePhotoPreview",
                                "Error loading photo ${document.idDocumento}: $photoUrl",
                                result.throwable
                            )
                            if (imageLoadStep == 0 && (previewPhotoUrl != null || thumbnailPhotoUrl != null)) {
                                imageLoadStep = 1
                            } else if (imageLoadStep == 1 && thumbnailPhotoUrl != null && photoUrl != thumbnailPhotoUrl) {
                                imageLoadStep = 2
                            }
                        },
                        onSuccess = { _, _ ->
                            Log.d("ProfilePhotoPreview", "Loaded photo ${document.idDocumento}: $photoUrl")
                        }
                    )
                    .build(),
                contentDescription = "Imagen ampliada",
                contentScale = ContentScale.Fit,
                placeholder = painterResource(id = R.drawable.acount2),
                error = painterResource(id = R.drawable.acount2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp)
            )
            PreviewDescription(document = document, textColor = Color.White)
        }
    }
}

@Composable
private fun VideoPreviewDialog(
    document: DocumentoDto,
    onDismiss: () -> Unit
) {
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
                        contentDescription = "Cerrar video",
                        tint = Color.White
                    )
                }
            }

            OfficiumVideoPlayer(
                videoUrl = document.url,
                isPlaying = true,
                muted = false,
                showControls = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            )
            PreviewDescription(document = document, textColor = Color.White)
        }
    }
}

@Composable
private fun PdfPreviewDialog(
    document: DocumentoDto,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var pageIndex by remember(document.idDocumento) { mutableStateOf(0) }
    var preview by remember(document.idDocumento, pageIndex) { mutableStateOf<PdfPagePreview?>(null) }
    var errorMessage by remember(document.idDocumento) { mutableStateOf<String?>(null) }

    LaunchedEffect(document.idDocumento, pageIndex) {
        preview = null
        errorMessage = null
        runCatching {
            withContext(Dispatchers.IO) {
                renderPdfPage(
                    cacheDir = context.cacheDir,
                    pdfUrl = document.url,
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
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cerrar PDF"
                    )
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
            PreviewDescription(document = document, textColor = Color(0xFF25313B))
        }
    }
}

@Composable
private fun PreviewDescription(
    document: DocumentoDto,
    textColor: Color
) {
    val description = document.descripcion
        ?.takeIf { it.isNotBlank() }
        ?: document.nombreArchivo

    Text(
        text = description,
        color = textColor,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth()
    )
}

private data class PdfPagePreview(
    val bitmap: Bitmap,
    val pageIndex: Int,
    val pageCount: Int
)

private fun renderPdfPage(
    cacheDir: File,
    pdfUrl: String,
    pageIndex: Int
): PdfPagePreview {
    val pdfFile = downloadPdfToCache(cacheDir, pdfUrl)
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

                return PdfPagePreview(
                    bitmap = bitmap,
                    pageIndex = safePageIndex,
                    pageCount = renderer.pageCount
                )
            }
        }
    }
}

private fun downloadPdfToCache(
    cacheDir: File,
    pdfUrl: String
): File {
    val resolvedUrl = pdfUrl.toStorageUrl().orEmpty()
    val file = File(cacheDir, "pdf_preview_${resolvedUrl.hashCode()}.pdf")
    if (file.exists() && file.length() > 0) return file

    URL(resolvedUrl).openStream().use { input ->
        file.outputStream().use { output -> input.copyTo(output) }
    }

    return file
}

@Composable
private fun ReportProfileDialog(
    onDismiss: () -> Unit,
    onReport: (String, String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reportar perfil") },
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
        confirmButton = {
            Button(onClick = { onReport(reason, description) }) {
                Text("Reportar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun CreateProfileContentDialog(
    isUploading: Boolean,
    onDismiss: () -> Unit,
    onCreate: (ProfileUploadType, String, String, Uri?) -> Unit
) {
    var uploadType by remember { mutableStateOf(ProfileUploadType.Publication) }
    var content by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var showErrors by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedFileUri = uri
    }

    val requiresFile = uploadType != ProfileUploadType.Publication
    val canCreate = when (uploadType) {
        ProfileUploadType.Publication -> content.isNotBlank()
        else -> selectedFileUri != null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Subir contenido") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                UploadTypeDropdown(
                    selectedType = uploadType,
                    onTypeSelected = {
                        uploadType = it
                        selectedFileUri = null
                    }
                )

                if (uploadType == ProfileUploadType.Publication) {
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Publicacion") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        isError = showErrors && content.isBlank(),
                        supportingText = {
                            if (showErrors && content.isBlank()) {
                                Text("Escribe algo para publicar")
                            }
                        }
                    )
                } else {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripcion") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }

                TextButton(
                    onClick = { filePicker.launch(uploadType.mimeType) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (selectedFileUri == null) {
                            if (requiresFile) "Seleccionar archivo" else "Adjuntar archivo opcional"
                        } else {
                            "Archivo seleccionado"
                        }
                    )
                }

                if (showErrors && requiresFile && selectedFileUri == null) {
                    Text(
                        text = "Selecciona un archivo",
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isUploading,
                onClick = {
                    showErrors = true
                    if (canCreate) {
                        onCreate(uploadType, content, description, selectedFileUri)
                    }
                }
            ) {
                Text(if (isUploading) "Subiendo..." else "Subir")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UploadTypeDropdown(
    selectedType: ProfileUploadType,
    onTypeSelected: (ProfileUploadType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedType.title,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tipo") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ProfileUploadType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.title) },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    profileName: String,
    profileRole: String,
    profilePhoto: String?,
    postCount: Int,
    photoCount: Int,
    videoCount: Int,
    fileCount: Int,
    description: String,
    canEditPhoto: Boolean,
    onPhotoClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = profilePhoto.toStorageUrl(),
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.acount2),
                error = painterResource(id = R.drawable.acount2),
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                    .clickable(enabled = canEditPhoto, onClick = onPhotoClick)
            )

            Spacer(modifier = Modifier.width(22.dp))

            ProfileMetric(
                count = postCount.toString(),
                label = "posts",
                modifier = Modifier.weight(1f)
            )
            ProfileMetric(
                count = photoCount.toString(),
                label = "imagenes",
                modifier = Modifier.weight(1f)
            )
            ProfileMetric(
                count = videoCount.toString(),
                label = "videos",
                modifier = Modifier.weight(1f)
            )
            ProfileMetric(
                count = fileCount.toString(),
                label = "archivos",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = profileName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = profileRole,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF6F7782)
        )
        Text(
            text = description,
            fontSize = 14.sp,
            color = Color(0xFF25313B),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun ProfileMetric(
    count: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = count, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = label, fontSize = 13.sp, color = Color(0xFF25313B))
    }
}

@Composable
private fun ProfileActions(
    isProfileOwner: Boolean,
    onEditClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (isProfileOwner) {
            ProfileActionButton(
                text = "Editar perfil",
                onClick = onEditClick,
                modifier = Modifier.weight(1f)
            )
        }
        ProfileActionButton(
            text = "Compartir",
            onClick = onShareClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ProfileActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF0F2F5),
            contentColor = Color.Black
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
private fun EditProfileDialog(
    role: String?,
    profileJson: String?,
    sectors: List<SectorData>,
    provincias: List<ProvinciaData>,
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onUpdateDesempleado: (String, String, String, String, String, String) -> Unit,
    onUpdateEmpresa: (String, String, String, String, String) -> Unit,
    onUpdateAdministrador: (String, String) -> Unit
) {
    val profile = remember(profileJson) {
        runCatching { JsonParser.parseString(profileJson).asJsonObject }.getOrNull()
    }

    if (role == "Administrador") {
        var nombre by remember(profileJson) { mutableStateOf(profile.getStringOrEmpty("Nombre")) }
        var apellido by remember(profileJson) { mutableStateOf(profile.getStringOrEmpty("Apellido")) }
        var showErrors by remember { mutableStateOf(false) }
        val canSave = nombre.isNotBlank() && apellido.isNotBlank()

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Editar administrador") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileEditField(
                        label = "Nombre",
                        value = nombre,
                        isError = showErrors && nombre.isBlank(),
                        errorText = "El nombre no puede estar vacio"
                    ) { nombre = it }
                    ProfileEditField(
                        label = "Apellido",
                        value = apellido,
                        isError = showErrors && apellido.isBlank(),
                        errorText = "El apellido no puede estar vacio"
                    ) { apellido = it }
                }
            },
            confirmButton = {
                Button(
                    enabled = !isUpdating,
                    onClick = {
                        showErrors = true
                        if (canSave) {
                            onUpdateAdministrador(nombre, apellido)
                        }
                    }
                ) {
                    Text(if (isUpdating) "Guardando..." else "Guardar")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) { Text("Cancelar") }
            }
        )
    } else if (role == "Empresa") {
        var nombreEmpresa by remember(profileJson) { mutableStateOf(profile.getStringOrEmpty("NombreEmpresa")) }
        var cif by remember(profileJson) { mutableStateOf(profile.getStringOrEmpty("CIF")) }
        var idSector by remember(profileJson) { mutableStateOf(profile.getStringOrEmpty("IDSector")) }
        var ubicacion by remember(profileJson) { mutableStateOf(profile.getStringOrEmpty("Ubicacion")) }
        var sitioWeb by remember(profileJson) { mutableStateOf(profile.getStringOrEmpty("SitioWeb")) }
        var showErrors by remember { mutableStateOf(false) }
        val selectedSectorName = sectors.firstOrNull { it.idSector.toString() == idSector }?.nombre.orEmpty()
        val canSave = nombreEmpresa.isNotBlank() &&
            isValidSpanishCif(cif) &&
            idSector.isNotBlank() &&
            ubicacion.isNotBlank() &&
            isValidWebAddress(sitioWeb)

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Editar empresa") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileEditField(
                        label = "Nombre empresa",
                        value = nombreEmpresa,
                        isError = showErrors && nombreEmpresa.isBlank(),
                        errorText = "El nombre no puede estar vacio"
                    ) { nombreEmpresa = it }
                    ProfileEditField(
                        label = "CIF",
                        value = cif,
                        isError = showErrors && !isValidSpanishCif(cif),
                        errorText = "Introduce un CIF valido"
                    ) { cif = it }
                    SectorDropdown(
                        sectors = sectors,
                        selectedSectorName = selectedSectorName,
                        isError = showErrors && idSector.isBlank(),
                        onSectorSelected = { sector -> idSector = sector.idSector.toString() }
                    )
                    ProvinciaDropdown(
                        provincias = provincias,
                        selectedProvinciaName = ubicacion,
                        isError = showErrors && ubicacion.isBlank(),
                        onProvinciaSelected = { provincia -> ubicacion = provincia.name }
                    )
                    ProfileEditField(
                        label = "Sitio web",
                        value = sitioWeb,
                        isError = showErrors && !isValidWebAddress(sitioWeb),
                        errorText = "Introduce una URL valida"
                    ) { sitioWeb = it }
                }
            },
            confirmButton = {
                Button(
                    enabled = !isUpdating,
                    onClick = {
                        showErrors = true
                        if (canSave) {
                            onUpdateEmpresa(nombreEmpresa, cif, idSector, ubicacion, sitioWeb)
                        }
                    }
                ) {
                    Text(if (isUpdating) "Guardando..." else "Guardar")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) { Text("Cancelar") }
            }
        )
    } else {
        var nombre by remember(profileJson) { mutableStateOf(profile.getStringOrEmpty("Nombre")) }
        var apellido by remember(profileJson) { mutableStateOf(profile.getStringOrEmpty("Apellido")) }
        var dni by remember(profileJson) { mutableStateOf(profile.getStringOrEmpty("DNI")) }
        var porfolios by remember(profileJson) { mutableStateOf(profile.getStringOrEmpty("Porfolios")) }
        var disponibilidad by remember(profileJson) { mutableStateOf(profile.getStringOrEmpty("Disponibilidad")) }
        var ubicacion by remember(profileJson) { mutableStateOf(profile.getStringOrEmpty("Ubicacion")) }
        var showErrors by remember { mutableStateOf(false) }
        val canSave = nombre.isNotBlank() &&
            apellido.isNotBlank() &&
            isValidSpanishDniOrNie(dni) &&
            isValidWebAddress(porfolios) &&
            disponibilidad in disponibilidadOptions &&
            ubicacion.isNotBlank()

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Editar desempleado") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileEditField(
                        label = "Nombre",
                        value = nombre,
                        isError = showErrors && nombre.isBlank(),
                        errorText = "El nombre no puede estar vacio"
                    ) { nombre = it }
                    ProfileEditField(
                        label = "Apellido",
                        value = apellido,
                        isError = showErrors && apellido.isBlank(),
                        errorText = "El apellido no puede estar vacio"
                    ) { apellido = it }
                    ProfileEditField(
                        label = "DNI/NIE",
                        value = dni,
                        isError = showErrors && !isValidSpanishDniOrNie(dni),
                        errorText = "Introduce un DNI o NIE valido"
                    ) { dni = it }
                    ProfileEditField(
                        label = "Porfolios",
                        value = porfolios,
                        isError = showErrors && !isValidWebAddress(porfolios),
                        errorText = "Introduce una URL valida"
                    ) { porfolios = it }
                    DisponibilidadDropdown(
                        selectedDisponibilidad = disponibilidad,
                        isError = showErrors && disponibilidad !in disponibilidadOptions,
                        onDisponibilidadSelected = { disponibilidad = it }
                    )
                    ProvinciaDropdown(
                        provincias = provincias,
                        selectedProvinciaName = ubicacion,
                        isError = showErrors && ubicacion.isBlank(),
                        onProvinciaSelected = { provincia -> ubicacion = provincia.name }
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = !isUpdating,
                    onClick = {
                        showErrors = true
                        if (canSave) {
                            onUpdateDesempleado(nombre, apellido, dni, porfolios, disponibilidad, ubicacion)
                        }
                    }
                ) {
                    Text(if (isUpdating) "Guardando..." else "Guardar")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ProfileEditField(
    label: String,
    value: String,
    isError: Boolean = false,
    errorText: String? = null,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = isError,
        supportingText = {
            if (isError && !errorText.isNullOrBlank()) {
                Text(errorText)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SectorDropdown(
    sectors: List<SectorData>,
    selectedSectorName: String,
    isError: Boolean,
    onSectorSelected: (SectorData) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedSectorName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Sector") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true,
            isError = isError,
            supportingText = {
                if (isError) {
                    Text("Selecciona un sector")
                }
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            sectors.forEach { sector ->
                DropdownMenuItem(
                    text = { Text(sector.nombre) },
                    onClick = {
                        onSectorSelected(sector)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProvinciaDropdown(
    provincias: List<ProvinciaData>,
    selectedProvinciaName: String,
    isError: Boolean,
    onProvinciaSelected: (ProvinciaData) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedProvinciaName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Ubicacion") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true,
            isError = isError,
            supportingText = {
                if (isError) {
                    Text("Selecciona una provincia")
                }
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            provincias.forEach { provincia ->
                DropdownMenuItem(
                    text = { Text(provincia.name) },
                    onClick = {
                        onProvinciaSelected(provincia)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisponibilidadDropdown(
    selectedDisponibilidad: String,
    isError: Boolean,
    onDisponibilidadSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedDisponibilidad,
            onValueChange = {},
            readOnly = true,
            label = { Text("Disponibilidad") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true,
            isError = isError,
            supportingText = {
                if (isError) {
                    Text("Selecciona una disponibilidad")
                }
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            disponibilidadOptions.forEach { disponibilidad ->
                DropdownMenuItem(
                    text = { Text(disponibilidad) },
                    onClick = {
                        onDisponibilidadSelected(disponibilidad)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun com.google.gson.JsonObject?.getStringOrEmpty(key: String): String {
    return this
        ?.get(key)
        ?.takeIf { !it.isJsonNull }
        ?.asString
        .orEmpty()
}

private val disponibilidadOptions = listOf(
    "Tiempo completo",
    "Medio tiempo",
    "Temporal",
    "Freelance"
)

private fun isValidSpanishDniOrNie(value: String): Boolean {
    val document = value.trim().uppercase()
    val nifLetters = "TRWAGMYFPDXBNJZSQVHLCKE"
    val numericPart = when {
        Regex("^\\d{8}[A-Z]$").matches(document) -> document.substring(0, 8)
        Regex("^[XYZ]\\d{7}[A-Z]$").matches(document) -> {
            val prefix = when (document.first()) {
                'X' -> '0'
                'Y' -> '1'
                else -> '2'
            }
            prefix + document.substring(1, 8)
        }
        else -> return false
    }

    val expectedLetter = nifLetters[numericPart.toInt() % 23]
    return document.last() == expectedLetter
}

private fun isValidSpanishCif(value: String): Boolean {
    val cif = value.trim().uppercase()
    if (!Regex("^[ABCDEFGHJKLMNPQRSUVW]\\d{7}[0-9A-J]$").matches(cif)) return false

    val entityType = cif.first()
    val digits = cif.substring(1, 8).map { it.digitToInt() }
    val sum = digits.mapIndexed { index, digit ->
        if (index % 2 == 0) {
            val doubled = digit * 2
            doubled / 10 + doubled % 10
        } else {
            digit
        }
    }.sum()

    val controlDigit = (10 - (sum % 10)) % 10
    val controlLetter = "JABCDEFGHI"[controlDigit]

    return when (entityType) {
        'A', 'B', 'E', 'H' -> cif.last() == controlDigit.digitToChar()
        'K', 'P', 'Q', 'S' -> cif.last() == controlLetter
        else -> cif.last() == controlDigit.digitToChar() || cif.last() == controlLetter
    }
}

private fun isValidWebAddress(value: String): Boolean {
    return Regex(
        pattern = "^(https?://)?([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}(:\\d{2,5})?(/\\S*)?$",
        option = RegexOption.IGNORE_CASE
    ).matches(value.trim())
}

@Composable
private fun ProfileTabs(
    selectedTab: ProfileTab,
    onTabSelected: (ProfileTab) -> Unit
) {
    val tabs = listOf(
        ProfileTab.Posts to Icons.Filled.GridOn,
        ProfileTab.Photos to Icons.Filled.PhotoLibrary,
        ProfileTab.Videos to Icons.Filled.PlayCircle,
        ProfileTab.Pdfs to Icons.Filled.Description
    )

    TabRow(
        selectedTabIndex = tabs.indexOfFirst { it.first == selectedTab },
        containerColor = Color.White,
        contentColor = Color.Black
    ) {
        tabs.forEach { (tab, icon) ->
            Tab(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = tab.name,
                        modifier = Modifier.size(28.dp)
                    )
                }
            )
        }
    }
}
