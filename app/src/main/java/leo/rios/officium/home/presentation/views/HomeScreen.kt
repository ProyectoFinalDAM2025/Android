package leo.rios.officium.home.presentation.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import leo.rios.officium.R
import leo.rios.officium.core.presentation.components.OfficiumBottomNavigation
import leo.rios.officium.home.presentation.viewModel.HomeViewModel
import leo.rios.officium.userProfile.presentation.composables.ProfilePublicationList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    profilePhoto: String?,
    profileRole: String?,
    navigateToDetail: (String) -> Unit,
    onProfileClick: () -> Unit,
    onSecondClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onUserProfileClick: (Int) -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val publications by viewModel.publications.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Officium") },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
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
                profileRole = profileRole,
                hasNotifications = true,
                onHomeClick = { },
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
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFF7F7F7),
                            Color(0xFFEDEDED)
                        )
                    )
                )
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfilePublicationList(
                publications = publications,
                currentUserId = currentUserId,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(680.dp),
                onLoadMore = viewModel::loadNextPage,
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
                onAuthorClick = onUserProfileClick
            )
        }
    }
}
