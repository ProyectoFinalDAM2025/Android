package leo.rios.officium.notifications.presentation.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import leo.rios.officium.core.presentation.components.OfficiumBottomNavigation
import leo.rios.officium.notifications.presentation.composables.NotificationCard
import leo.rios.officium.notifications.presentation.viewModel.NotificationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit,
    profilePhoto: String?,
    profileRole: String?,
    onHomeClick: () -> Unit,
    onSecondClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onPublicationNotificationClick: (Int) -> Unit,
    onJobOfferNotificationClick: (Int) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(message) {
        message?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones ($unreadCount)") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Recargar")
                    }
                }
            )
        },
        bottomBar = {
            OfficiumBottomNavigation(
                profileImageUrl = profilePhoto,
                profileRole = profileRole,
                hasNotifications = unreadCount > 0,
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
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (notifications.isEmpty()) {
                Text(
                    text = "No tienes notificaciones",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF5F6B76)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = paddingValues
                ) {
                    items(notifications, key = { it.idNotificacion }) { notification ->
                        NotificationCard(
                            notification = notification,
                            onClick = {
                                viewModel.markAsRead(notification.idNotificacion)
                                notification.ruta.toNotificationTarget()?.let { target ->
                                    when (target) {
                                        is NotificationTarget.Publication -> onPublicationNotificationClick(target.id)
                                        is NotificationTarget.JobOffer -> onJobOfferNotificationClick(target.id)
                                    }
                                }
                            },
                            onMarkAsRead = { viewModel.markAsRead(notification.idNotificacion) },
                            onDelete = { viewModel.deleteNotification(notification.idNotificacion) }
                        )
                    }
                }
            }
        }
    }
}

private sealed interface NotificationTarget {
    data class Publication(val id: Int) : NotificationTarget
    data class JobOffer(val id: Int) : NotificationTarget
}

private fun String?.toNotificationTarget(): NotificationTarget? {
    if (isNullOrBlank()) return null
    val normalized = trim()
    val id = normalized.substringAfterLast('/').toIntOrNull() ?: return null
    return when {
        normalized.contains("/post/") -> NotificationTarget.Publication(id)
        normalized.contains("/ofertaEmpleo/") -> NotificationTarget.JobOffer(id)
        else -> null
    }
}
