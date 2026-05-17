package leo.rios.officium.verifyProfile.domain

import android.util.Log
import leo.rios.officium.verifyProfile.data.RegisterClientResponse
import leo.rios.officium.core.api.ApiService
import leo.rios.officium.verifyProfile.presentation.model.VerifyProfilModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class VerifyProfileRepository @Inject constructor(
    private val apiService: ApiService
){

    suspend fun sendVerifyProfileRepository(
        user: VerifyProfilModel,
        picture: MultipartBody.Part?
    ) : Result<RegisterClientResponse>{
        return  try {


            Log.d("Repostory ON","Entro")
            val idUsuario = user.idUsuario.toRequestBody("text/plain".toMediaTypeOrNull())
            val nombre = user.nombre.toRequestBody("text/plain".toMediaTypeOrNull())
            val apellido = user.apellido.toRequestBody("text/plain".toMediaTypeOrNull())
            val dni = user.dni.toRequestBody("text/plain".toMediaTypeOrNull())
            val porfolios = user.porfolios.toRequestBody("text/plain".toMediaTypeOrNull())
            val disponibilidad = user.disponibilidad.toRequestBody("text/plain".toMediaTypeOrNull())


            Log.d("API_CALL", "idUsuario: ${idUsuario.contentType()} - ${idUsuario.contentLength()}")
            Log.d("API_CALL", "nombre: ${nombre.contentType()} - ${nombre.contentLength()}")
            Log.d("API_CALL", "apellido: ${apellido.contentType()} - ${apellido.contentLength()}")
            Log.d("API_CALL", "Imagen: ${picture?.body?.contentType()} - ${picture?.body?.contentLength()}")
            val response = apiService.apiRegisterProfile(idUsuario, nombre, apellido, dni, porfolios, disponibilidad, picture)
            if(response.isSuccessful){
                Log.d("Repository: VerifyProfile","${response.body()}")
                val registerClientResponse = response.body()

                if(registerClientResponse != null){
                    Result.success(registerClientResponse)
                }else{
                    Log.e("VerificationCode Error", "Respuesta vacía recibida del servidor")
                    Result.failure(Exception("Respuesta vacía recidida del seervidor"))
                }
            }else{
                Log.e("VerifyProfile Error", "Error de respuesta del servidor: Código ${response.code()}, Mensaje: ${response.message()}")
                Result.failure(Exception("Respuesta vacía recibido del servidor"))
            }
        }catch (e: Exception){
            Log.e("VerifyProfile Error", "Error en la llamada a la API: ${e.message}", e)
            Result.failure(e)
        }
    }
}
