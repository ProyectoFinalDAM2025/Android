package leo.rios.officium.recover.presentation.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import leo.rios.officium.R
import leo.rios.officium.core.navigation.Login
import leo.rios.officium.recover.presentation.composables.RecoverEmailTextFiel
import leo.rios.officium.recover.presentation.viewModel.RecoverViewModel

@Composable
fun RecoverScreen(navigateTo: NavController, viewModel: RecoverViewModel) {
    val email: String by viewModel.email.collectAsState()
    val message by viewModel.message.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF7F7F7),
                        Color(0xFFEDEDED)
                    )
                )
            )
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Español (ES)",
            color = Color(0xFF46525E),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 28.dp)
        )

        Spacer(modifier = Modifier.weight(0.8f))

        Image(
            painter = painterResource(id = R.drawable.recover),
            contentDescription = "Officium",
            modifier = Modifier.size(400.dp)
        )

        Spacer(modifier = Modifier.weight(0.8f))

        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Recordar contraseña",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            RecoverEmailTextFiel(email) { newEmail ->
                viewModel.onEmailChange(newEmail)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                enabled = !isLoading && email.isNotBlank(),
                onClick = { viewModel.recoverPassword() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    disabledContainerColor = Color(0xFF9E9E9E),
                    contentColor = Color.White,
                    disabledContentColor = Color.White
                )
            ) {
                Text(
                    text = if (isLoading) "Enviando..." else "Enviar",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            TextButton(onClick = { navigateTo.navigate(Login) }) {
                Text(
                    text = "Volver al login",
                    color = Color(0xFF25313B),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            message?.let { recoverMessage ->
                Text(
                    text = recoverMessage,
                    color = if (isSuccess) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1.4f))

        Text(
            text = "IES PERE MARIA",
            color = Color(0xFF46525E),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 12.dp, bottom = 14.dp)
        )
    }
}
