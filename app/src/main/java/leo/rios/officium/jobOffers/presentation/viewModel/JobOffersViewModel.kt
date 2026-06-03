package leo.rios.officium.jobOffers.presentation.viewModel

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
class JobOffersViewModel @Inject constructor(
    private val repository: JobOffersRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {
    private val _offers = MutableStateFlow<List<JobOfferDto>>(emptyList())
    val offers: StateFlow<List<JobOfferDto>> = _offers

    private val _categories = MutableStateFlow<List<CategoriaDto>>(emptyList())
    val categories: StateFlow<List<CategoriaDto>> = _categories

    private val _provincias = MutableStateFlow<List<ProvinciaData>>(emptyList())
    val provincias: StateFlow<List<ProvinciaData>> = _provincias

    private val _profilePhoto = MutableStateFlow<String?>(null)
    val profilePhoto: StateFlow<String?> = _profilePhoto

    private val _profileRole = MutableStateFlow<String?>(null)
    val profileRole: StateFlow<String?> = _profileRole

    private val _currentProfileId = MutableStateFlow<Int?>(null)
    val currentProfileId: StateFlow<Int?> = _currentProfileId

    private val _applicationsByOffer = MutableStateFlow<Map<Int, List<JobApplicationDto>>>(emptyMap())
    val applicationsByOffer: StateFlow<Map<Int, List<JobApplicationDto>>> = _applicationsByOffer

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private var page = 1
    private var hasMore = true

    init {
        loadSession()
        loadCategories()
        loadProvincias()
        refreshOffers()
    }

    fun refreshOffers() {
        page = 1
        hasMore = true
        _offers.value = emptyList()
        loadNextPage()
    }

    fun loadNextPage() = viewModelScope.launch {
        if (_isLoading.value || !hasMore) return@launch
        _isLoading.value = true
        repository.getMyOffers(page)
            .onSuccess { (items, canLoadMore) ->
                _offers.value = _offers.value + items
                hasMore = canLoadMore
                page++
            }
            .onFailure { _message.value = it.localizedMessage ?: "Error al cargar ofertas" }
        _isLoading.value = false
    }

    fun createOffer(form: JobOfferFormState) = viewModelScope.launch {
        val categoryId = form.categoryId
        if (form.title.isBlank() || form.description.isBlank() || form.location.isBlank() || categoryId == null) {
            _message.value = "Completa todos los campos"
            return@launch
        }

        _isLoading.value = true
        repository.createOffer(
            categoryId = categoryId,
            title = form.title,
            description = form.description,
            location = form.location,
            status = form.status
        ).onSuccess { createdOffer ->
            _message.value = "Oferta creada"
            _offers.value = listOf(createdOffer) + _offers.value.filterNot { it.idOferta == createdOffer.idOferta }
        }.onFailure {
            _message.value = it.localizedMessage ?: "Error al crear oferta"
        }
        _isLoading.value = false
    }

    fun updateOffer(offerId: Int, form: JobOfferFormState) = viewModelScope.launch {
        val categoryId = form.categoryId
        if (form.title.isBlank() || form.description.isBlank() || form.location.isBlank() || categoryId == null) {
            _message.value = "Completa todos los campos"
            return@launch
        }

        _isLoading.value = true
        repository.updateOffer(
            offerId = offerId,
            categoryId = categoryId,
            title = form.title,
            description = form.description,
            location = form.location,
            status = form.status
        ).onSuccess { updatedOffer ->
            _message.value = "Oferta actualizada"
            _offers.value = _offers.value.map { currentOffer ->
                if (currentOffer.idOferta == updatedOffer.idOferta) {
                    updatedOffer.copy(
                        empresa = updatedOffer.empresa ?: currentOffer.empresa,
                        categoria = updatedOffer.categoria ?: currentOffer.categoria
                    )
                } else {
                    currentOffer
                }
            }
        }.onFailure {
            _message.value = it.localizedMessage ?: "Error al actualizar oferta"
        }
        _isLoading.value = false
    }

    fun deleteOffer(offerId: Int) = viewModelScope.launch {
        repository.deleteOffer(offerId)
            .onSuccess {
                _message.value = "Oferta eliminada"
                _offers.value = _offers.value.filterNot { it.idOferta == offerId }
                _applicationsByOffer.value = _applicationsByOffer.value - offerId
            }
            .onFailure { _message.value = it.localizedMessage ?: "Error al eliminar oferta" }
    }

    fun applyToOffer(offerId: Int) = viewModelScope.launch {
        repository.applyToOffer(offerId)
            .onSuccess {
                _message.value = "Aplicacion enviada"
                refreshOffer(offerId)
            }
            .onFailure { _message.value = it.localizedMessage ?: "Error al aplicar" }
    }

    fun deleteApplication(offerId: Int, applicationId: Int) = viewModelScope.launch {
        repository.deleteApplication(applicationId)
            .onSuccess {
                _message.value = "Aplicacion eliminada"
                refreshOffer(offerId)
            }
            .onFailure { _message.value = it.localizedMessage ?: "Error al eliminar aplicacion" }
    }

    fun loadApplications(offerId: Int) = viewModelScope.launch {
        repository.getApplications(offerId)
            .onSuccess { applications ->
                _applicationsByOffer.value = _applicationsByOffer.value + (offerId to applications)
            }
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

    private fun loadSession() = viewModelScope.launch {
        _profilePhoto.value = dataStoreManager.getProfilePhoto().firstOrNull()
        _profileRole.value = dataStoreManager.getRole().firstOrNull()
        _currentProfileId.value = dataStoreManager.getIdProfile().firstOrNull()?.toIntOrNull()
    }

    private fun refreshOffer(offerId: Int) = viewModelScope.launch {
        repository.getOffer(offerId)
            .onSuccess { updatedOffer ->
                _offers.value = _offers.value.map { currentOffer ->
                    if (currentOffer.idOferta == updatedOffer.idOferta) updatedOffer else currentOffer
                }
            }
            .onFailure { _message.value = it.localizedMessage ?: "Error al actualizar oferta" }
    }
}
