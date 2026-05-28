package leo.rios.officium.jobOffers.presentation.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import leo.rios.officium.core.presentation.components.OfficiumBottomNavigation
import leo.rios.officium.jobOffers.data.JobOfferDto
import leo.rios.officium.empresaProfile.data.ProvinciaData
import leo.rios.officium.jobOffers.presentation.composables.JobOfferCard
import leo.rios.officium.jobOffers.presentation.model.JobOfferFormState
import leo.rios.officium.jobOffers.presentation.model.jobOfferStatusOptions
import leo.rios.officium.jobOffers.presentation.viewModel.JobOffersViewModel
import leo.rios.officium.subscriptions.data.CategoriaDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobOffersScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onSecondClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onUserProfileClick: (Int) -> Unit,
    viewModel: JobOffersViewModel = hiltViewModel()
) {
    val offers by viewModel.offers.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val provincias by viewModel.provincias.collectAsState()
    val profilePhoto by viewModel.profilePhoto.collectAsState()
    val profileRole by viewModel.profileRole.collectAsState()
    val currentProfileId by viewModel.currentProfileId.collectAsState()
    val applicationsByOffer by viewModel.applicationsByOffer.collectAsState()
    val message by viewModel.message.collectAsState()
    val context = LocalContext.current
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingOffer by remember { mutableStateOf<JobOfferDto?>(null) }
    val listState = rememberLazyListState()
    val shouldLoadMore = remember(listState, offers) {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            offers.isNotEmpty() && lastVisible >= offers.lastIndex - 2
        }
    }

    LaunchedEffect(message) {
        message?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            viewModel.loadNextPage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis ofertas") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (profileRole == "Empresa") {
                FloatingActionButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Crear oferta")
                }
            }
        },
        bottomBar = {
            OfficiumBottomNavigation(
                profileImageUrl = profilePhoto,
                profileRole = profileRole,
                hasNotifications = true,
                onHomeClick = onHomeClick,
                onSecondClick = onSecondClick,
                onNotificationsClick = onNotificationsClick,
                onSearchClick = onSearchClick,
                onProfileClick = onProfileClick
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues),
            state = listState
        ) {
            items(offers, key = { it.idOferta }) { offer ->
                JobOfferCard(
                    offer = offer,
                    currentRole = profileRole,
                    currentProfileId = currentProfileId,
                    isOwner = profileRole == "Empresa",
                    applications = applicationsByOffer[offer.idOferta],
                    onEditClick = { editingOffer = it },
                    onApplyClick = { viewModel.applyToOffer(it.idOferta) },
                    onDeleteApplicationClick = { jobOffer, application ->
                        viewModel.deleteApplication(jobOffer.idOferta, application.idAplicacion)
                    },
                    onLoadApplicationsClick = { viewModel.loadApplications(it.idOferta) },
                    onApplicationStatusChange = { application, status ->
                        viewModel.updateApplicationStatus(
                            offerId = offer.idOferta,
                            applicationId = application.idAplicacion,
                            status = status
                        )
                    },
                    onCompanyClick = onUserProfileClick,
                    onApplicantClick = onUserProfileClick
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateJobOfferDialog(
            title = "Crear oferta",
            categories = categories,
            provincias = provincias,
            onDismiss = { showCreateDialog = false },
            onSave = { form ->
                viewModel.createOffer(form)
                showCreateDialog = false
            }
        )
    }

    editingOffer?.let { offer ->
        CreateJobOfferDialog(
            title = "Editar oferta",
            offer = offer,
            categories = categories,
            provincias = provincias,
            onDismiss = { editingOffer = null },
            onSave = { form ->
                viewModel.updateOffer(offer.idOferta, form)
                editingOffer = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateJobOfferDialog(
    title: String,
    offer: JobOfferDto? = null,
    categories: List<CategoriaDto>,
    provincias: List<ProvinciaData>,
    onDismiss: () -> Unit,
    onSave: (JobOfferFormState) -> Unit
) {
    var form by remember(offer?.idOferta, categories) {
        mutableStateOf(
            JobOfferFormState(
                title = offer?.titulo.orEmpty(),
                description = offer?.descripcion.orEmpty(),
                categoryId = offer?.idCategoria,
                categoryName = offer?.categoryName.orEmpty(),
                location = offer?.ubicacion.orEmpty(),
                status = offer?.estado ?: "Abierta"
            )
        )
    }
    var categoryExpanded by remember { mutableStateOf(false) }
    var provinciaExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = form.title,
                    onValueChange = { form = form.copy(title = it) },
                    label = { Text("Titulo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = form.description,
                    onValueChange = { form = form.copy(description = it) },
                    label = { Text("Descripcion") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    minLines = 3
                )
                DropdownField(
                    label = "Categoria",
                    value = form.categoryName,
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it },
                    items = categories.map { it.nombre },
                    onItemSelected = { name ->
                        val category = categories.firstOrNull { it.nombre == name }
                        form = form.copy(
                            categoryId = category?.idCategoria,
                            categoryName = name
                        )
                    }
                )
                DropdownField(
                    label = "Ubicacion",
                    value = form.location,
                    expanded = provinciaExpanded,
                    onExpandedChange = { provinciaExpanded = it },
                    items = provincias.map { it.name },
                    onItemSelected = { form = form.copy(location = it) }
                )
                DropdownField(
                    label = "Estado",
                    value = form.status,
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = it },
                    items = jobOfferStatusOptions,
                    onItemSelected = { form = form.copy(status = it) }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(form) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    items: List<String>,
    onItemSelected: (String) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onExpandedChange(!expanded) },
        modifier = Modifier.padding(top = 8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onItemSelected(item)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}
