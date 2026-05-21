package leo.rios.officium.userProfile.presentation.view

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Menu
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import leo.rios.officium.R
import leo.rios.officium.core.api.toStorageUrl
import leo.rios.officium.core.presentation.components.OfficiumBottomNavigation
import leo.rios.officium.core.presentation.components.OfficiumVideoPlayer
import leo.rios.officium.empresaProfile.data.ProvinciaData
import leo.rios.officium.empresaProfile.data.SectorData
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
    onHomeClick: () -> Unit,
    onLogout: () -> Unit
) {
    val profileName by viewModel.profileName.collectAsState()
    val profileRole by viewModel.profileRole.collectAsState()
    val profilePhoto by viewModel.profilePhoto.collectAsState()
    val profileDescription by viewModel.profileDescription.collectAsState()
    val profileJson by viewModel.profileJson.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val publications by viewModel.publications.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val videos by viewModel.videos.collectAsState()
    val pdfs by viewModel.pdfs.collectAsState()
    val message by viewModel.message.collectAsState()
    val sectors by viewModel.sectors.collectAsState()
    val provincias by viewModel.provincias.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(ProfileTab.Posts) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var selectedVideoUrl by remember { mutableStateOf<String?>(null) }
    var selectedPdfUrl by remember { mutableStateOf<String?>(null) }

    val selectedDocuments = when (selectedTab) {
        ProfileTab.Posts -> emptyList()
        ProfileTab.Photos -> photos
        ProfileTab.Videos -> videos
        ProfileTab.Pdfs -> pdfs
    }

    LaunchedEffect(message) {
        message?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
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
                    IconButton(onClick = { showUploadDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Subir contenido"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
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
                    }
                }
            )
        },
        bottomBar = {
            OfficiumBottomNavigation(
                profileImageUrl = profilePhoto,
                hasNotifications = true,
                onHomeClick = onHomeClick,
                onSecondClick = { },
                onNotificationsClick = { },
                onSearchClick = { },
                onProfileClick = { }
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
                postCount = publications.size + photos.size + videos.size + pdfs.size,
                description = profileDescription ?: "Perfil profesional en Officium"
            )

            ProfileActions(
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
                    modifier = Modifier.height(520.dp)
                )
            } else {
                ProfileDocumentGrid(
                    selectedTab = selectedTab,
                    documents = selectedDocuments,
                    modifier = Modifier.height(520.dp),
                    onDocumentClick = { document ->
                        if (selectedTab == ProfileTab.Videos) {
                            selectedVideoUrl = document.url
                        } else if (selectedTab == ProfileTab.Pdfs) {
                            selectedPdfUrl = document.url
                        }
                    }
                )
            }
        }
    }

    selectedVideoUrl?.let { videoUrl ->
        VideoPreviewDialog(
            videoUrl = videoUrl,
            onDismiss = { selectedVideoUrl = null }
        )
    }

    selectedPdfUrl?.let { pdfUrl ->
        PdfPreviewDialog(
            pdfUrl = pdfUrl,
            onDismiss = { selectedPdfUrl = null }
        )
    }

    if (showUploadDialog) {
        CreateProfileContentDialog(
            isUploading = isUpdating,
            onDismiss = { showUploadDialog = false },
            onCreate = { uploadType, content, description, fileUri ->
                viewModel.createProfileContent(uploadType, content, description, fileUri)
                showUploadDialog = false
            }
        )
    }

    if (showEditDialog) {
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
            }
        )
    }
}

@Composable
private fun VideoPreviewDialog(
    videoUrl: String,
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
                videoUrl = videoUrl,
                isPlaying = true,
                muted = false,
                showControls = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            )
        }
    }
}

@Composable
private fun PdfPreviewDialog(
    pdfUrl: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var pageIndex by remember(pdfUrl) { mutableStateOf(0) }
    var preview by remember(pdfUrl, pageIndex) { mutableStateOf<PdfPagePreview?>(null) }
    var errorMessage by remember(pdfUrl) { mutableStateOf<String?>(null) }

    LaunchedEffect(pdfUrl, pageIndex) {
        preview = null
        errorMessage = null
        runCatching {
            withContext(Dispatchers.IO) {
                renderPdfPage(
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
        }
    }
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
    description: String
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
            )

            Spacer(modifier = Modifier.width(22.dp))

            ProfileMetric(
                count = postCount.toString(),
                label = "posts",
                modifier = Modifier.weight(1f)
            )
            ProfileMetric(
                count = "0",
                label = "imagenes",
                modifier = Modifier.weight(1f)
            )
            ProfileMetric(
                count = "0",
                label = "videos",
                modifier = Modifier.weight(1f)
            )
            ProfileMetric(
                count = "0",
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
    onEditClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ProfileActionButton(
            text = "Editar perfil",
            onClick = onEditClick,
            modifier = Modifier.weight(1f)
        )
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
    onUpdateEmpresa: (String, String, String, String, String) -> Unit
) {
    val profile = remember(profileJson) {
        runCatching { JsonParser.parseString(profileJson).asJsonObject }.getOrNull()
    }

    if (role == "Empresa") {
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
