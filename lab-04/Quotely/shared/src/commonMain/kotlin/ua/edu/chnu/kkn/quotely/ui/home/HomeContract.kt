package ua.edu.chnu.kkn.quotely.ui.home

import androidx.compose.runtime.Immutable
import ua.edu.chnu.kkn.quotely.domain.auth.User
import ua.edu.chnu.kkn.quotely.domain.common.DataError

@Immutable
data class HomeState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: DataError? = null,
)

sealed interface HomeAction {
    data object RetryProfile : HomeAction
    data object SignOut : HomeAction
}

sealed interface HomeEvent {
    data object NavigateToLogin : HomeEvent
}
