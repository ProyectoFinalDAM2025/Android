package leo.rios.officium.jobOffers.presentation.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import coil3.compose.AsyncImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import leo.rios.officium.R
import leo.rios.officium.core.api.toStorageUrl
import leo.rios.officium.jobOffers.data.JobApplicationDto
import leo.rios.officium.jobOffers.data.JobOfferDto
import leo.rios.officium.jobOffers.presentation.model.jobApplicationStatusOptions

@Composable
fun JobOfferCard(
    offer: JobOfferDto,
    currentRole: String?,
    currentProfileId: Int? = null,
    isOwner: Boolean,
    applications: List<JobApplicationDto>?,
    modifier: Modifier = Modifier,
    onEditClick: (JobOfferDto) -> Unit = {},
    onApplyClick: (JobOfferDto) -> Unit = {},
    onDeleteApplicationClick: (JobOfferDto, JobApplicationDto) -> Unit = { _, _ -> },
    onLoadApplicationsClick: (JobOfferDto) -> Unit = {},
    onApplicationStatusChange: (JobApplicationDto, String) -> Unit = { _, _ -> },
    onDetailClick: ((JobOfferDto) -> Unit)? = null,
    onReportClick: (JobOfferDto, String, String) -> Unit = { _, _, _ -> },
    onCompanyClick: (Int) -> Unit = {},
    onApplicantClick: (Int) -> Unit = {}
) {
    var showMenu by remember(offer.idOferta) { mutableStateOf(false) }
    var showApplications by remember(offer.idOferta) { mutableStateOf(false) }
    var showReport by remember(offer.idOferta) { mutableStateOf(false) }
    val visibleApplications = applications.orEmpty()
    val currentApplication = offer.currentUserApplication(currentProfileId)
    val shouldCollapseDescription = onDetailClick != null
    val collapsedDescription = offer.descripcion.takeWords(maxWords = 15)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9FC))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = offer.empresa?.foto.toStorageUrl(),
                    contentDescription = "Foto empresa",
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.acount2),
                    error = painterResource(id = R.drawable.acount2),
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                        .clickable {
                            offer.empresa?.idUsuario?.let(onCompanyClick)
                        }
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            offer.empresa?.idUsuario?.let(onCompanyClick)
                        }
                ) {
                    Text(
                        text = offer.empresa?.nombreEmpresa ?: "Empresa",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = offer.categoryName.ifBlank { "Oferta de empleo" },
                        color = Color(0xFF5F6B76)
                    )
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Opciones oferta")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (isOwner) {
                            DropdownMenuItem(
                                text = { Text("Editar") },
                                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onEditClick(offer)
                                }
                            )
                        }
                        if (!isOwner) {
                            DropdownMenuItem(
                                text = { Text("Reportar") },
                                leadingIcon = { Icon(Icons.Filled.Flag, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    showReport = true
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = offer.titulo, fontWeight = FontWeight.Bold)
            Text(
                text = if (shouldCollapseDescription) collapsedDescription else offer.descripcion,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (shouldCollapseDescription && collapsedDescription != offer.descripcion) {
                TextButton(onClick = { onDetailClick?.invoke(offer) }) {
                    Text("Leer mas")
                }
            }
            Text(text = offer.ubicacion, color = Color(0xFF5F6B76), modifier = Modifier.padding(top = 8.dp))
            Text(text = offer.estado, color = Color(0xFF0F6C45))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${offer.applicationCount} aplicaciones",
                    color = Color(0xFF25313B),
                    modifier = Modifier.weight(1f)
                )
                onDetailClick?.let { detailClick ->
                    TextButton(onClick = { detailClick(offer) }) {
                        Icon(Icons.Filled.Info, contentDescription = null)
                        Text("Detalle")
                    }
                }
                if (currentRole == "Desempleado" && !isOwner) {
                    if (currentApplication == null) {
                        Button(onClick = { onApplyClick(offer) }) {
                            Text("Aplicar")
                        }
                    } else {
                        OutlinedButton(onClick = { onDeleteApplicationClick(offer, currentApplication) }) {
                            Text("Retirar")
                        }
                    }
                }
                if (isOwner) {
                    OutlinedButton(
                        onClick = {
                            val opening = !showApplications
                            showApplications = opening
                            if (opening && applications == null) {
                                onLoadApplicationsClick(offer)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (showApplications) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null
                        )
                        Text("Aplicantes")
                    }
                }
            }

            if (showApplications && isOwner) {
                Spacer(modifier = Modifier.height(8.dp))
                if (visibleApplications.isEmpty()) {
                    Text("Todavia no hay aplicaciones", color = Color(0xFF5F6B76))
                } else {
                    visibleApplications.forEach { application ->
                        ApplicationRow(
                            application = application,
                            onApplicantClick = onApplicantClick,
                            onStatusSelected = { status -> onApplicationStatusChange(application, status) }
                        )
                    }
                }
            }
        }
    }

    if (showReport) {
        ReportJobOfferDialog(
            onDismiss = { showReport = false },
            onReport = { reason, description ->
                showReport = false
                onReportClick(offer, reason, description)
            }
        )
    }
}

private fun String.takeWords(maxWords: Int): String {
    val words = trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return if (words.size <= maxWords) {
        this
    } else {
        words.take(maxWords).joinToString(" ") + "..."
    }
}

@Composable
private fun ReportJobOfferDialog(
    onDismiss: () -> Unit,
    onReport: (String, String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reportar oferta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripcion") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(onClick = { onReport(reason, description) }) {
                Text("Reportar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApplicationRow(
    application: JobApplicationDto,
    onApplicantClick: (Int) -> Unit,
    onStatusSelected: (String) -> Unit
) {
    var expanded by remember(application.idAplicacion) { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = application.desempleado?.foto.toStorageUrl(),
            contentDescription = "Foto aplicante",
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.acount2),
            error = painterResource(id = R.drawable.acount2),
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                .clickable {
                    application.desempleado?.idUsuario?.let(onApplicantClick)
                }
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable {
                    application.desempleado?.idUsuario?.let(onApplicantClick)
                }
        ) {
            Text(
                text = application.desempleado?.displayName ?: "Desempleado",
                fontWeight = FontWeight.SemiBold
            )
            Text(text = application.fechaAplicacion.orEmpty(), color = Color(0xFF5F6B76))
        }
        Box {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = application.estado,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .size(width = 150.dp, height = 56.dp),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    jobApplicationStatusOptions.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status) },
                            onClick = {
                                expanded = false
                                onStatusSelected(status)
                            }
                        )
                    }
                }
            }
        }
    }
}
