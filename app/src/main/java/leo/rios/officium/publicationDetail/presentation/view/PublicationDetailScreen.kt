package leo.rios.officium.publicationDetail.presentation.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import leo.rios.officium.core.presentation.components.OfficiumBottomNavigation
import leo.rios.officium.publicationDetail.presentation.viewModel.PublicationDetailViewModel
import leo.rios.officium.userProfile.presentation.composables.ProfilePublicationList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicationDetailScreen(
    publicationId: Int,
    profilePhoto: String?,
    profileRole: String?,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onSecondClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAuthorClick: (Int) -> Unit,
    viewModel: PublicationDetailViewModel = hiltViewModel()
) {
    val publication by viewModel.publication.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val currentUserRole by viewModel.currentUserRole.collectAsState()
    val message by viewModel.message.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(publicationId) {
        viewModel.load(publicationId)
    }

    LaunchedEffect(message) {
        message?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Publicacion") },
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
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            val item = publication
            if (item == null) {
                Text("No se pudo cargar la publicacion", color = Color(0xFF5F6B76))
            } else {
                ProfilePublicationList(
                    publications = listOf(item),
                    currentUserId = currentUserId,
                    canManageContent = currentUserRole == "Administrador",
                    modifier = Modifier
                        .fillMaxSize()
                        .height(520.dp),
                    onLikeClick = { viewModel.likePublication(it.idPublicacion, it.likedByCurrentUser) },
                    onCommentSubmit = { pub, content -> viewModel.addComment(pub.idPublicacion, content) },
                    onPublicationEdit = { pub, content, fileUri ->
                        viewModel.updatePublication(pub.idPublicacion, content, fileUri)
                    },
                    onPublicationDelete = { viewModel.deletePublication(it.idPublicacion) },
                    onCommentEdit = { comment, content -> viewModel.updateComment(comment.idComentario, content) },
                    onCommentDelete = { viewModel.deleteComment(it.idComentario) },
                    onReport = { pub, reason, description ->
                        viewModel.reportPublication(pub.idPublicacion, reason, description)
                    },
                    onAuthorClick = onAuthorClick
                )
            }
        }
    }
}
