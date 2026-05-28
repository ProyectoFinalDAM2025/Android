package leo.rios.officium.notifications.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import leo.rios.officium.notifications.data.NotificationDto

@Composable
fun NotificationCard(
    notification: NotificationDto,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.leido) Color.White else Color(0xFFEFF7FF)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .clip(CircleShape)
                    .background(if (notification.leido) Color(0xFFB8C0C8) else Color(0xFFFF2F5F))
                    .padding(5.dp)
            ) {}

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.titulo.ifBlank { "Notificacion" },
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF16202A)
                )
                Text(
                    text = notification.mensaje,
                    color = Color(0xFF34404A),
                    modifier = Modifier.padding(top = 4.dp)
                )
                notification.fechaNotificacion?.let {
                    Text(
                        text = it,
                        color = Color(0xFF75808A),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            if (!notification.leido) {
                IconButton(onClick = onMarkAsRead) {
                    Icon(Icons.Filled.Done, contentDescription = "Marcar como leida")
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar notificacion")
            }
        }
    }
}
