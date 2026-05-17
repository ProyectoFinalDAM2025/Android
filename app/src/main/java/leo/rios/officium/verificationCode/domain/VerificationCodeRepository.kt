package leo.rios.officium.verificationCode.domain

import android.util.Log
import leo.rios.officium.core.api.ApiService
import leo.rios.officium.core.api.toApiMessage
import leo.rios.officium.login.data.AuthenticatedUserResponse
import leo.rios.officium.verificationCode.data.VerificationCodeResponse
import leo.rios.officium.verificationCode.presentation.model.VerificationCodeModel
import javax.inject.Inject

class VerificationCodeRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun sendVerificationCodeRepository(
        verificationCodeModel: VerificationCodeModel
    ): Result<VerificationCodeResponse> {
        return try {
            val response = apiService.apiVerificationCode(verificationCodeModel)
            val body = response.body()

            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("VerificationCode Error", "Error servidor: $errorBody")
                val serverMessage = errorBody.toApiMessage()

                Result.failure(Exception(serverMessage ?: "Error al verificar el codigo"))
            }
        } catch (e: Exception) {
            Log.e("VerificationCode Error", "Error en la llamada a la API: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getAuthenticatedUserRepository(): Result<AuthenticatedUserResponse> {
        return try {
            val response = apiService.authenticatedUser()
            val user = response.body()

            if (response.isSuccessful && user != null) {
                Result.success(user)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("AuthenticatedUser Error", "Error servidor: $errorBody")
                val serverMessage = errorBody.toApiMessage()

                Result.failure(Exception(serverMessage ?: "No se pudo obtener el usuario autenticado"))
            }
        } catch (e: Exception) {
            Log.e("AuthenticatedUser Error", "Error en la llamada a la API: ${e.message}", e)
            Result.failure(e)
        }
    }
}
