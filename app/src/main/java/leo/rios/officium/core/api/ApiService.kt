package leo.rios.officium.core.api

import leo.rios.officium.login.data.ApiMessageResponse
import leo.rios.officium.login.data.AuthenticatedUserResponse
import leo.rios.officium.login.data.LoginResponse
import leo.rios.officium.empresaProfile.data.ProvinciaResponse
import leo.rios.officium.empresaProfile.data.SectorResponse
import leo.rios.officium.login.presentation.model.LogInModel
import leo.rios.officium.recover.data.RecoverResponse
import leo.rios.officium.recover.presentation.model.RecoverModel
import leo.rios.officium.registro.data.RegisterResponse
import leo.rios.officium.registro.presentation.model.RegisterModel
import leo.rios.officium.verificationCode.data.VerificationCodeResponse
import leo.rios.officium.verificationCode.presentation.model.VerificationCodeModel
import leo.rios.officium.verifyProfile.data.RegisterClientResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @POST("login")
    suspend fun apiLogIn(@Body login: LogInModel) : Response<LoginResponse>

    @POST("register")
    suspend fun apiAddUser(@Body register : RegisterModel) : Response<RegisterResponse>

    @POST("recover")
    suspend fun apiRecoverPassword(@Body recover: RecoverModel): Response<RecoverResponse>

    @POST("verifyCode")
    suspend fun apiVerificationCode(
        @Body code: VerificationCodeModel
    ): Response<VerificationCodeResponse>

    @Multipart
    @POST("desempleado")
    suspend fun apiRegisterProfile(
        @Part("IDUsuario") idUsuario: RequestBody,
        @Part("Nombre") nombre: RequestBody,
        @Part("Apellido") apellido: RequestBody,
        @Part("DNI") dni: RequestBody,
        @Part("Porfolios") porfolios: RequestBody,
        @Part("Disponibilidad") disponibilidad: RequestBody,
        @Part foto: MultipartBody.Part?
    ): Response<RegisterClientResponse>

    @Multipart
    @POST("empresa")
    suspend fun apiRegisterCompanyProfile(
        @Part("IDUsuario") idUsuario: RequestBody,
        @Part("NombreEmpresa") nombreEmpresa: RequestBody,
        @Part("CIF") cif: RequestBody,
        @Part("IDSector") idSector: RequestBody,
        @Part("Ubicacion") ubicacion: RequestBody,
        @Part("SitioWeb") sitioWeb: RequestBody,
        @Part picture: MultipartBody.Part?
    ) : Response<RegisterClientResponse>

    @POST("logout")
    suspend fun apiLogout(): Response<ApiMessageResponse>

    @GET("testAuth")
    suspend fun testAuth(): Response<ApiMessageResponse>

    @GET("user")
    suspend fun authenticatedUser(): Response<AuthenticatedUserResponse>

    @GET("sector")
    suspend fun apiGetSectors(): Response<SectorResponse>

    @GET("provincia")
    suspend fun apiGetProvincias(): Response<ProvinciaResponse>
}
