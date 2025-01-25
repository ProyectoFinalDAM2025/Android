package leo.rios.officium.core.api

import leo.rios.officium.login.data.LoginResponse
import leo.rios.officium.login.presentation.model.LogInModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("Usuario/logIn")
    suspend fun apiLogIn(@Body login: LogInModel) : Response<LoginResponse>
}