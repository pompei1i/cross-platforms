package ua.edu.chnu.kkn.quotely.ui.onboarding

import androidx.compose.runtime.Immutable

@Immutable
data class OnboardingState(
    val currentPage: Int = 0,
    val pageCount: Int = onboardingPages.size,
) {
    val isLastPage: Boolean get() = currentPage == pageCount - 1
}

sealed interface OnboardingAction {
    data class PageChanged(val page: Int) : OnboardingAction
    data object Skip : OnboardingAction
    data object GetStarted : OnboardingAction
}

sealed interface OnboardingEvent {
    /** The result is saved, the welcome flow continues on the sign-in screen. */
    data object NavigateToLogin : OnboardingEvent
}
