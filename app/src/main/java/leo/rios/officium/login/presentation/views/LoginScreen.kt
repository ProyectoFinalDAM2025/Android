package leo.rios.officium.login.presentation.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text


import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import leo.rios.officium.login.presentation.composables.EmailTextFiel
import leo.rios.officium.login.presentation.composables.PasswordTextField

import leo.rios.officium.login.presentation.viewModel.LoginViewModel

@Composable
fun LoginScreen(navigationToHome : NavController, viewModel: LoginViewModel){


    val email: String by viewModel.email.collectAsState()
    val password:  String by viewModel.password.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment =  Alignment.CenterHorizontally
    ){
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Login",
            fontSize = 25.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Email",
            fontSize = 20.sp
        )
        EmailTextFiel(email) { newEmail ->
            viewModel.onLoginChange(newEmail, password)
        }


        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Password",
            fontSize = 20.sp
        )
        PasswordTextField (password) { newPassword ->
            viewModel.onLoginChange(email,newPassword)

        }

//        OutlinedTextField(
//           value = password,
//            onValueChange = {password = it},
//            placeholder = {Text(text = "Password")},
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
//            singleLine = true,
//           maxLines = 1,
//            )

        Spacer(modifier = Modifier.weight(1f))
        Button(

            onClick = {  viewModel.DoLogin(navigationToHome) }
        ) {
            Text(
                text = "Navegar a home",
                fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}