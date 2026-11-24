package ua.edu.chnu.kkn.quotely.ui.login

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import ua.edu.chnu.kkn.quotely.domain.auth.validation.ValidatePasswordUseCase
import ua.edu.chnu.kkn.quotely.domain.auth.validation.ValidateUsernameUseCase

private val log = Logger.withTag("Login")

class LoginViewModel(
    private val validateUsername: ValidateUsernameUseCase,
    private val validatePassword: ValidatePasswordUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events: Flow<LoginEvent> = _events.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            // Once an error is shown it is re-checked on every keystroke, so it disappears as soon as it's fixed
            is LoginAction.UsernameChanged -> _state.update {
                it.copy(
                    username = action.value,
                    usernameError = if (it.usernameError != null) validateUsername(action.value) else null,
                )
            }
            is LoginAction.PasswordChanged -> _state.update {
                it.copy(
                    password = action.value,
                    passwordError = if (it.passwordError != null) validatePassword(action.value) else null,
                )
            }
            LoginAction.TogglePasswordVisibility -> _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            LoginAction.SignIn -> signIn()
        }
    }

    private fun signIn() {
        val current = _state.value
        if (current.isLoading) return

        val usernameError = validateUsername(current.username)
        val passwordError = validatePassword(current.password)
        _state.update { it.copy(usernameError = usernameError, passwordError = passwordError) }

        when {
            usernameError != null -> _events.trySend(LoginEvent.FocusField(LoginField.Username))
            passwordError != null -> _events.trySend(LoginEvent.FocusField(LoginField.Password))
            else -> {
                log.i { "Credentials are valid for '${current.username.trim()}'" }
                _events.trySend(LoginEvent.NavigateToHome)
            }
        }
    }
}
