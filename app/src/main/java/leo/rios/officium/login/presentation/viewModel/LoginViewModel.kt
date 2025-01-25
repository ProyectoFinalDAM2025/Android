package leo.rios.officium.login.presentation.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import leo.rios.officium.core.api.ApiService
import leo.rios.officium.core.navigation.Home
import leo.rios.officium.login.domain.LogInRepository
import leo.rios.officium.login.presentation.model.LogInModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repositoryLogin: LogInRepository
): ViewModel() {

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


    fun loginUser(navController: NavController) = viewModelScope.launch{

        try{
            // Crear el modelo con los valores actuales de email y password
           val user = LogInModel(_email.value, _password.value)
            Log.d("Login", "Iniciando sesión con: Email=${user.email}, Password=${user.password}")

            // Llamar al repositorio
            val result = repositoryLogin.loginUser(user)
            if (result.isSuccess){
                val loginResponse = result.getOrNull()
                if(loginResponse != null){
                    Log.d("Login Exitoso", "Datos del usuario: ${loginResponse.data}")
                    navController.navigate(Home)
                }else{
                    Log.e("Login Error", "El servidor devolvió una respuesta exitosa, pero el cuerpo está vacío")
                }
            }else{
                val errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Error desconocido"
                Log.e("Login Error", "Error durante el login: $errorMessage")
            }
        }catch (e: Exception){
            Log.e("Login Error", "Error inesperado: ${e.message}", e)
        }
    }

}