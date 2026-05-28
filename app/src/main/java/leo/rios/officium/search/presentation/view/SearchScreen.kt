package leo.rios.officium.search.presentation.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import leo.rios.officium.core.presentation.components.OfficiumBottomNavigation
import leo.rios.officium.jobOffers.presentation.composables.JobOfferCard
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
    viewModel: SearchViewModel = hiltViewModel()
) {
    val filters by viewModel.filters.collectAsState()
    val offers by viewModel.offers.collectAsState()
    val profilePhoto by viewModel.profilePhoto.collectAsState()
    val profileRole by viewModel.profileRole.collectAsState()
    val currentProfileId by viewModel.currentProfileId.collectAsState()
    val message by viewModel.message.collectAsState()
    val context = LocalContext.current
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
                onSearchClick = {},
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
                OutlinedTextField(
                    value = filters.location,
                    onValueChange = { viewModel.onFiltersChange(filters.copy(location = it)) },
                    label = { Text("Ubicacion") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = filters.category,
                    onValueChange = { viewModel.onFiltersChange(filters.copy(category = it)) },
                    label = { Text("Categoria") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = filters.status,
                    onValueChange = { viewModel.onFiltersChange(filters.copy(status = it)) },
                    label = { Text("Estado") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = { viewModel.search(reset = true) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null)
                    Text("Buscar")
                }
            }

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
                        applications = null,
                        onApplyClick = { viewModel.applyToOffer(it.idOferta) },
                        onDeleteApplicationClick = { jobOffer, application ->
                            viewModel.deleteApplication(jobOffer.idOferta, application.idAplicacion)
                        },
                        onCompanyClick = onUserProfileClick,
                        onApplicantClick = onUserProfileClick
                    )
                }
            }
        }
    }
}
