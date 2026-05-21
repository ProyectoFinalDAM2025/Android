package leo.rios.officium.userProfile.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import leo.rios.officium.core.api.toStorageUrl
import leo.rios.officium.userProfile.data.DocumentoDto
import leo.rios.officium.userProfile.presentation.model.ProfileTab

@Composable
fun ProfileDocumentGrid(
    selectedTab: ProfileTab,
    documents: List<DocumentoDto>,
    modifier: Modifier = Modifier,
    onDocumentClick: (DocumentoDto) -> Unit = {}
) {
    if (selectedTab == ProfileTab.Posts || documents.isEmpty()) {
        EmptyProfileTab(
            text = if (selectedTab == ProfileTab.Posts) {
                "Aun no hay publicaciones"
            } else {
                "Aun no hay contenido"
            },
            modifier = modifier
        )
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(documents, key = { it.idDocumento }) { document ->
            DocumentTile(
                document = document,
                selectedTab = selectedTab,
                onClick = { onDocumentClick(document) }
            )
        }
    }
}

@Composable
private fun DocumentTile(
    document: DocumentoDto,
    selectedTab: ProfileTab,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(Color(0xFFF0F2F5))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when (selectedTab) {
            ProfileTab.Photos -> {
                AsyncImage(
                    model = document.url.toStorageUrl(),
                    contentDescription = document.descripcion ?: document.nombreArchivo,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            ProfileTab.Videos -> {
                document.thumbnail?.takeIf { it.isNotBlank() }?.let { thumbnail ->
                    AsyncImage(
                        model = thumbnail.toStorageUrl(),
                        contentDescription = document.descripcion ?: document.nombreArchivo,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Icon(
                    imageVector = Icons.Filled.PlayCircle,
                    contentDescription = "Video",
                    tint = if (document.thumbnail.isNullOrBlank()) Color.Black else Color.White,
                    modifier = Modifier.fillMaxSize(0.34f)
                )
            }
            ProfileTab.Pdfs -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = "PDF",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.fillMaxSize(0.42f)
                    )
                    Text(
                        text = document.nombreArchivo,
                        color = Color(0xFF25313B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
            ProfileTab.Posts -> Unit
        }
    }
}

@Composable
private fun EmptyProfileTab(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 42.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(0xFF6F7782),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
