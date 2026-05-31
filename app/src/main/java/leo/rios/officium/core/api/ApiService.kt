package leo.rios.officium.core.api

import leo.rios.officium.login.data.ApiMessageResponse
import leo.rios.officium.login.data.AuthenticatedUserResponse
import leo.rios.officium.login.data.LoginResponse
import leo.rios.officium.jobOffers.data.JobOfferListResponse
import leo.rios.officium.jobOffers.data.JobApplicationListResponse
import leo.rios.officium.jobOffers.data.JobApplicationRequest
import leo.rios.officium.jobOffers.data.JobApplicationResponse
import leo.rios.officium.jobOffers.data.JobApplicationUpdateRequest
import leo.rios.officium.jobOffers.data.JobOfferRequest
import leo.rios.officium.jobOffers.data.JobOfferResponse
import leo.rios.officium.jobOffers.data.JobOfferUpdateRequest
import leo.rios.officium.jobOffers.data.ReporteOfertaRequest
import leo.rios.officium.empresaProfile.data.ProvinciaResponse
import leo.rios.officium.empresaProfile.data.SectorResponse
import leo.rios.officium.login.presentation.model.LogInModel
import leo.rios.officium.notifications.data.NotificationResponse
import leo.rios.officium.recover.data.RecoverResponse
import leo.rios.officium.recover.presentation.model.RecoverModel
import leo.rios.officium.registro.data.RegisterResponse
import leo.rios.officium.registro.presentation.model.RegisterModel
import leo.rios.officium.subscriptions.data.CategoriaResponse
import leo.rios.officium.subscriptions.data.MyApplicationsResponse
import leo.rios.officium.subscriptions.data.SubscriptionRequest
import leo.rios.officium.userProfile.data.DocumentoListResponse
import leo.rios.officium.userProfile.data.DocumentoResponse
import leo.rios.officium.userProfile.data.ComentarioRequest
import leo.rios.officium.userProfile.data.ComentarioUpdateRequest
import leo.rios.officium.userProfile.data.PublicacionListResponse
import leo.rios.officium.userProfile.data.PublicacionPageResponse
import leo.rios.officium.userProfile.data.PublicacionResponse
import leo.rios.officium.userProfile.data.ReportePublicacionRequest
import leo.rios.officium.userProfile.data.UserProfileResponse
import leo.rios.officium.verificationCode.data.VerificationCodeResponse
import leo.rios.officium.verificationCode.presentation.model.VerificationCodeModel
import leo.rios.officium.verifyProfile.data.RegisterClientResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Query

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
        @Part("Ubicacion") ubicacion: RequestBody,
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

    @GET("notificacion")
    suspend fun apiGetNotifications(): Response<NotificationResponse>

    @GET("notificaciones/{id}")
    suspend fun apiMarkNotificationAsRead(
        @Path("id") id: Int
    ): Response<ApiMessageResponse>

    @DELETE("notificacion/{id}")
    suspend fun apiDeleteNotification(
        @Path("id") id: Int
    ): Response<ApiMessageResponse>

    @GET("user")
    suspend fun authenticatedUser(): Response<AuthenticatedUserResponse>

    @GET("sector")
    suspend fun apiGetSectors(): Response<SectorResponse>

    @GET("provincia")
    suspend fun apiGetProvincias(): Response<ProvinciaResponse>

    @GET("categoria")
    suspend fun apiGetCategorias(): Response<CategoriaResponse>

    @GET("categoriasUsuario")
    suspend fun apiGetAvailableCategorias(): Response<CategoriaResponse>

    @GET("misSuscripciones")
    suspend fun apiGetMySubscriptions(): Response<CategoriaResponse>

    @GET("misAplicaciones")
    suspend fun apiGetMyApplications(): Response<MyApplicationsResponse>

    @POST("suscripcion/add")
    suspend fun apiAddSubscription(
        @Body request: SubscriptionRequest
    ): Response<ApiMessageResponse>

    @POST("suscripcion/eliminar")
    suspend fun apiDeleteSubscription(
        @Body request: SubscriptionRequest
    ): Response<ApiMessageResponse>

    @GET("ofertasEmpleos")
    suspend fun apiGetMyJobOffers(
        @Query("page") page: Int = 1
    ): Response<JobOfferListResponse>

    @GET("ofertaEmpleo/buscar")
    suspend fun apiSearchJobOffers(
        @Query("titulo") title: String? = null,
        @Query("ubicacion") location: String? = null,
        @Query("categoria") category: String? = null,
        @Query("estado") status: String? = null,
        @Query("page") page: Int = 1
    ): Response<JobOfferListResponse>

    @POST("ofertaEmpleo")
    suspend fun apiCreateJobOffer(
        @Body request: JobOfferRequest
    ): Response<JobOfferResponse>

    @GET("ofertaEmpleo/{id}")
    suspend fun apiGetJobOffer(
        @Path("id") id: Int
    ): Response<JobOfferResponse>

    @PUT("ofertaEmpleo/{id}")
    suspend fun apiUpdateJobOffer(
        @Path("id") id: Int,
        @Body request: JobOfferUpdateRequest
    ): Response<JobOfferResponse>

    @POST("aplicacion")
    suspend fun apiApplyToJobOffer(
        @Body request: JobApplicationRequest
    ): Response<JobApplicationResponse>

    @GET("aplicacion/{oferta}/aplicaciones")
    suspend fun apiGetJobApplications(
        @Path("oferta") offerId: Int,
        @Query("page") page: Int = 1
    ): Response<JobApplicationListResponse>

    @PUT("aplicacion/{id}")
    suspend fun apiUpdateJobApplication(
        @Path("id") applicationId: Int,
        @Body request: JobApplicationUpdateRequest
    ): Response<JobApplicationResponse>

    @DELETE("aplicacion/{id}")
    suspend fun apiDeleteJobApplication(
        @Path("id") applicationId: Int
    ): Response<ApiMessageResponse>

    @POST("ofertaEmpleo/reportar")
    suspend fun apiReportJobOffer(
        @Body request: ReporteOfertaRequest
    ): Response<ApiMessageResponse>

    @GET("documentos/fotosByIDUsuario")
    suspend fun apiGetMyPhotos(): Response<DocumentoListResponse>

    @GET("documentos/fotosByIDUsuario/{userId}")
    suspend fun apiGetPhotosByUser(
        @Path("userId") userId: Int
    ): Response<DocumentoListResponse>

    @GET("documentos/videosByIDUsuario")
    suspend fun apiGetMyVideos(): Response<DocumentoListResponse>

    @GET("documentos/videosByIDUsuario/{userId}")
    suspend fun apiGetVideosByUser(
        @Path("userId") userId: Int
    ): Response<DocumentoListResponse>

    @GET("documentos/pdfsByIDUsuario")
    suspend fun apiGetMyPdfs(): Response<DocumentoListResponse>

    @GET("documentos/pdfsByIDUsuario/{userId}")
    suspend fun apiGetPdfsByUser(
        @Path("userId") userId: Int
    ): Response<DocumentoListResponse>

    @GET("publicaciones/postsByUsuario")
    suspend fun apiGetMyPublications(): Response<PublicacionListResponse>

    @GET("publicaciones/postsByUsuario/{userId}")
    suspend fun apiGetPublicationsByUser(
        @Path("userId") userId: Int
    ): Response<PublicacionListResponse>

    @GET("usuarios/{idUsuario}")
    suspend fun apiGetUserProfile(
        @Path("idUsuario") idUsuario: Int
    ): Response<UserProfileResponse>

    @GET("publicacion")
    suspend fun apiGetPublications(
        @Query("page") page: Int
    ): Response<PublicacionPageResponse>

    @GET("publicacion/{id}")
    suspend fun apiGetPublication(
        @Path("id") id: Int
    ): Response<PublicacionResponse>

    @Multipart
    @POST("publicacion")
    suspend fun apiCreatePublication(
        @Part("Contenido") contenido: RequestBody,
        @Part("TipoArchivo") tipoArchivo: RequestBody?,
        @Part archivo: MultipartBody.Part? = null,
        @Part thumbnail: MultipartBody.Part? = null
    ): Response<PublicacionResponse>

    @GET("publicacion/{id}/like")
    suspend fun apiLikePublication(
        @Path("id") id: Int
    ): Response<ApiMessageResponse>

    @DELETE("publicacion/{id}/unlike")
    suspend fun apiUnlikePublication(
        @Path("id") id: Int
    ): Response<ApiMessageResponse>

    @Multipart
    @POST("publicacion/{id}")
    suspend fun apiUpdatePublication(
        @Path("id") id: Int,
        @Part("_method") method: RequestBody,
        @Part("Contenido") contenido: RequestBody,
        @Part("TipoArchivo") tipoArchivo: RequestBody? = null,
        @Part archivo: MultipartBody.Part? = null,
        @Part thumbnail: MultipartBody.Part? = null
    ): Response<PublicacionResponse>

    @DELETE("publicacion/{id}")
    suspend fun apiDeletePublication(
        @Path("id") id: Int
    ): Response<ApiMessageResponse>

    @POST("comentario")
    suspend fun apiCreateComment(
        @Body request: ComentarioRequest
    ): Response<ApiMessageResponse>

    @PUT("comentario/{id}")
    suspend fun apiUpdateComment(
        @Path("id") id: Int,
        @Body request: ComentarioUpdateRequest
    ): Response<ApiMessageResponse>

    @DELETE("comentario/{id}")
    suspend fun apiDeleteComment(
        @Path("id") id: Int
    ): Response<ApiMessageResponse>

    @POST("publicacion/reportar")
    suspend fun apiReportPublication(
        @Body request: ReportePublicacionRequest
    ): Response<ApiMessageResponse>

    @Multipart
    @POST("documento")
    suspend fun apiCreateDocument(
        @Part("Tipo") tipo: RequestBody,
        @Part("Descripcion") descripcion: RequestBody?,
        @Part archivo: MultipartBody.Part,
        @Part thumbnail: MultipartBody.Part? = null
    ): Response<DocumentoResponse>

    @Multipart
    @POST("documento/{id}")
    suspend fun apiUpdateDocument(
        @Path("id") id: Int,
        @Part("_method") method: RequestBody,
        @Part("Descripcion") descripcion: RequestBody?,
        @Part archivo: MultipartBody.Part? = null,
        @Part thumbnail: MultipartBody.Part? = null
    ): Response<DocumentoResponse>

    @DELETE("documento/{id}")
    suspend fun apiDeleteDocument(
        @Path("id") id: Int
    ): Response<ApiMessageResponse>

    @Multipart
    @POST("desempleado/{id}")
    suspend fun apiUpdateDesempleadoProfile(
        @Path("id") id: String,
        @Part("_method") method: RequestBody,
        @Part("Nombre") nombre: RequestBody,
        @Part("Apellido") apellido: RequestBody,
        @Part("DNI") dni: RequestBody,
        @Part("Porfolios") porfolios: RequestBody,
        @Part("Disponibilidad") disponibilidad: RequestBody,
        @Part("Ubicacion") ubicacion: RequestBody,
        @Part foto: MultipartBody.Part? = null
    ): Response<leo.rios.officium.userProfile.data.ProfileUpdateResponse>

    @Multipart
    @POST("empresa/{id}")
    suspend fun apiUpdateEmpresaProfile(
        @Path("id") id: String,
        @Part("_method") method: RequestBody,
        @Part("NombreEmpresa") nombreEmpresa: RequestBody,
        @Part("CIF") cif: RequestBody,
        @Part("IDSector") idSector: RequestBody,
        @Part("Ubicacion") ubicacion: RequestBody,
        @Part("SitioWeb") sitioWeb: RequestBody,
        @Part foto: MultipartBody.Part? = null
    ): Response<leo.rios.officium.userProfile.data.ProfileUpdateResponse>
}
