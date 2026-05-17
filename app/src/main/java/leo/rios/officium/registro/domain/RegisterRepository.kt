package leo.rios.officium.registro.domain

import android.util.Log
import leo.rios.officium.core.api.ApiService
import leo.rios.officium.core.api.toApiMessage
import leo.rios.officium.core.dataStore.DataStoreManager
import leo.rios.officium.registro.data.RegisterResponse
import leo.rios.officium.registro.presentation.model.RegisterModel
import javax.inject.Inject

class RegisterRepository @Inject constructor(
    private val apiService: ApiService,
    private val dataStoreManager: DataStoreManager
) {

    suspend fun registerUserRepository(registerModel: RegisterModel): Result<RegisterResponse> {
        return try {
            val response = apiService.apiAddUser(registerModel)

            if (response.isSuccessful) {
                val responseSuccess = response.body()
                    ?: return Result.failure(Exception("Respuesta vacía recibida del servidor"))

                val token = responseSuccess.data?.token

                if (token.isNullOrBlank()) {
                    return Result.failure(Exception("Token no recibido en la respuesta"))
                }

                dataStoreManager.saveAccessToken(token)

                Result.success(responseSuccess)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("Register Error", "Error servidor: $errorBody")
                val serverMessage = errorBody.toApiMessage()

                Result.failure(
                    Exception(serverMessage ?: "Error de registro")
                )
            }
        } catch (e: Exception) {
            Log.e("Register Error", "Error en la llamada a la API: ${e.message}", e)
            Result.failure(e)
        }
    }
}
