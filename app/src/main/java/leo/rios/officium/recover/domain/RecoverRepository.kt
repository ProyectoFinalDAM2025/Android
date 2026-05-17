package leo.rios.officium.recover.domain

import android.util.Log
import leo.rios.officium.core.api.ApiService
import leo.rios.officium.core.api.toApiMessage
import leo.rios.officium.recover.data.RecoverResponse
import leo.rios.officium.recover.presentation.model.RecoverModel
import javax.inject.Inject

class RecoverRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun recoverPasswordRepository(recoverModel: RecoverModel): Result<RecoverResponse> {
        return try {
            val response = apiService.apiRecoverPassword(recoverModel)

            if (response.isSuccessful) {
                val responseSuccess = response.body()
                    ?: return Result.failure(Exception("Respuesta vacia recibida del servidor"))

                Result.success(responseSuccess)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("Recover Error", "Error servidor: $errorBody")

                val serverMessage = errorBody.toApiMessage()

                Result.failure(
                    Exception(serverMessage ?: "Error al recuperar contrasena")
                )
            }
        } catch (e: Exception) {
            Log.e("Recover Error", "Error en la llamada a la API: ${e.message}", e)
            Result.failure(e)
        }
    }
}
