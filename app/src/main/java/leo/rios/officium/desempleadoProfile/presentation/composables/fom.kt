package leo.rios.officium.desempleadoProfile.presentation.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import leo.rios.officium.empresaProfile.data.ProvinciaData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisponibilidadDropdown(
    disponibilidadSeleccionada: String,
    onDisponibilidadSelected: (String) -> Unit
) {

    val opcionesDisponibilidad = listOf(
        "Tiempo completo",
        "Medio tiempo",
        "Temporal",
        "Freelance"
    )

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {

        OutlinedTextField(
            value = disponibilidadSeleccionada,
            onValueChange = {},
            readOnly = true,
            label = {
                Text("Disponibilidad")
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color(0xFFD1D8DE),
                unfocusedIndicatorColor = Color(0xFFD1D8DE)
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {

            opcionesDisponibilidad.forEach { opcion ->

                DropdownMenuItem(
                    text = {
                        Text(opcion)
                    },
                    onClick = {
                        onDisponibilidadSelected(opcion)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvinciaDropdown(
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
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color(0xFFD1D8DE),
                unfocusedIndicatorColor = Color(0xFFD1D8DE)
            ),
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
