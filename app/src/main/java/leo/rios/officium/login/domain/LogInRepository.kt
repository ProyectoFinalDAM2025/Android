package leo.rios.officium.login.domain

import android.util.Log
import leo.rios.officium.core.api.ApiService
import leo.rios.officium.login.data.LoginResponse
import leo.rios.officium.login.presentation.model.LogInModel
import javax.inject.Inject

class LogInRepository @Inject constructor (
    private val apiService: ApiService
){

    suspend fun loginUser(logInModel: LogInModel): Result<LoginResponse> {
        return  try{
            val response = apiService.apiLogIn(logInModel)
            if (response.isSuccessful){
                Log.d("Api Response","${response.body()}")
                val loginResponse = response.body()
                if(loginResponse != null){
                    Result.success(loginResponse)
                }else{
                    Log.e("Login Error", "Respuesta vacía recibida del servidor")
                    Result.failure(Exception("Respuesta vacía recibido del servidor"))
                }

            }else{
                Log.e("Login Error", "Error de respuesta del servidor: Código ${response.code()}, Mensaje: ${response.message()}")
                Result.failure(Exception("Respuesta vacía recibido del servidor"))
            }

        }catch (e: Exception){
            Log.e("Login Error", "Error en la llamada a la API: ${e.message}", e)
            Result.failure(e)
        }
    }

}