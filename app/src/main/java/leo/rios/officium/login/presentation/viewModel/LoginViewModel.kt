package leo.rios.officium.login.presentation.viewModel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import leo.rios.officium.core.navigation.Home

class LoginViewModel(): ViewModel() {
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _isLoginView = MutableStateFlow<Boolean>(true)
    val isLoginView: StateFlow<Boolean> = _isLoginView

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        // Inicializar el estado de la vista de inicio de sesión
        _isLoginView.value = true // Por defecto, la vista de inicio de sesión está activa
    }

    fun onLoginViewChange(newIsLoginView: Boolean ){
        _isLoginView.value = newIsLoginView
    }
    fun onLoginChange(email: String, password: String){
        _email.value = email
        _password.value = password
    }


    fun DoLogin(navController: NavController){


        Log.wtf ( "Login","El email es: ${_email.value} y el password ${_password.value}")
        navController.navigate(Home)
    }

}