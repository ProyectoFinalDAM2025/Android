package leo.rios.officium.login.domain

import android.util.Log
import leo.rios.officium.core.api.ApiService
import leo.rios.officium.core.api.toApiMessage
import leo.rios.officium.core.dataStore.DataStoreManager
import leo.rios.officium.login.data.ApiMessageResponse
import leo.rios.officium.login.data.LoginResponse
import leo.rios.officium.login.data.getProfileId
import leo.rios.officium.login.data.getProfileName
import leo.rios.officium.login.data.getProfilePhoto
import leo.rios.officium.login.data.normalizeProfileRole
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
                    val normalizedRole = authData.rol.normalizeProfileRole()

                    dataStoreManager.saveAccessToken(authData.token)
                    dataStoreManager.saveRole(normalizedRole)
                    dataStoreManager.saveProfileBasicData(
                        idProfile = authData.profile.getProfileId(),
                        profileName = authData.profile.getProfileName(),
                        profilePhoto = authData.profile.getProfilePhoto(),
                        profileJson = authData.profile?.toString()
                    )
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

    suspend fun logoutUserRepository(): Result<ApiMessageResponse> {
        return try {
            val response = apiService.apiLogout()
            val body = response.body()

            if (response.isSuccessful) {
                Result.success(body ?: ApiMessageResponse(message = "Sesion cerrada correctamente"))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("Logout Error", "Error servidor: $errorBody")
                Result.failure(Exception(errorBody.toApiMessage() ?: "Error al cerrar sesion"))
            }
        } catch (e: Exception) {
            Log.e("Logout Error", "Error en la llamada a la API: ${e.message}", e)
            Result.failure(e)
        }
    }
}
