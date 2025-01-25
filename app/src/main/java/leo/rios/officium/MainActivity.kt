package leo.rios.officium

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import leo.rios.officium.core.navigation.NavigationApp
import leo.rios.officium.ui.theme.OFFICIUMTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OFFICIUMTheme {
                NavigationApp()
            }
        }
    }
}

