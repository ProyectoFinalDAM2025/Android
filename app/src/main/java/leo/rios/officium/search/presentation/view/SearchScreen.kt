package leo.rios.officium.search.presentation.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import leo.rios.officium.jobOffers.presentation.composables.JobOfferCard
import leo.rios.officium.jobOffers.presentation.model.jobOfferStatusOptions
import leo.rios.officium.jobOffers.presentation.view.CreateJobOfferDialog
import leo.rios.officium.search.presentation.viewModel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onSecondClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onUserProfileClick: (Int) -> Unit,
    onOfferDetailClick: (Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val filters by viewModel.filters.collectAsState()
    val offers by viewModel.offers.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val provincias by viewModel.provincias.collectAsState()
    val profilePhoto by viewModel.profilePhoto.collectAsState()
    val profileRole by viewModel.profileRole.collectAsState()
    val currentProfileId by viewModel.currentProfileId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var editingOffer by remember { mutableStateOf<JobOfferDto?>(null) }
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
                title = { Text("Buscar ofertas") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            OfficiumBottomNavigation(
                profileImageUrl = profilePhoto,
                profileRole = profileRole,
                hasNotifications = true,
                onHomeClick = onHomeClick,
                onSecondClick = onSecondClick,
                onNotificationsClick = onNotificationsClick,
                onSearchClick = { viewModel.search(reset = true) },
                onProfileClick = onProfileClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = filters.title,
                    onValueChange = { viewModel.onFiltersChange(filters.copy(title = it)) },
                    label = { Text("Titulo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                FilterDropdownField(
                    label = { Text("Ubicacion") },
                    value = filters.location,
                    items = provincias.map { it.name },
                    onItemSelected = { viewModel.onFiltersChange(filters.copy(location = it)) }
                )
                FilterDropdownField(
                    label = { Text("Categoria") },
                    value = filters.category,
                    items = categories.map { it.nombre },
                    onItemSelected = { viewModel.onFiltersChange(filters.copy(category = it)) }
                )
                FilterDropdownField(
                    label = { Text("Estado") },
                    value = filters.status,
                    items = jobOfferStatusOptions,
                    onItemSelected = { viewModel.onFiltersChange(filters.copy(status = it)) }
                )
                Button(
                    onClick = { viewModel.search(reset = true) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null)
                    Text("Buscar")
                }
            }

            when {
                isLoading && offers.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                offers.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text("No hay ofertas para esos filtros")
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                    ) {
                        items(offers, key = { it.idOferta }) { offer ->
                            JobOfferCard(
                                offer = offer,
                                currentRole = profileRole,
                                currentProfileId = currentProfileId,
                                isOwner = false,
                                canManageOffer = profileRole == "Administrador",
                                applications = null,
                                onEditClick = { editingOffer = it },
                                onApplyClick = { viewModel.applyToOffer(it.idOferta) },
                                onDeleteApplicationClick = { jobOffer, application ->
                                    viewModel.deleteApplication(jobOffer.idOferta, application.idAplicacion)
                                },
                                onDetailClick = { onOfferDetailClick(it.idOferta) },
                                onReportClick = { jobOffer, reason, description ->
                                    viewModel.reportOffer(jobOffer.idOferta, reason, description)
                                },
                                onCompanyClick = onUserProfileClick,
                                onApplicantClick = onUserProfileClick
                            )
                        }
                    }
                }
            }
        }
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
private fun FilterDropdownField(
    label: @Composable () -> Unit,
    value: String,
    items: List<String>,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val allOption = "Todas"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value.ifBlank { allOption },
            onValueChange = {},
            readOnly = true,
            label = label,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(allOption) },
                onClick = {
                    onItemSelected("")
                    expanded = false
                }
            )
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
