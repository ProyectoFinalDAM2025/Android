package leo.rios.officium.verifyProfile.presentation.composables


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import java.util.Calendar
import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun NombreTextFiel (
    value: String,
    onTextChanged: (String) -> Unit
){
    OutlinedTextField(
        value = value,
        onValueChange = {onTextChanged(it)},
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = "Nombre") },
        placeholder = { Text(text = "Añade tu nombre") },
        maxLines = 1,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )
}

@Composable
fun ApellidoTextFiel (
    value: String,
    onTextChanged: (String) -> Unit
){
    OutlinedTextField(
        value = value,
        onValueChange = {onTextChanged(it)},
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = "Apellido(´s)") },
        placeholder = { Text(text = "Añade tu apellido") },
        maxLines = 1,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )
}

@Composable
fun DniTextFiel (
    value: String,
    onTextChanged: (String) -> Unit
){
    OutlinedTextField(
        value = value,
        onValueChange = {onTextChanged(it)},
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = "DNI") },
        placeholder = { Text(text = "Añade tu dni") },
        maxLines = 1,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )
}


@Composable
fun CiudadTextFiel (
    value: String,
    onTextChanged: (String) -> Unit
){
    OutlinedTextField(
        value = value,
        onValueChange = {onTextChanged(it)},
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = "Ciudad") },
        placeholder = { Text(text = "Añade tu ciudad") },
        maxLines = 1,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )
}

@Composable
fun DatePickerField(
    date:String,
    onDateSelected: (String) -> Unit
){

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            onDateSelected(selectedDate)
        },
        year, month, day
    )

    OutlinedTextField(
        value = date,
        onValueChange = {},
        readOnly = true,
        label = { Text(text = "Fecha de Nacimiento") },
        trailingIcon = {
            IconButton( onClick = ({datePickerDialog.show()})) {
                Icon(imageVector = Icons.Default.DateRange, contentDescription = "Seleccionar Fecha")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun GenderDropdown(
    selectedGender: String,
    onGenderSelected: (String) -> Unit
){
    var expanded by remember { mutableStateOf(false) }
    val genders = listOf("Hombre","Mujer","Indeterminado")

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedGender,
            onValueChange = {},
            readOnly = true,
            label = { Text(text = "Sexo") },
            trailingIcon = {
                IconButton( onClick = {expanded = true}) {
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Expandir")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false},
            modifier = Modifier.fillMaxWidth()
        ) {
            genders.forEach{ gender ->
                DropdownMenuItem(
                    text = { Text( gender) },
                    onClick = {
                        onGenderSelected(gender)
                        expanded = false
                    }
                )
            }
        }
    }
}

