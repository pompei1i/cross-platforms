package ua.edu.chnu.kkn.quotely.ui.login

import androidx.compose.runtime.Immutable
import ua.edu.chnu.kkn.quotely.domain.auth.validation.ValidationError
import ua.edu.chnu.kkn.quotely.domain.common.DataError

@Immutable
data class LoginState(
    val username: String = "",
    val password: String = "",
    val usernameError: ValidationError? = null,
    val passwordError: ValidationError? = null,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
) {
    val isSignInEnabled: Boolean get() = !isLoading
}

enum class LoginField { Username, Password }

sealed interface LoginAction {
    data class UsernameChanged(val value: String) : LoginAction
    data class PasswordChanged(val value: String) : LoginAction
    data object TogglePasswordVisibility : LoginAction

    /** The "Sign in" button and the keyboard action of the password field. */
    data object SignIn : LoginAction
}

sealed interface LoginEvent {
    /** Moves the cursor to the first field that failed validation. */
    data class FocusField(val field: LoginField) : LoginEvent
    data object NavigateToHome : LoginEvent

    /** Sign-in failed: shown in a snackbar. */
    data class ShowError(val error: DataError) : LoginEvent
}
