package leo.rios.officium.notifications.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import leo.rios.officium.notifications.data.NotificationDto
import leo.rios.officium.notifications.domain.NotificationsRepository
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationsRepository
) : ViewModel() {
    private val _notifications = MutableStateFlow<List<NotificationDto>>(emptyList())
    val notifications: StateFlow<List<NotificationDto>> = _notifications

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        _isLoading.value = true
        repository.getNotifications()
            .onSuccess { (items, unreadCount) ->
                _notifications.value = items
                _unreadCount.value = unreadCount
            }
            .onFailure { _message.value = it.localizedMessage ?: "Error al cargar notificaciones" }
        _isLoading.value = false
    }

    fun markAsRead(notificationId: Int) = viewModelScope.launch {
        repository.markAsRead(notificationId)
            .onSuccess { refresh() }
            .onFailure { _message.value = it.localizedMessage ?: "Error al marcar notificacion" }
    }

    fun deleteNotification(notificationId: Int) = viewModelScope.launch {
        repository.deleteNotification(notificationId)
            .onSuccess {
                _message.value = "Notificacion eliminada"
                refresh()
            }
            .onFailure { _message.value = it.localizedMessage ?: "Error al eliminar notificacion" }
    }
}
