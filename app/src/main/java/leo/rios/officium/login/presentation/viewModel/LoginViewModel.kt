package leo.rios.officium.login.presentation.viewModel

import android.util.Log
import androidx.datastore.dataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import leo.rios.officium.core.api.ApiService
import leo.rios.officium.core.dataStore.DataStoreManager
import leo.rios.officium.core.navigation.Home
import leo.rios.officium.login.domain.LogInRepository
import leo.rios.officium.login.presentation.model.LogInModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repositoryLogin: LogInRepository,
    private val dataStoreManager: DataStoreManager
): ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    private val _authState = MutableStateFlow<String?>(null)
    val authState: StateFlow<String?> = _authState

    private val _isCheckingToken = MutableStateFlow(true)
    val isCheckingToken: StateFlow<Boolean> = _isCheckingToken


    private val _isLoginView = MutableStateFlow<Boolean>(true)
    val isLoginView: StateFlow<Boolean> = _isLoginView

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {

        viewModelScope.launch {
            // Cargar el token desde DataStore al iniciar la aplicación
            Log.d("Init VM_Login","Se inicio viewModel")
            dataStoreManager.getAccessToken().collect(){ tokenAlmacenado ->
                _token.value = tokenAlmacenado // Actualizar el token en memoria
                if(!tokenAlmacenado.isNullOrEmpty()){
                    Log.d("Init VM_Login","Token valido encontrado")
                    _authState.value = "Token valido encontrado"
                }else{
                    Log.d("Init VM_Login","No hay token")
                    _authState.value = "No hay token"
                }
            }
        }
    }

    fun onLoginViewChange(newIsLoginView: Boolean) {
        _isLoginView.value = newIsLoginView
    }
    fun onLoginChange(email: String, password: String){
        _email.value = email
        _password.value = password
    }


    fun loginUser() = viewModelScope.launch{
        try{
            // Crear el modelo con los valores actuales de email y password
           val user = LogInModel(_email.value, _password.value)
            Log.d("Login", "Iniciando sesión con: Email=${user.email}, Password=${user.password}")
            // Llamar al repositorio
            val result = repositoryLogin.loginUserRepository(user)
            if (result.isSuccess){
                val loginResponse = result.getOrNull()
                if(loginResponse != null){
                    Log.d("Login Exitoso", "Datos del usuario: ${loginResponse.data}")
                    dataStoreManager.guardarTokens(
                        loginResponse.data.token,
                        loginResponse.data.appToken,
                        loginResponse.data.user.rol
                    )
                    _token.value = loginResponse.data.token
                    _authState.value = "Login exitoso"

                }else{
                    _authState.value = result.exceptionOrNull()?.localizedMessage ?: "Error desconocido"
                    Log.e("Login Error", "El servidor devolvió una respuesta exitosa, pero el cuerpo está vacío.  ${result.exceptionOrNull()?.localizedMessage}")
                }
            }else{
                val errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Error desconocido"
                Log.e("Login Error", "Error durante el login: $errorMessage")
                _authState.value = result.exceptionOrNull()?.localizedMessage ?: "Error desconocido"
            }
        }catch (e: Exception){
            _authState.value = e.localizedMessage ?: "Error de red"
            Log.e("Login Error", "Error inesperado: ${e.message}", e)
        }
    }


    fun checkAuthStatus(){
        viewModelScope.launch {
            _isCheckingToken.value = true

            dataStoreManager.getAccessToken().collect{ tokenAlmacenado ->
                Log.d("AuthStatus", "Access Token: $tokenAlmacenado")
                if(!tokenAlmacenado.isNullOrEmpty()){
                    Log.d("CheckAuthStatus","Token valido encontrado")
                    _token.value = tokenAlmacenado
                    _authState.value = "Token valido encontrado"
                }else{
                    Log.d("CheckAuthStatus","No hay token valido")
                    _token.value = null
                    _authState.value = "No hay token valido"
                }
                _isCheckingToken.value = false

            }
        }
    }
}