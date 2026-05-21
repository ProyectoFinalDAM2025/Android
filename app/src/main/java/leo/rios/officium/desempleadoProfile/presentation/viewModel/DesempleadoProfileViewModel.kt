package leo.rios.officium.desempleadoProfile.presentation.viewModel

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
import leo.rios.officium.core.navigation.Login
import leo.rios.officium.desempleadoProfile.domain.DesempleadoProfileRepository
import leo.rios.officium.desempleadoProfile.presentation.model.DesempleadoProfileModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import leo.rios.officium.core.session.AuthState
import leo.rios.officium.core.dataStore.DataStoreManager
import leo.rios.officium.empresaProfile.data.ProvinciaData

@HiltViewModel
class DesempleadoProfileViewModel @Inject constructor(
    private val repository: DesempleadoProfileRepository,
    private val application: Application,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState.PROFILE_PENDING)
    val authState: StateFlow<AuthState> = _authState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre

    private val _apellido = MutableStateFlow("")
    val apellido: StateFlow<String> = _apellido

    private val _dni = MutableStateFlow("")
    val dni: StateFlow<String> = _dni

    private val _porfolios = MutableStateFlow("")
    val porfolios: StateFlow<String> = _porfolios

    private val _disponibilidad = MutableStateFlow("")
    val disponibilidad: StateFlow<String> = _disponibilidad

    private val _ubicacion = MutableStateFlow("")
    val ubicacion: StateFlow<String> = _ubicacion

    private val _foto = MutableStateFlow<Uri?>(null)
    val foto: StateFlow<Uri?> = _foto

    private val _state = MutableStateFlow<String?>(null)
    val state: StateFlow<String?> = _state

    private val _provincias = MutableStateFlow<List<ProvinciaData>>(emptyList())
    val provincias: StateFlow<List<ProvinciaData>> = _provincias

    init {
        loadProvincias()
    }

    fun onNombreChange(value: String) { _nombre.value = value }
    fun onApellidoChange(value: String) { _apellido.value = value }
    fun onDniChange(value: String) { _dni.value = value }
    fun onPorfoliosChange(value: String) { _porfolios.value = value }
    fun onDisponibilidadChange(value: String) { _disponibilidad.value = value }
    fun onUbicacionChange(value: String) { _ubicacion.value = value }
    fun onFotoSelected(uri: Uri?) { _foto.value = uri }

    private fun loadProvincias() = viewModelScope.launch {
        repository.getProvincias()
            .onSuccess { _provincias.value = it }
            .onFailure { _state.value = it.localizedMessage ?: "Error al obtener provincias" }
    }

    fun sendDesempleadoProfile(idUsuario: String) = viewModelScope.launch {
        if (
            _nombre.value.isBlank() ||
            _apellido.value.isBlank() ||
            _dni.value.isBlank() ||
            _porfolios.value.isBlank() ||
            _disponibilidad.value.isBlank() ||
            _ubicacion.value.isBlank()
        ) {
            _state.value = "Completa los campos obligatorios"
            return@launch
        }

        _isLoading.value = true

        try {
            val fotoPart = withContext(Dispatchers.IO) {
                _foto.value?.let { uriToImagePart(it) }
            }

            val profile = DesempleadoProfileModel(
                idUsuario = idUsuario,
                nombre = _nombre.value,
                apellido = _apellido.value,
                dni = _dni.value,
                porfolios = _porfolios.value,
                disponibilidad = _disponibilidad.value,
                ubicacion = _ubicacion.value
            )

            val result = repository.sendDesempleadoProfileRepository(profile, fotoPart)

            if (result.isSuccess) {
                dataStoreManager.saveRole("usuario")
                dataStoreManager.saveIdProfile(idUsuario)

                _state.value = "Perfil de desempleado creado"
                _authState.value = AuthState.AUTHENTICATED
            } else {
                _state.value =
                    result.exceptionOrNull()?.localizedMessage ?: "Error al crear perfil"
            }
        } catch (e: Exception) {
            _state.value = e.localizedMessage ?: "Error al crear perfil"
        } finally {
            _isLoading.value = false
        }
    }

    private fun uriToImagePart(uri: Uri): MultipartBody.Part? {
        val file = File(application.cacheDir, "desempleado_${System.currentTimeMillis()}.jpg")
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
            Log.e("DesempleadoProfile", "Error preparando imagen", e)
            null
        }
    }
}
