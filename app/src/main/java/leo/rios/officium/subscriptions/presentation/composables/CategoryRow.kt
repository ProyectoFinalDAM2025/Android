package leo.rios.officium.subscriptions.presentation.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import leo.rios.officium.subscriptions.data.CategoriaDto

@Composable
fun CategoryRow(
    category: CategoriaDto,
    subscribed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.nombre,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (subscribed) "Recibiras avisos de esta categoria" else "Disponible para suscribirte"
            )
        }

        if (subscribed) {
            OutlinedButton(onClick = onClick) {
                Icon(Icons.Filled.Close, contentDescription = null)
                Text("Quitar")
            }
        } else {
            Button(onClick = onClick) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("Suscribir")
            }
        }
    }
}
