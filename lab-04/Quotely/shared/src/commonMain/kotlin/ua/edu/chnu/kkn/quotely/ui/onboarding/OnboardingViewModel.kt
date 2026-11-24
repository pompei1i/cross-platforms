package ua.edu.chnu.kkn.quotely.ui.onboarding

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import ua.edu.chnu.kkn.quotely.domain.onboarding.CompleteOnboardingUseCase

private val log = Logger.withTag("Onboarding")

class OnboardingViewModel(
    private val completeOnboarding: CompleteOnboardingUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _events = Channel<OnboardingEvent>(Channel.BUFFERED)
    val events: Flow<OnboardingEvent> = _events.receiveAsFlow()

    fun onAction(action: OnboardingAction) {
        when (action) {
            is OnboardingAction.PageChanged -> {
                if (action.page == _state.value.currentPage) return
                log.d { "Page ${action.page + 1}/${_state.value.pageCount} shown" }
                _state.update { it.copy(currentPage = action.page) }
            }
            OnboardingAction.Skip -> finish(skipped = true)
            OnboardingAction.GetStarted -> finish(skipped = false)
        }
    }

    private fun finish(skipped: Boolean) {
        log.i { if (skipped) "Onboarding skipped on page ${_state.value.currentPage + 1}" else "Onboarding completed" }
        completeOnboarding()
        _events.trySend(OnboardingEvent.NavigateToLogin)
    }
}
