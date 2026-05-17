package leo.rios.officium.login.domain

import android.util.Log
import leo.rios.officium.core.api.ApiService
import leo.rios.officium.core.api.toApiMessage
import leo.rios.officium.core.dataStore.DataStoreManager
import leo.rios.officium.login.data.LoginResponse
import leo.rios.officium.login.presentation.model.LogInModel
import javax.inject.Inject

class LogInRepository @Inject constructor(
    private val apiService: ApiService,
    private val dataStoreManager: DataStoreManager
) {



    suspend fun loginUserRepository(logInModel: LogInModel): Result<LoginResponse> {
        return try {
            val response = apiService.apiLogIn(logInModel)
            if (response.isSuccessful) {
                val loginResponse = response.body()
                val authData = loginResponse?.data

                if (loginResponse != null && authData != null) {
                    dataStoreManager.saveAccessToken(authData.token)
                    dataStoreManager.saveRole(authData.rol)
                    Result.success(loginResponse)
                }
                else {
                    val serverMessage = loginResponse?.message
                        ?: "Respuesta vacía recibida del servidor"

                    Log.e("Login Error", serverMessage)

                    Result.failure(Exception(serverMessage))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(
                    "Login Error",
                    "Error servidor: $errorBody"
                )
                val serverMessage = errorBody.toApiMessage()

                Result.failure(
                    Exception(
                        serverMessage ?: "Error de login"
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("Login Error", "Error en la llamada a la API: ${e.message}", e)
            Result.failure(e)
        }
    }
}
