package leo.rios.officium.login.presentation.views

import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import leo.rios.officium.R
import leo.rios.officium.core.navigation.Home
import leo.rios.officium.core.navigation.Login
import leo.rios.officium.core.navigation.Recover
import leo.rios.officium.core.navigation.Register
import leo.rios.officium.core.session.AuthState
import leo.rios.officium.login.presentation.viewModel.LoginViewModel

@Composable
fun LoginScreen(navigationTo: NavController, viewModel: LoginViewModel) {
    val email: String by viewModel.email.collectAsState()
    val password: String by viewModel.password.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val message by viewModel.message.collectAsState()
    val context = LocalContext.current
    var passwordVisibility by remember { mutableStateOf(false) }

//    LaunchedEffect(message) {
//        message?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
//    }

    LaunchedEffect(authState) {
        if (authState == AuthState.AUTHENTICATED) {
            Toast.makeText(context, "Sesion iniciada", Toast.LENGTH_SHORT).show()
            navigationTo.navigate(Home) {
                popUpTo<Login> { inclusive = true }
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
            text = "Espanol (ES)",
            color = Color(0xFF46525E),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 28.dp)
        )

        Spacer(modifier = Modifier.weight(0.8f))

        Image(
            painter = painterResource(id = R.drawable.login),
            contentDescription = "Officium",
            modifier = Modifier.size(400.dp)
        )

        Spacer(modifier = Modifier.weight(0.9f))

        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { newEmail ->
                    viewModel.onLoginChange(newEmail, password)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Email") },
                placeholder = { Text(text = "Correo electronico") },
                maxLines = 1,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { newPassword ->
                    viewModel.onLoginChange(email, newPassword)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Contraseña") },
                placeholder = { Text(text = "Contraseña") },
                maxLines = 1,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (passwordVisibility) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    val image = if (passwordVisibility) {
                        Icons.Filled.VisibilityOff
                    } else {
                        Icons.Filled.Visibility
                    }
                    IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                        Icon(
                            imageVector = image,
                            contentDescription = stringResource(id = R.string.show_password),
                            tint = Color.Black
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.loginUser() },
                enabled = email.isNotBlank() && password.isNotBlank(),
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
                    text = "Iniciar session",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            TextButton(onClick = { navigationTo.navigate(Recover) }) {
                Text(
                    text = "Olvidaste tu contraseña?",
                    color = Color(0xFF25313B),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            message?.let { loginMessage ->
                Text(
                    text = loginMessage,
                    color = Color(0xFFD32F2F),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1.4f))

        OutlinedButton(
            onClick = { navigationTo.navigate(Register) },
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
        ) {
            Text(
                text = "Crear nueva cuenta",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }

        Text(
            text = "IES PERE MARIA",
            color = Color(0xFF46525E),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 12.dp, bottom = 14.dp)
        )
    }
}
