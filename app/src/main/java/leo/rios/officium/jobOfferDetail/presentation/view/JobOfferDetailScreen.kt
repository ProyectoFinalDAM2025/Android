package leo.rios.officium.jobOfferDetail.presentation.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import leo.rios.officium.jobOfferDetail.presentation.viewModel.JobOfferDetailViewModel
import leo.rios.officium.jobOffers.data.JobOfferDto
import leo.rios.officium.jobOffers.presentation.composables.JobOfferCard
import leo.rios.officium.jobOffers.presentation.view.CreateJobOfferDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobOfferDetailScreen(
    offerId: Int,
    profilePhoto: String?,
    profileRole: String?,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onSecondClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onUserProfileClick: (Int) -> Unit,
    viewModel: JobOfferDetailViewModel = hiltViewModel()
) {
    val offer by viewModel.offer.collectAsState()
    val applications by viewModel.applications.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val provincias by viewModel.provincias.collectAsState()
    val currentProfileId by viewModel.currentProfileId.collectAsState()
    val message by viewModel.message.collectAsState()
    val context = LocalContext.current
    var editingOffer by remember { mutableStateOf<JobOfferDto?>(null) }

    LaunchedEffect(offerId) {
        viewModel.load(offerId)
    }

    LaunchedEffect(message) {
        message?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Oferta de empleo") },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            val item = offer
            if (item == null) {
                Text("No se pudo cargar la oferta", color = Color(0xFF5F6B76))
            } else {
                val isOfferOwner = profileRole == "Empresa" && currentProfileId == item.idEmpresa
                JobOfferCard(
                    offer = item,
                    currentRole = profileRole,
                    currentProfileId = currentProfileId,
                    isOwner = isOfferOwner,
                    canManageOffer = profileRole == "Administrador",
                    applications = applications,
                    onEditClick = { editingOffer = it },
                    onDeleteClick = { jobOffer ->
                        viewModel.deleteOffer(jobOffer.idOferta, onDeleted = onBackClick)
                    },
                    onApplyClick = { viewModel.applyToOffer(it.idOferta) },
                    onDeleteApplicationClick = { jobOffer, application ->
                        viewModel.deleteApplication(jobOffer.idOferta, application.idAplicacion)
                    },
                    onLoadApplicationsClick = { viewModel.loadApplications(it.idOferta) },
                    onApplicationStatusChange = { application, status ->
                        viewModel.updateApplicationStatus(
                            offerId = item.idOferta,
                            applicationId = application.idAplicacion,
                            status = status
                        )
                    },
                    onCompanyClick = onUserProfileClick,
                    onApplicantClick = onUserProfileClick,
                    onReportClick = { jobOffer, reason, description ->
                        viewModel.reportOffer(jobOffer.idOferta, reason, description)
                    }
                )
            }
        }
    }

    editingOffer?.let { item ->
        CreateJobOfferDialog(
            title = "Editar oferta",
            offer = item,
            categories = categories,
            provincias = provincias,
            onDismiss = { editingOffer = null },
            onSave = { form ->
                viewModel.updateOffer(item.idOferta, form)
                editingOffer = null
            }
        )
    }
}
