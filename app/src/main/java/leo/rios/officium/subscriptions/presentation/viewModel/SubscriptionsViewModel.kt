package leo.rios.officium.subscriptions.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import leo.rios.officium.core.dataStore.DataStoreManager
import leo.rios.officium.jobOffers.data.JobOfferDto
import leo.rios.officium.subscriptions.data.CategoriaDto
import leo.rios.officium.subscriptions.domain.SubscriptionsRepository
import javax.inject.Inject

@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val repository: SubscriptionsRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {
    private val _availableCategories = MutableStateFlow<List<CategoriaDto>>(emptyList())
    val availableCategories: StateFlow<List<CategoriaDto>> = _availableCategories

    private val _subscribedCategories = MutableStateFlow<List<CategoriaDto>>(emptyList())
    val subscribedCategories: StateFlow<List<CategoriaDto>> = _subscribedCategories

    private val _myApplications = MutableStateFlow<List<JobOfferDto>>(emptyList())
    val myApplications: StateFlow<List<JobOfferDto>> = _myApplications

    private val _currentProfileId = MutableStateFlow<Int?>(null)
    val currentProfileId: StateFlow<Int?> = _currentProfileId

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    init {
        loadSession()
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        _isLoading.value = true
        repository.getAvailableCategories()
            .onSuccess { _availableCategories.value = it }
            .onFailure { _message.value = it.localizedMessage ?: "Error al cargar categorias" }

        repository.getSubscribedCategories()
            .onSuccess { _subscribedCategories.value = it }
            .onFailure { _message.value = it.localizedMessage ?: "Error al cargar suscripciones" }

        repository.getMyApplications()
            .onSuccess { _myApplications.value = it }
            .onFailure { _message.value = it.localizedMessage ?: "Error al cargar aplicaciones" }
        _isLoading.value = false
    }

    fun subscribe(categoryId: Int) = viewModelScope.launch {
        repository.subscribe(categoryId)
            .onSuccess {
                _message.value = "Suscripcion creada"
                refresh()
            }
            .onFailure { _message.value = it.localizedMessage ?: "Error al suscribirse" }
    }

    fun unsubscribe(categoryId: Int) = viewModelScope.launch {
        repository.unsubscribe(categoryId)
            .onSuccess {
                _message.value = "Suscripcion eliminada"
                refresh()
            }
            .onFailure { _message.value = it.localizedMessage ?: "Error al eliminar suscripcion" }
    }

    fun deleteApplication(offerId: Int, applicationId: Int) = viewModelScope.launch {
        repository.deleteApplication(applicationId)
            .onSuccess {
                _message.value = "Aplicacion eliminada"
                _myApplications.value = _myApplications.value.filterNot { it.idOferta == offerId }
            }
            .onFailure { _message.value = it.localizedMessage ?: "Error al eliminar aplicacion" }
    }

    private fun loadSession() = viewModelScope.launch {
        _currentProfileId.value = dataStoreManager.getIdProfile().firstOrNull()?.toIntOrNull()
    }
}
