package leo.rios.officium.registro.presentation.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import leo.rios.officium.core.dataStore.DataStoreManager
import leo.rios.officium.core.navigation.VerificationCode
import leo.rios.officium.core.navigation.VerificationData
import leo.rios.officium.core.session.AuthState
import leo.rios.officium.registro.domain.RegisterRepository
import leo.rios.officium.registro.presentation.model.RegisterModel
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor (
    private val dataStoreManager: DataStoreManager,
    private val registerRepository: RegisterRepository
) : ViewModel() {

    private val _email = MutableStateFlow<String>("")
    val email : StateFlow<String> = _email

    private val _password = MutableStateFlow<String>("")
    val password : StateFlow<String> = _password

    private val _authState = MutableStateFlow(AuthState.LOGGED_OUT)
    val authState: StateFlow<AuthState> = _authState

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading


    fun onRegisterChange(email: String, password: String){
        _email.value = email
        _password.value = password
    }

    fun registerUser() = viewModelScope.launch {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _message.value = "Introduce email y contraseña"
            return@launch
        }

        _isLoading.value = true

        try {
            val user = RegisterModel(
                email = _email.value,
                password = _password.value
            )

            val result = registerRepository.registerUserRepository(user)

            if (result.isSuccess) {
                val response = result.getOrNull()

                if (response?.data?.token != null) {
                    _authState.value = AuthState.EMAIL_PENDING
                    _message.value = null
                } else {
                    _message.value = "Respuesta de registro vacía"
                }
            } else {
                _message.value =
                    result.exceptionOrNull()?.localizedMessage ?: "Error desconocido"
            }
        } catch (e: Exception) {
            _message.value = e.localizedMessage ?: "Error de red"
        } finally {
            _isLoading.value = false
        }
    }

}
