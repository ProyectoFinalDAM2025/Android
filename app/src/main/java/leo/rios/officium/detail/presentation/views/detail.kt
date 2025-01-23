package leo.rios.officium.detail.presentation.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import leo.rios.officium.core.navigation.SettingsInfo

@Composable
fun DetailScreen(
    name:String,
    navigateBack: () -> Unit,
    navigateToSettings: (SettingsInfo) -> Unit
){

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Detail Screen $name",
            fontSize = 25.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { navigateBack() }
        ) {
            Text(
                text = "Navegar a Login",
                fontSize = 20.sp
            )
        }
        Button(
            onClick = {
                val settingsInfo = SettingsInfo(
                    name = "Leo",
                    id = 28,
                    darkMode = true
                )
                navigateToSettings(settingsInfo)
            }
        ) {
            Text(
                text = "Navegar a Ajustes",
                fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}