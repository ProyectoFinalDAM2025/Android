package leo.rios.officium.search.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import leo.rios.officium.core.dataStore.DataStoreManager
import leo.rios.officium.empresaProfile.data.ProvinciaData
import leo.rios.officium.jobOffers.data.JobOfferDto
import leo.rios.officium.jobOffers.domain.JobOffersRepository
import leo.rios.officium.search.presentation.model.SearchFilters
import leo.rios.officium.subscriptions.data.CategoriaDto
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: JobOffersRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {
    private val _filters = MutableStateFlow(SearchFilters())
    val filters: StateFlow<SearchFilters> = _filters

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
        search(reset = true)
    }

    fun onFiltersChange(filters: SearchFilters) {
        _filters.value = filters
    }

    fun search(reset: Boolean = true) = viewModelScope.launch {
        if (_isLoading.value) return@launch
        if (reset) {
            page = 1
            hasMore = true
            _offers.value = emptyList()
        }
        if (!hasMore) return@launch

        _isLoading.value = true
        val currentFilters = _filters.value
        repository.searchOffers(
            title = currentFilters.title,
            location = currentFilters.location,
            category = currentFilters.category,
            status = currentFilters.status,
            page = page
        ).onSuccess { (items, canLoadMore) ->
            _offers.value = _offers.value + items
            hasMore = canLoadMore
            page++
        }.onFailure {
            _message.value = it.localizedMessage ?: "Error al buscar ofertas"
        }
        _isLoading.value = false
    }

    fun loadNextPage() {
        search(reset = false)
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

    fun reportOffer(offerId: Int, reason: String, description: String) = viewModelScope.launch {
        if (reason.isBlank()) {
            _message.value = "Indica un motivo"
            return@launch
        }
        repository.reportOffer(offerId, reason, description)
            .onSuccess { _message.value = "Reporte enviado" }
            .onFailure { _message.value = it.localizedMessage ?: "Error al reportar oferta" }
    }

    private fun loadSession() = viewModelScope.launch {
        _profilePhoto.value = dataStoreManager.getProfilePhoto().firstOrNull()
        _profileRole.value = dataStoreManager.getRole().firstOrNull()
        _currentProfileId.value = dataStoreManager.getIdProfile().firstOrNull()?.toIntOrNull()
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

    private fun refreshOffer(offerId: Int) = viewModelScope.launch {
        repository.getOffer(offerId)
            .onSuccess { updatedOffer ->
                _offers.value = _offers.value.map { offer ->
                    if (offer.idOferta == updatedOffer.idOferta) updatedOffer else offer
                }
            }
            .onFailure { _message.value = it.localizedMessage ?: "Error al actualizar oferta" }
    }
}
