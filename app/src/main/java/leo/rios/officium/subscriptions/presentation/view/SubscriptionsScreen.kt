package leo.rios.officium.subscriptions.presentation.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import leo.rios.officium.core.presentation.components.OfficiumBottomNavigation
import leo.rios.officium.jobOffers.presentation.composables.JobOfferCard
import leo.rios.officium.subscriptions.presentation.composables.CategoryRow
import leo.rios.officium.subscriptions.presentation.model.SubscriptionTab
import leo.rios.officium.subscriptions.presentation.viewModel.SubscriptionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen(
    onBackClick: () -> Unit,
    profilePhoto: String?,
    profileRole: String?,
    onHomeClick: () -> Unit,
    onSecondClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onOfferDetailClick: (Int) -> Unit,
    viewModel: SubscriptionsViewModel = hiltViewModel()
) {
    val availableCategories by viewModel.availableCategories.collectAsState()
    val subscribedCategories by viewModel.subscribedCategories.collectAsState()
    val myApplications by viewModel.myApplications.collectAsState()
    val currentProfileId by viewModel.currentProfileId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(SubscriptionTab.Available) }

    LaunchedEffect(message) {
        message?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Suscripciones") },
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
                onSearchClick = onSearchClick,
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
            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(
                    selectedTabIndex = when (selectedTab) {
                        SubscriptionTab.Available -> 0
                        SubscriptionTab.Subscribed -> 1
                        SubscriptionTab.Applications -> 2
                    }
                ) {
                    Tab(
                        selected = selectedTab == SubscriptionTab.Available,
                        onClick = { selectedTab = SubscriptionTab.Available },
                        text = { Text("Disponibles") }
                    )
                    Tab(
                        selected = selectedTab == SubscriptionTab.Subscribed,
                        onClick = { selectedTab = SubscriptionTab.Subscribed },
                        text = { Text("Suscritas") }
                    )
                    Tab(
                        selected = selectedTab == SubscriptionTab.Applications,
                        onClick = { selectedTab = SubscriptionTab.Applications },
                        text = { Text("Aplicaciones") }
                    )
                }

                if (isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    if (selectedTab == SubscriptionTab.Applications) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)
                        ) {
                            items(myApplications, key = { it.idOferta }) { offer ->
                                JobOfferCard(
                                    offer = offer,
                                    currentRole = profileRole,
                                    currentProfileId = currentProfileId,
                                    isOwner = false,
                                    applications = null,
                                    onDeleteApplicationClick = { jobOffer, application ->
                                        viewModel.deleteApplication(jobOffer.idOferta, application.idAplicacion)
                                    },
                                    onDetailClick = { onOfferDetailClick(it.idOferta) },
                                    onReportClick = { jobOffer, reason, description ->
                                        viewModel.reportOffer(jobOffer.idOferta, reason, description)
                                    }
                                )
                            }
                        }
                    } else {
                        val categories = if (selectedTab == SubscriptionTab.Available) {
                            availableCategories
                        } else {
                            subscribedCategories
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)
                        ) {
                            items(categories, key = { it.idCategoria }) { category ->
                                CategoryRow(
                                    category = category,
                                    subscribed = selectedTab == SubscriptionTab.Subscribed,
                                    onClick = {
                                        if (selectedTab == SubscriptionTab.Subscribed) {
                                            viewModel.unsubscribe(category.idCategoria)
                                        } else {
                                            viewModel.subscribe(category.idCategoria)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
