package leo.rios.officium.userProfile.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import leo.rios.officium.core.api.toStorageUrl
import leo.rios.officium.core.presentation.components.OfficiumVideoPlayer
import leo.rios.officium.userProfile.data.PublicacionDto

@Composable
fun ProfilePublicationList(
    publications: List<PublicacionDto>,
    modifier: Modifier = Modifier
) {
    if (publications.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 42.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Aun no hay publicaciones",
                color = Color(0xFF6F7782),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        return
    }

    val listState = rememberLazyListState()
    val currentVisibleVideoId = remember(publications, listState) {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo
                .mapNotNull { visibleItem ->
                    publications.getOrNull(visibleItem.index)
                        ?.takeIf { it.tipoArchivo == "Video" && !it.archivo.isNullOrBlank() }
                }
                .maxByOrNull { publication ->
                    val visibleItem = listState.layoutInfo.visibleItemsInfo
                        .firstOrNull { publications.getOrNull(it.index)?.idPublicacion == publication.idPublicacion }

                    visibleItem?.let { item ->
                        minOf(item.offset + item.size, listState.layoutInfo.viewportEndOffset) -
                            maxOf(item.offset, listState.layoutInfo.viewportStartOffset)
                    } ?: 0
                }
                ?.idPublicacion
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(publications, key = { it.idPublicacion }) { publication ->
            PublicationItem(
                publication = publication,
                isVideoPlaying = publication.idPublicacion == currentVisibleVideoId.value
            )
        }
    }
}

@Composable
private fun PublicationItem(
    publication: PublicacionDto,
    isVideoPlaying: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = publication.contenido,
            color = Color(0xFF25313B),
            fontSize = 15.sp,
            maxLines = 6,
            overflow = TextOverflow.Ellipsis
        )

        publication.archivo?.takeIf { it.isNotBlank() }?.let { archivo ->
            PublicationAttachment(
                url = archivo,
                thumbnail = publication.thumbnail,
                type = publication.tipoArchivo,
                isVideoPlaying = isVideoPlaying
            )
        }
    }
}

@Composable
private fun PublicationAttachment(
    url: String,
    thumbnail: String?,
    type: String?,
    isVideoPlaying: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp, max = 280.dp)
            .background(Color(0xFFF0F2F5)),
        contentAlignment = Alignment.Center
    ) {
        when (type) {
            "Foto" -> AsyncImage(
                model = url.toStorageUrl(),
                contentDescription = "Archivo de publicacion",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            "Video" -> OfficiumVideoPlayer(
                videoUrl = url,
                isPlaying = isVideoPlaying,
                muted = false,
                showControls = true,
                modifier = Modifier.fillMaxSize()
            )
            "PDF" -> Icon(
                imageVector = Icons.Filled.Description,
                contentDescription = "PDF",
                tint = Color(0xFFD32F2F)
            )
            else -> Text(
                text = "Archivo adjunto",
                color = Color(0xFF6F7782),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
