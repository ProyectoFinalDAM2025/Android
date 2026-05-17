package leo.rios.officium.desempleadoProfile.domain

import android.util.Log
import leo.rios.officium.core.api.ApiService
import leo.rios.officium.core.api.toApiMessage
import leo.rios.officium.desempleadoProfile.presentation.model.DesempleadoProfileModel
import leo.rios.officium.verifyProfile.data.RegisterClientResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class DesempleadoProfileRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun sendDesempleadoProfileRepository(
        profile: DesempleadoProfileModel,
        foto: MultipartBody.Part?
    ): Result<RegisterClientResponse> {
        return try {
            val response = apiService.apiRegisterProfile(
                idUsuario = profile.idUsuario.toRequestBody("text/plain".toMediaTypeOrNull()),
                nombre = profile.nombre.toRequestBody("text/plain".toMediaTypeOrNull()),
                apellido = profile.apellido.toRequestBody("text/plain".toMediaTypeOrNull()),
                dni = profile.dni.toRequestBody("text/plain".toMediaTypeOrNull()),
                porfolios = profile.porfolios.toRequestBody("text/plain".toMediaTypeOrNull()),
                disponibilidad = profile.disponibilidad.toRequestBody("text/plain".toMediaTypeOrNull()),
                foto = foto
            )

            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("DesempleadoProfile", "Error ${response.code()} ${response.message()} $errorBody")
                val serverMessage = errorBody.toApiMessage()

                Result.failure(
                    Exception(serverMessage ?: "Error al crear perfil de desempleado")
                )
            }
        } catch (e: Exception) {
            Log.e("DesempleadoProfile", "Error en la llamada a la API: ${e.message}", e)
            Result.failure(e)
        }
    }
}
