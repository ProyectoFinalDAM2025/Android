package leo.rios.officium.verifyProfile.presentation.viewModel


import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import leo.rios.officium.verifyProfile.domain.VerifyProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import leo.rios.officium.core.navigation.Login
import leo.rios.officium.verifyProfile.presentation.model.VerifyProfilModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject


@HiltViewModel
class VerifyProfileViewModel @Inject constructor(
    private val repositoryVerifyProfile: VerifyProfileRepository,
    private val application: Application
): ViewModel() {

/*    private val _idUsuario = MutableStateFlow<String>("")
    val idUsuario : StateFlow<String> = _idUsuario*/

    private val _nombre = MutableStateFlow<String>("")
    val nombre : StateFlow<String> = _nombre

    private val _apellido = MutableStateFlow<String>("")
    val apellido : StateFlow<String> = _apellido

    private val _dni = MutableStateFlow<String>("")
    val dni : StateFlow<String> = _dni

    private val _date = MutableStateFlow<String>("")
    val date : StateFlow<String> = _date

    private val _ciudad = MutableStateFlow<String>("")
    val ciudad : StateFlow<String> = _ciudad

    private val _sexo = MutableStateFlow<String>("")
    val sexo : StateFlow<String> = _sexo

    private val _picture = MutableStateFlow<Uri?>(null)
    val picture : StateFlow<Uri?> = _picture



    fun onNombreChange(nombre: String) {
        _nombre.value = nombre
        Log.d("ViewModel", "onChange nombre: $nombre")
    }
    fun onApellidoChange(apellido: String) {
        _apellido.value = apellido
        Log.d("ViewModel", "onChange apellido: $apellido")
    }
    fun onDniChange(dni: String) {
        _dni.value = dni
        Log.d("ViewModel", "onChange dni: $dni")
    }
    fun onDateChange(date: String) {
        _date.value = date
        Log.d("ViewModel", "onChange dni: $date")
    }
    fun onCiudadChange(ciudad: String) {
        _ciudad.value = ciudad
        Log.d("ViewModel", "onChange dni: $ciudad")
    }
    fun onSexoChange(sexo: String) {
        _sexo.value = sexo
        Log.d("ViewModel", "onChange dni: $sexo")
    }
    fun onPictureSelected(uri: Uri?) { _picture.value = uri }

   private fun getFileFromUri(uri: Uri): File? {
        val fileName = "picture_${System.currentTimeMillis()}.jpg"
        val file = File(application.cacheDir,fileName)
       return try {
           val inputStream = application.contentResolver.openInputStream(uri)
           val outputStream = FileOutputStream(file)
           inputStream?.copyTo(outputStream)
           inputStream?.close()
           outputStream.close()
           file
       }catch (e:Exception){
           Log.e("File Conversion", "Error converting Uri to File", e)
           null
       }
    }


    fun sendRegisterProfile(navigateTo: NavController,idUsuario:String) = viewModelScope.launch {
        Log.d("ViewModel", "sendRegisterProfile ha sido llamada")
        withContext(Dispatchers.IO){

            Log.d("ViewModel", "Entrando a sendRegisterProfile con idUsuario: $idUsuario")

            val imageFile = _picture.value?.let { getFileFromUri(it) }
            val imagePart = imageFile?.let {
                val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull()) // ← Corrección aquí
                MultipartBody.Part.createFormData("Foto",it.name,requestFile)
            }

            Log.d("Data viewModel: ","$_nombre")
            Log.d("Data viewModel: ","$_apellido")
            Log.d("Data viewModel: ","$_dni")
            Log.d("Data viewModel: ","$_date")
            Log.d("Data viewModel: ","$_ciudad")
            Log.d("Data viewModel: ","$_sexo")
            val user = VerifyProfilModel(
                idUsuario = idUsuario,
                nombre = _nombre.value,
                apellido = apellido.value,
                dni = _dni.value,
                porfolios = _ciudad.value,
                disponibilidad = sexo.value
            )
            val result = repositoryVerifyProfile.sendVerifyProfileRepository(user,imagePart)
            result
                .onSuccess {
                    //Hacer algo con la peticion.
                    Log.d("API Response", "Registro exitoso")
                    withContext(Dispatchers.Main) {  // 🚀 Cambia al hilo principal para la navegación
                        navigateTo.navigate(Login){
                            popUpTo<Login>{inclusive = true}
                        }
                    }
                }
                .onFailure {
                    Log.e("API Error", "Error en el registro: ${it.message}")
                }
        }
    }

}
