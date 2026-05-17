package leo.rios.officium.desempleadoProfile.presentation.view

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
import leo.rios.officium.desempleadoProfile.presentation.composables.DisponibilidadDropdown
import leo.rios.officium.desempleadoProfile.presentation.viewModel.DesempleadoProfileViewModel

@Composable
fun DesempleadoProfileScreen(
    verifyData: VerifyData,
    viewModel: DesempleadoProfileViewModel,
    navigateTo: NavController
) {
    val authState by viewModel.authState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val nombre by viewModel.nombre.collectAsState()
    val apellido by viewModel.apellido.collectAsState()
    val dni by viewModel.dni.collectAsState()
    val porfolios by viewModel.porfolios.collectAsState()
    val disponibilidad by viewModel.disponibilidad.collectAsState()
    val state by viewModel.state.collectAsState()
    val foto by viewModel.foto.collectAsState()
    var showValidationErrors by remember { mutableStateOf(false) }
    val isDniValid = isValidSpanishDniOrNie(dni)
    val isPorfoliosValid = isValidWebAddress(porfolios)

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        viewModel.onFotoSelected(uri)
    }

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

//            Text(text = "Perfil de desempleado")
//            Text(text = "Usuario: ${verifyData.idUser}")

            OutlinedTextField(
                value = nombre,
                onValueChange = viewModel::onNombreChange,
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = profileTextFieldColors()
            )
            OutlinedTextField(
                value = apellido,
                onValueChange = viewModel::onApellidoChange,
                label = { Text("Apellido") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = profileTextFieldColors()
            )
            OutlinedTextField(
                value = dni,
                onValueChange = viewModel::onDniChange,
                label = { Text("DNI") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = profileTextFieldColors(),
                isError = showValidationErrors && !isDniValid,
                supportingText = {
                    if (showValidationErrors && !isDniValid) {
                        Text("Introduce un DNI o NIE espanol valido")
                    }
                }
            )
            OutlinedTextField(
                value = porfolios,
                onValueChange = viewModel::onPorfoliosChange,
                label = { Text("Porfolios") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = profileTextFieldColors(),
                isError = showValidationErrors && !isPorfoliosValid,
                supportingText = {
                    if (showValidationErrors && !isPorfoliosValid) {
                        Text("Introduce una direccion web valida")
                    }
                }
            )
            DisponibilidadDropdown(
                disponibilidadSeleccionada = disponibilidad,
                onDisponibilidadSelected = viewModel::onDisponibilidadChange
            )


            Button(
                enabled = !isLoading,
                onClick = {
                    showValidationErrors = true
                    if (isDniValid && isPorfoliosValid) {
                        viewModel.sendDesempleadoProfile(verifyData.idUser)
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
                Text(if (isLoading) "Creando..." else "Crear desempleado")
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

private fun profileBackground(): Brush =
    Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFAFAFA),
            Color(0xFFF2F3F5),
            Color(0xFFE8EAED)
        )
    )

private fun isValidSpanishDniOrNie(value: String): Boolean {
    val document = value.trim().uppercase()
    val nifLetters = "TRWAGMYFPDXBNJZSQVHLCKE"
    val numericPart = when {
        Regex("^\\d{8}[A-Z]$").matches(document) -> document.substring(0, 8)
        Regex("^[XYZ]\\d{7}[A-Z]$").matches(document) -> {
            val prefix = when (document.first()) {
                'X' -> '0'
                'Y' -> '1'
                else -> '2'
            }
            prefix + document.substring(1, 8)
        }
        else -> return false
    }

    val expectedLetter = nifLetters[numericPart.toInt() % 23]
    return document.last() == expectedLetter
}

private fun isValidWebAddress(value: String): Boolean {
    return Regex(
        pattern = "^(https?://)?([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}(:\\d{2,5})?(/\\S*)?$",
        option = RegexOption.IGNORE_CASE
    ).matches(value.trim())
}
