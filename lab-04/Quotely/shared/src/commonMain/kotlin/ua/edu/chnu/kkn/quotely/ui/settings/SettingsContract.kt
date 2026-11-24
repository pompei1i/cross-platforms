package ua.edu.chnu.kkn.quotely.ui.settings

import androidx.compose.runtime.Immutable
import ua.edu.chnu.kkn.quotely.domain.auth.User
import ua.edu.chnu.kkn.quotely.domain.common.DataError

@Immutable
data class SettingsState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: DataError? = null,
)

sealed interface SettingsAction {
    data object RetryProfile : SettingsAction
    data object SignOut : SettingsAction
}

sealed interface SettingsEvent {
    data object NavigateToLogin : SettingsEvent
}
