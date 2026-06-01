package leo.rios.officium.jobOfferDetail.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import leo.rios.officium.core.dataStore.DataStoreManager
import leo.rios.officium.empresaProfile.data.ProvinciaData
import leo.rios.officium.jobOffers.data.JobApplicationDto
import leo.rios.officium.jobOffers.data.JobOfferDto
import leo.rios.officium.jobOffers.domain.JobOffersRepository
import leo.rios.officium.jobOffers.presentation.model.JobOfferFormState
import leo.rios.officium.subscriptions.data.CategoriaDto
import javax.inject.Inject

@HiltViewModel
class JobOfferDetailViewModel @Inject constructor(
    private val repository: JobOffersRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {
    private val _offer = MutableStateFlow<JobOfferDto?>(null)
    val offer: StateFlow<JobOfferDto?> = _offer

    private val _applications = MutableStateFlow<List<JobApplicationDto>?>(null)
    val applications: StateFlow<List<JobApplicationDto>?> = _applications

    private val _categories = MutableStateFlow<List<CategoriaDto>>(emptyList())
    val categories: StateFlow<List<CategoriaDto>> = _categories

    private val _provincias = MutableStateFlow<List<ProvinciaData>>(emptyList())
    val provincias: StateFlow<List<ProvinciaData>> = _provincias

    private val _currentProfileId = MutableStateFlow<Int?>(null)
    val currentProfileId: StateFlow<Int?> = _currentProfileId

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    init {
        loadCategories()
        loadProvincias()
    }

    fun load(offerId: Int) = viewModelScope.launch {
        _currentProfileId.value = dataStoreManager.getIdProfile().firstOrNull()?.toIntOrNull()
        repository.getOffer(offerId)
            .onSuccess { _offer.value = it }
            .onFailure { _message.value = it.localizedMessage ?: "No se pudo cargar la oferta" }
    }

    fun applyToOffer(offerId: Int) = viewModelScope.launch {
        repository.applyToOffer(offerId)
            .onSuccess {
                _message.value = "Aplicacion enviada"
                load(offerId)
            }
            .onFailure { _message.value = it.localizedMessage ?: "Error al aplicar" }
    }

    fun deleteApplication(offerId: Int, applicationId: Int) = viewModelScope.launch {
        repository.deleteApplication(applicationId)
            .onSuccess {
                _message.value = "Aplicacion eliminada"
                load(offerId)
            }
            .onFailure { _message.value = it.localizedMessage ?: "Error al eliminar aplicacion" }
    }

    fun loadApplications(offerId: Int) = viewModelScope.launch {
        repository.getApplications(offerId)
            .onSuccess { _applications.value = it }
            .onFailure { _message.value = it.localizedMessage ?: "Error al cargar aplicaciones" }
    }

    fun updateApplicationStatus(offerId: Int, applicationId: Int, status: String) = viewModelScope.launch {
        repository.updateApplicationStatus(applicationId, status)
            .onSuccess {
                _message.value = "Estado actualizado"
                loadApplications(offerId)
            }
            .onFailure { _message.value = it.localizedMessage ?: "Error al actualizar aplicacion" }
    }

    fun updateOffer(offerId: Int, form: JobOfferFormState) = viewModelScope.launch {
        val categoryId = form.categoryId
        if (form.title.isBlank() || form.description.isBlank() || form.location.isBlank() || categoryId == null) {
            _message.value = "Completa todos los campos"
            return@launch
        }

        repository.updateOffer(
            offerId = offerId,
            categoryId = categoryId,
            title = form.title,
            description = form.description,
            location = form.location,
            status = form.status
        ).onSuccess { updatedOffer ->
            _message.value = "Oferta actualizada"
            _offer.value = updatedOffer.copy(
                empresa = updatedOffer.empresa ?: _offer.value?.empresa,
                categoria = updatedOffer.categoria ?: _offer.value?.categoria
            )
        }.onFailure {
            _message.value = it.localizedMessage ?: "Error al actualizar oferta"
        }
    }

    fun reportOffer(offerId: Int, reason: String, description: String) = viewModelScope.launch {
        if (reason.isBlank()) {
            _message.value = "Indica un motivo"
            return@launch
        }
        repository.reportOffer(offerId, reason, description)
            .onSuccess { _message.value = "Reporte enviado" }
            .onFailure { _message.value = it.localizedMessage ?: "Error al reportar oferta" }
    }

    private fun loadCategories() = viewModelScope.launch {
        repository.getCategories()
            .onSuccess { _categories.value = it }
            .onFailure { _message.value = it.localizedMessage ?: "Error al cargar categorias" }
    }

    private fun loadProvincias() = viewModelScope.launch {
        repository.getProvincias()
            .onSuccess { _provincias.value = it }
            .onFailure { _message.value = it.localizedMessage ?: "Error al cargar provincias" }
    }
}
