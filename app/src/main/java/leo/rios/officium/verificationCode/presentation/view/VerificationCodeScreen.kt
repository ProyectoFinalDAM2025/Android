package leo.rios.officium.verificationCode.presentation.view

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import leo.rios.officium.core.navigation.VerificationData
import leo.rios.officium.core.navigation.VerifyProfile
import leo.rios.officium.core.session.AuthState
import leo.rios.officium.verificationCode.presentation.composables.VerificationTextFiel
import leo.rios.officium.verificationCode.presentation.viewModel.VerificationCodeViewModel

@Composable
fun VerificationCodeScreen(
    verificationData: VerificationData,
    viewModel: VerificationCodeViewModel,
    navigateTo: NavController
) {
    val code: String by viewModel.code.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val verifyData by viewModel.verifyData.collectAsState()
    val message by viewModel.message.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(authState, verifyData) {
        val data = verifyData

        if (authState == AuthState.PROFILE_PENDING && data != null) {
            navigateTo.navigate(VerifyProfile(data)) {
                launchSingleTop = true
            }
        }
    }

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
            text = "English (US)",
            color = Color(0xFF46525E),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 28.dp)
        )

        Spacer(modifier = Modifier.weight(0.8f))

        Image(
            painter = painterResource(id = R.drawable.code),
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
                text = "Se ha enviado un codigo de verificacion a... ",
                fontSize = 18.sp,
                softWrap = true
            )
            Text(text = "Email: ${verificationData.email}")

            Spacer(modifier = Modifier.height(12.dp))

            VerificationTextFiel(code) { newCode ->
                viewModel.onCodeChange(newCode)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                enabled = !isLoading,
                onClick = {
                    viewModel.sendVerificationCode(verificationData.email)
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
                Text(
                    text = if (isLoading) "Verificando..." else "Confirmar",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            message?.let { mensaje ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = mensaje,
                    color = Color(0xFFD32F2F)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1.2f))
    }
}
