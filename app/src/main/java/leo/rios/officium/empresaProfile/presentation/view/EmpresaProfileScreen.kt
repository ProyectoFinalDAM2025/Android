package leo.rios.officium.empresaProfile.presentation.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import leo.rios.officium.R
import leo.rios.officium.core.navigation.Home
import leo.rios.officium.core.navigation.VerifyData
import leo.rios.officium.core.session.AuthState
import leo.rios.officium.empresaProfile.data.ProvinciaData
import leo.rios.officium.empresaProfile.data.SectorData
import leo.rios.officium.empresaProfile.presentation.viewModel.EmpresaProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmpresaProfileScreen(
    verifyData: VerifyData,
    viewModel: EmpresaProfileViewModel,
    navigateTo: NavController
) {
    val nombreEmpresa by viewModel.nombreEmpresa.collectAsState()
    val cif by viewModel.cif.collectAsState()
    val idSector by viewModel.idSector.collectAsState()
    val ubicacion by viewModel.ubicacion.collectAsState()
    val sitioWeb by viewModel.sitioWeb.collectAsState()
    val state by viewModel.state.collectAsState()
    val foto by viewModel.foto.collectAsState()
    val sectors by viewModel.sectors.collectAsState()
    val provincias by viewModel.provincias.collectAsState()
    var showValidationErrors by remember { mutableStateOf(false) }
    val isCifValid = isValidSpanishCif(cif)
    val isSitioWebValid = isValidWebAddress(sitioWeb)
    val selectedSectorName = sectors
        .firstOrNull { it.idSector.toString() == idSector }
        ?.nombre
        .orEmpty()

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        viewModel.onFotoSelected(uri)
    }

    val authState by viewModel.authState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(authState) {
        if (authState == AuthState.AUTHENTICATED) {
            navigateTo.navigate(Home) {
                launchSingleTop = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(profileBackground())
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = foto?.let { imageUri ->
                    rememberAsyncImagePainter(imageUri)
                } ?: painterResource(id = R.drawable.acount2),
                contentDescription = if (foto == null) "Foto por defecto" else "Foto seleccionada",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            OutlinedButton(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
            ) {
                Text(if (foto == null) "Seleccionar foto" else "Cambiar foto")
            }


//            Text(text = "Perfil de empresa")
//            Text(text = "Usuario: ${verifyData.idUser}")

            OutlinedTextField(
                value = nombreEmpresa,
                onValueChange = viewModel::onNombreEmpresaChange,
                label = { Text("Nombre empresa") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = profileTextFieldColors()
            )
            OutlinedTextField(
                value = cif,
                onValueChange = viewModel::onCifChange,
                label = { Text("CIF") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = profileTextFieldColors(),
                isError = showValidationErrors && !isCifValid,
                supportingText = {
                    if (showValidationErrors && !isCifValid) {
                        Text("Introduce un CIF espanol valido")
                    }
                }
            )
            SectorDropdown(
                sectors = sectors,
                selectedSectorName = selectedSectorName,
                isError = showValidationErrors && idSector.isBlank(),
                onSectorSelected = { sector ->
                    viewModel.onIdSectorChange(sector.idSector.toString())
                }
            )
            ProvinciaDropdown(
                provincias = provincias,
                selectedProvinciaName = ubicacion,
                isError = showValidationErrors && ubicacion.isBlank(),
                onProvinciaSelected = { provincia ->
                    viewModel.onUbicacionChange(provincia.name)
                }
            )
            OutlinedTextField(
                value = sitioWeb,
                onValueChange = viewModel::onSitioWebChange,
                label = { Text("Sitio web") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = profileTextFieldColors(),
                isError = showValidationErrors && !isSitioWebValid,
                supportingText = {
                    if (showValidationErrors && !isSitioWebValid) {
                        Text("Introduce un sitio web valido")
                    }
                }
            )

            Button(
                enabled = !isLoading,
                onClick = {
                    showValidationErrors = true
                    if (isCifValid && isSitioWebValid) {
                        viewModel.sendEmpresaProfile(verifyData.idUser)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    disabledContainerColor = Color(0xFF9E9E9E),
                    contentColor = Color.White,
                    disabledContentColor = Color.White
                )
            ) {
                Text(if (isLoading) "Creando..." else "Crear empresa")
            }

            state?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = it)
            }
        }
    }
}

@Composable
private fun profileTextFieldColors(): TextFieldColors =
    TextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedIndicatorColor = Color(0xFFD1D8DE),
        unfocusedIndicatorColor = Color(0xFFD1D8DE)
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SectorDropdown(
    sectors: List<SectorData>,
    selectedSectorName: String,
    isError: Boolean,
    onSectorSelected: (SectorData) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedSectorName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Sector") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = profileTextFieldColors(),
            isError = isError,
            supportingText = {
                if (isError) {
                    Text("Selecciona un sector")
                }
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            sectors.forEach { sector ->
                DropdownMenuItem(
                    text = { Text(sector.nombre) },
                    onClick = {
                        onSectorSelected(sector)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProvinciaDropdown(
    provincias: List<ProvinciaData>,
    selectedProvinciaName: String,
    isError: Boolean,
    onProvinciaSelected: (ProvinciaData) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedProvinciaName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Ubicacion") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = profileTextFieldColors(),
            isError = isError,
            supportingText = {
                if (isError) {
                    Text("Selecciona una provincia")
                }
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            provincias.forEach { provincia ->
                DropdownMenuItem(
                    text = { Text(provincia.name) },
                    onClick = {
                        onProvinciaSelected(provincia)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun profileBackground(): Brush =
    Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFAFAFA),
            Color(0xFFF2F3F5),
            Color(0xFFE8EAED)
        )
    )

private fun isValidSpanishCif(value: String): Boolean {
    val cif = value.trim().uppercase()
    if (!Regex("^[ABCDEFGHJKLMNPQRSUVW]\\d{7}[0-9A-J]$").matches(cif)) return false

    val entityType = cif.first()
    val digits = cif.substring(1, 8).map { it.digitToInt() }
    val control = cif.last()
    val sum = digits.mapIndexed { index, digit ->
        if (index % 2 == 0) {
            val doubled = digit * 2
            doubled / 10 + doubled % 10
        } else {
            digit
        }
    }.sum()

    val controlDigit = (10 - (sum % 10)) % 10
    val controlLetter = "JABCDEFGHI"[controlDigit]

    return when (entityType) {
        'A', 'B', 'E', 'H' -> control == controlDigit.digitToChar()
        'K', 'P', 'Q', 'S' -> control == controlLetter
        else -> control == controlDigit.digitToChar() || control == controlLetter
    }
}

private fun isValidWebAddress(value: String): Boolean {
    return Regex(
        pattern = "^(https?://)?([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}(:\\d{2,5})?(/\\S*)?$",
        option = RegexOption.IGNORE_CASE
    ).matches(value.trim())
}
