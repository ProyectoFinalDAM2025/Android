package leo.rios.officium.empresaProfile.presentation.viewModel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import leo.rios.officium.core.dataStore.DataStoreManager

import leo.rios.officium.core.session.AuthState
import leo.rios.officium.empresaProfile.data.ProvinciaData
import leo.rios.officium.empresaProfile.data.SectorData
import leo.rios.officium.empresaProfile.domain.EmpresaProfileRepository
import leo.rios.officium.empresaProfile.presentation.model.EmpresaProfileModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class EmpresaProfileViewModel @Inject constructor(
    private val repository: EmpresaProfileRepository,
    private val application: Application,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState.PROFILE_PENDING)
    val authState: StateFlow<AuthState> = _authState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _nombreEmpresa = MutableStateFlow("")
    val nombreEmpresa: StateFlow<String> = _nombreEmpresa

    private val _cif = MutableStateFlow("")
    val cif: StateFlow<String> = _cif

    private val _idSector = MutableStateFlow("")
    val idSector: StateFlow<String> = _idSector

    private val _ubicacion = MutableStateFlow("")
    val ubicacion: StateFlow<String> = _ubicacion

    private val _sitioWeb = MutableStateFlow("")
    val sitioWeb: StateFlow<String> = _sitioWeb

    private val _foto = MutableStateFlow<Uri?>(null)
    val foto: StateFlow<Uri?> = _foto

    private val _state = MutableStateFlow<String?>(null)
    val state: StateFlow<String?> = _state

    private val _sectors = MutableStateFlow<List<SectorData>>(emptyList())
    val sectors: StateFlow<List<SectorData>> = _sectors

    private val _provincias = MutableStateFlow<List<ProvinciaData>>(emptyList())
    val provincias: StateFlow<List<ProvinciaData>> = _provincias

    init {
        loadCatalogs()
    }

    fun onNombreEmpresaChange(value: String) { _nombreEmpresa.value = value }
    fun onCifChange(value: String) { _cif.value = value }
    fun onIdSectorChange(value: String) { _idSector.value = value }
    fun onUbicacionChange(value: String) { _ubicacion.value = value }
    fun onSitioWebChange(value: String) { _sitioWeb.value = value }
    fun onFotoSelected(uri: Uri?) { _foto.value = uri }

    private fun loadCatalogs() = viewModelScope.launch {
        repository.getSectors()
            .onSuccess { _sectors.value = it }
            .onFailure { _state.value = it.localizedMessage ?: "Error al obtener sectores" }

        repository.getProvincias()
            .onSuccess { _provincias.value = it }
            .onFailure { _state.value = it.localizedMessage ?: "Error al obtener provincias" }
    }

    fun sendEmpresaProfile(idUsuario: String) = viewModelScope.launch {
        if (_nombreEmpresa.value.isBlank() || _cif.value.isBlank() || _idSector.value.isBlank() || _ubicacion.value.isBlank()) {
            _state.value = "Completa los campos obligatorios"
            return@launch
        }

        withContext(Dispatchers.IO) {
            val fotoPart = _foto.value?.let { uriToImagePart(it) }
            val profile = EmpresaProfileModel(
                idUsuario = idUsuario,
                nombreEmpresa = _nombreEmpresa.value,
                cif = _cif.value,
                idSector = _idSector.value,
                ubicacion = _ubicacion.value,
                sitioWeb = _sitioWeb.value
            )
            repository.sendEmpresaProfileRepository(profile, fotoPart)
                .onSuccess {
                    _state.value = "Perfil de empresa creado"
                    withContext(Dispatchers.Main) {
                        dataStoreManager.saveRole("empresa")
                        dataStoreManager.saveIdProfile(idUsuario)

                        _state.value = "Perfil de empresa creado"
                        _authState.value = AuthState.AUTHENTICATED
                    }
                }
                .onFailure {
                    Log.e("EmpresaProfile", "Error: ${it.message}", it)
                    _state.value = it.localizedMessage ?: "Error al crear perfil"
                }
        }
    }

    private fun uriToImagePart(uri: Uri): MultipartBody.Part? {
        val file = File(application.cacheDir, "empresa_${System.currentTimeMillis()}.jpg")
        return try {
            application.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }
            MultipartBody.Part.createFormData(
                "Foto",
                file.name,
                file.asRequestBody("image/*".toMediaTypeOrNull())
            )
        } catch (e: Exception) {
            Log.e("EmpresaProfile", "Error preparando imagen", e)
            null
        }
    }
}
