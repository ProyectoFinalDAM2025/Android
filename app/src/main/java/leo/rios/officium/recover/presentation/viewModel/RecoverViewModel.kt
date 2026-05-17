package leo.rios.officium.recover.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import leo.rios.officium.recover.domain.RecoverRepository
import leo.rios.officium.recover.presentation.model.RecoverModel
import javax.inject.Inject

@HiltViewModel
class RecoverViewModel @Inject constructor(
    private val recoverRepository: RecoverRepository
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess

    fun onEmailChange(email: String) {
        _email.value = email
        _message.value = null
        _isSuccess.value = false
    }

    fun recoverPassword() = viewModelScope.launch {
        if (_email.value.isBlank()) {
            _message.value = "Introduce tu email"
            _isSuccess.value = false
            return@launch
        }

        _isLoading.value = true

        try {
            val result = recoverRepository.recoverPasswordRepository(
                RecoverModel(email = _email.value)
            )

            if (result.isSuccess) {
                _message.value = result.getOrNull()?.message
                    ?: "Respuesta vacia recibida del servidor"
                _isSuccess.value = true
            } else {
                _message.value = result.exceptionOrNull()?.localizedMessage ?: "Error desconocido"
                _isSuccess.value = false
            }
        } catch (e: Exception) {
            _message.value = e.localizedMessage ?: "Error de red"
            _isSuccess.value = false
        } finally {
            _isLoading.value = false
        }
    }
}
