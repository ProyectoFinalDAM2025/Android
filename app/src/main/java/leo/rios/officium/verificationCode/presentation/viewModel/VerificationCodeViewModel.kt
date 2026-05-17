package leo.rios.officium.verificationCode.presentation.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import leo.rios.officium.verificationCode.domain.VerificationCodeRepository
import leo.rios.officium.verificationCode.presentation.model.VerificationCodeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import leo.rios.officium.core.navigation.VerifyData
import leo.rios.officium.core.navigation.VerifyProfile
import leo.rios.officium.core.session.AuthState
import javax.inject.Inject

@HiltViewModel
class VerificationCodeViewModel @Inject constructor(
    private val repositoryVerificationCode: VerificationCodeRepository
) : ViewModel() {

    private val _code = MutableStateFlow<String>("")
    val code: StateFlow<String> = _code

    private val _authState = MutableStateFlow(AuthState.EMAIL_PENDING)
    val authState: StateFlow<AuthState> = _authState

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _verifyData = MutableStateFlow<VerifyData?>(null)
    val verifyData: StateFlow<VerifyData?> = _verifyData

    fun onCodeChange(code: String){
        _code.value = code
    }

    fun sendVerificationCode(email: String) = viewModelScope.launch {
        if (_code.value.isBlank()) {
            _message.value = "Introduce el código"
            return@launch
        }

        _isLoading.value = true

        try {
            val user = VerificationCodeModel(
                email = email,
                code = _code.value
            )

            val result = repositoryVerificationCode.sendVerificationCodeRepository(user)

            if (result.isSuccess) {
                val authUserResult = repositoryVerificationCode.getAuthenticatedUserRepository()

                if (authUserResult.isSuccess) {
                    val authUser = authUserResult.getOrNull()

                    if (authUser == null) {
                        _message.value = "No se pudo obtener el usuario autenticado"
                        return@launch
                    }

                    if (authUser.emailVerifiedAt == null) {
                        _message.value = "El email aún no aparece como verificado"
                        return@launch
                    }

                    _verifyData.value = VerifyData(
                        emailApp = authUser.email,
                        idUser = authUser.idUsuario.toString()
                    )

                    _message.value = null
                    _authState.value = AuthState.PROFILE_PENDING
                } else {
                    _message.value =
                        authUserResult.exceptionOrNull()?.localizedMessage
                            ?: "No se pudo obtener el usuario autenticado"
                }
            } else {
                _message.value =
                    result.exceptionOrNull()?.localizedMessage
                        ?: "Error al verificar el código"
            }
        } catch (e: Exception) {
            _message.value = e.localizedMessage ?: "Error de red"
        } finally {
            _isLoading.value = false
        }
    }
}
