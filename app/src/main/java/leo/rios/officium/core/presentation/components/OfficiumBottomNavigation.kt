package leo.rios.officium.core.presentation.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import leo.rios.officium.R
import leo.rios.officium.core.api.toStorageUrl

@Composable
fun OfficiumBottomNavigation(
    modifier: Modifier = Modifier,
    profileImageUrl: String? = null,
    profileRole: String? = null,
    hasNotifications: Boolean = false,
    onHomeClick: () -> Unit,
    onSecondClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val resolvedProfileImageUrl = profileImageUrl.toStorageUrl()
    val isCompany = profileRole == "Empresa"
    val secondIcon = if (isCompany) Icons.Filled.BusinessCenter else Icons.Filled.Category
    val secondDescription = if (isCompany) "Mis ofertas de empleo" else "Suscripciones"

    LaunchedEffect(resolvedProfileImageUrl) {
        Log.d("OfficiumBottomNav", "Profile image url: $resolvedProfileImageUrl")
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(72.dp)
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onHomeClick) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Home",
                    tint = Color.Black,
                    modifier = Modifier.size(34.dp)
                )
            }

            IconButton(onClick = onSecondClick) {
                Icon(
                    imageVector = secondIcon,
                    contentDescription = secondDescription,
                    tint = Color.Black,
                    modifier = Modifier.size(34.dp)
                )
            }

            IconButton(onClick = onNotificationsClick) {
                Box(contentAlignment = Alignment.TopEnd) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notificaciones",
                        tint = Color.Black,
                        modifier = Modifier.size(34.dp)
                    )
                    if (hasNotifications) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF2F5F))
                        )
                    }
                }
            }

            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Busqueda",
                    tint = Color.Black,
                    modifier = Modifier.size(36.dp)
                )
            }

            IconButton(onClick = onProfileClick) {
                AsyncImage(
                    model = resolvedProfileImageUrl,
                    contentDescription = "Perfil",
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.acount2),
                    error = painterResource(id = R.drawable.acount2),
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                )
            }
        }
    }
}
