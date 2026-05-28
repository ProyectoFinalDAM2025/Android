package leo.rios.officium.notifications.domain

import leo.rios.officium.core.api.ApiService
import leo.rios.officium.core.api.toApiMessage
import leo.rios.officium.notifications.data.NotificationDto
import javax.inject.Inject

class NotificationsRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getNotifications(): Result<Pair<List<NotificationDto>, Int>> {
        return try {
            val response = apiService.apiGetNotifications()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body.data to body.unreadCount)
            } else {
                Result.failure(Exception(response.errorBody()?.string().toApiMessage() ?: body?.message ?: "No se pudieron cargar las notificaciones"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsRead(notificationId: Int): Result<Unit> =
        safeUnitCall("No se pudo marcar como leida") {
            apiService.apiMarkNotificationAsRead(notificationId)
        }

    suspend fun deleteNotification(notificationId: Int): Result<Unit> =
        safeUnitCall("No se pudo eliminar la notificacion") {
            apiService.apiDeleteNotification(notificationId)
        }

    private suspend fun safeUnitCall(
        fallbackMessage: String,
        request: suspend () -> retrofit2.Response<*>
    ): Result<Unit> {
        return try {
            val response = request()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string().toApiMessage() ?: fallbackMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
