package leo.rios.officium.login.presentation.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import leo.rios.officium.core.dataStore.DataStoreManager
import leo.rios.officium.core.navigation.Home
import leo.rios.officium.core.navigation.Login
import leo.rios.officium.login.data.getProfilePhoto
import leo.rios.officium.login.domain.LogInRepository
import leo.rios.officium.login.presentation.model.LogInModel
import javax.inject.Inject
import leo.rios.officium.core.session.AuthState

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repositoryLogin: LogInRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    private val _profilePhoto = MutableStateFlow<String?>(null)
    val profilePhoto: StateFlow<String?> = _profilePhoto

    private val _authState = MutableStateFlow(AuthState.LOGGED_OUT)
    val authState: StateFlow<AuthState> = _authState

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _isCheckingToken = MutableStateFlow(true)
    val isCheckingToken: StateFlow<Boolean> = _isCheckingToken

    private val _isLoginView = MutableStateFlow(true)
    val isLoginView: StateFlow<Boolean> = _isLoginView

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun setUserId(id: String) {
        _userId.value = id
    }

    fun onLoginViewChange(newIsLoginView: Boolean) {
        _isLoginView.value = newIsLoginView
    }

    fun onLoginChange(email: String, password: String) {
        _email.value = email
        _password.value = password
    }

    fun loginUser() = viewModelScope.launch {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _message.value = "Introduce email y contraseña"
            return@launch
        }

        _isLoading.value = true

        try {
            val user = LogInModel(_email.value, _password.value)

            val result = repositoryLogin.loginUserRepository(user)

            if (result.isSuccess) {
                val authData = result.getOrNull()?.data

                if (authData != null) {
                    _token.value = authData.token
                    _profilePhoto.value = authData.profile.getProfilePhoto()
                        ?: dataStoreManager.getProfilePhoto().firstOrNull()
                    _authState.value = AuthState.AUTHENTICATED
                    _message.value = null
                } else {
                    _message.value = "Respuesta de login vacía"
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

    fun checkAuthStatus() {
        viewModelScope.launch {
            _isCheckingToken.value = true

            val tokenStored = dataStoreManager.getAccessToken().firstOrNull()
            val roleStored = dataStoreManager.getRole().firstOrNull()
            val idProfileStored = dataStoreManager.getIdProfile().firstOrNull()
            val profilePhotoStored = dataStoreManager.getProfilePhoto().firstOrNull()

            _token.value = tokenStored
            _userId.value = idProfileStored
            _profilePhoto.value = profilePhotoStored

            _authState.value = when {
                tokenStored.isNullOrEmpty() -> AuthState.LOGGED_OUT
                idProfileStored.isNullOrEmpty() -> AuthState.PROFILE_PENDING
                !roleStored.isNullOrEmpty() -> AuthState.AUTHENTICATED
                else -> AuthState.PROFILE_PENDING
            }

            _isCheckingToken.value = false
        }
    }

    fun logout(onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repositoryLogin.logoutUserRepository()

            result.exceptionOrNull()?.localizedMessage?.let { logoutMessage ->
                _message.value = logoutMessage
            }

            dataStoreManager.deleteStore()
            _token.value = null
            _userId.value = null
            _profilePhoto.value = null
            _authState.value = AuthState.LOGGED_OUT
            if (result.isSuccess) {
                _message.value = null
            }
            onFinished()
        }
    }
}
