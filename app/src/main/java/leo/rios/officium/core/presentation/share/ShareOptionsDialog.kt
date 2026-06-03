package leo.rios.officium.core.presentation.share

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

private const val SHARE_BASE_URL = "https://api.officium.es"

fun buildProfileShareLink(userId: Int): String = "$SHARE_BASE_URL/perfil/$userId"

fun buildPublicationShareLink(publicationId: Int): String = "$SHARE_BASE_URL/publicacion/$publicationId"

fun buildJobOfferShareLink(offerId: Int): String = "$SHARE_BASE_URL/oferta-empleo/$offerId"

@Composable
fun ShareOptionsDialog(
    title: String,
    link: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Compartir") },
        text = {
            Column {
                ShareActionButton(
                    text = "Copiar link",
                    icon = { Icon(Icons.Filled.ContentCopy, contentDescription = null) },
                    onClick = {
                        copyToClipboard(context, link)
                        onDismiss()
                    }
                )
                ShareActionButton(
                    text = "Compartir con WhatsApp",
                    icon = { Icon(Icons.Filled.Share, contentDescription = null) },
                    onClick = {
                        openShareUrl(
                            context = context,
                            url = "https://wa.me/?text=${Uri.encode("$title\n$link")}"
                        )
                        onDismiss()
                    }
                )
                ShareActionButton(
                    text = "Compartir con X",
                    icon = { Text("X") },
                    onClick = {
                        openShareUrl(
                            context = context,
                            url = "https://twitter.com/intent/tweet?text=${Uri.encode(title)}&url=${Uri.encode(link)}"
                        )
                        onDismiss()
                    }
                )
                ShareActionButton(
                    text = "Compartir con Facebook",
                    icon = { Icon(Icons.Filled.Facebook, contentDescription = null) },
                    onClick = {
                        openShareUrl(
                            context = context,
                            url = "https://www.facebook.com/sharer/sharer.php?u=${Uri.encode(link)}"
                        )
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ShareActionButton(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        icon()
        Text(text)
    }
}

private fun copyToClipboard(context: Context, link: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Officium link", link))
    Toast.makeText(context, "Link copiado", Toast.LENGTH_SHORT).show()
}

private fun openShareUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "No se pudo abrir la opcion de compartir", Toast.LENGTH_SHORT).show()
    }
}
