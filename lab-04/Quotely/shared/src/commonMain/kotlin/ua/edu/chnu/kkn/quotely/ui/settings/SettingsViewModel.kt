package ua.edu.chnu.kkn.quotely.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.edu.chnu.kkn.quotely.domain.auth.GetCurrentUserUseCase
import ua.edu.chnu.kkn.quotely.domain.auth.LogoutUseCase
import ua.edu.chnu.kkn.quotely.domain.common.onFailure
import ua.edu.chnu.kkn.quotely.domain.common.onSuccess

class SettingsViewModel(
    private val getCurrentUser: GetCurrentUserUseCase,
    private val logout: LogoutUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events: Flow<SettingsEvent> = _events.receiveAsFlow()

    init {
        loadProfile()
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            SettingsAction.RetryProfile -> loadProfile()
            SettingsAction.SignOut -> viewModelScope.launch {
                logout()
                _events.send(SettingsEvent.NavigateToLogin)
            }
        }
    }

    /** An authorized call: an expired access token is refreshed by the HTTP client transparently. */
    private fun loadProfile() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getCurrentUser()
                .onSuccess { user -> _state.update { it.copy(user = user) } }
                .onFailure { error -> _state.update { it.copy(error = error) } }
            _state.update { it.copy(isLoading = false) }
        }
    }
}
