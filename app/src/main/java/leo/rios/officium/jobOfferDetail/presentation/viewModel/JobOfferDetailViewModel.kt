package leo.rios.officium.jobOfferDetail.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import leo.rios.officium.core.dataStore.DataStoreManager
import leo.rios.officium.jobOffers.data.JobApplicationDto
import leo.rios.officium.jobOffers.data.JobOfferDto
import leo.rios.officium.jobOffers.domain.JobOffersRepository
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

    private val _currentProfileId = MutableStateFlow<Int?>(null)
    val currentProfileId: StateFlow<Int?> = _currentProfileId

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

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
}
