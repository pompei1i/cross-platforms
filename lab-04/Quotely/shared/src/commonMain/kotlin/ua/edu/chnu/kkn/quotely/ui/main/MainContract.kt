package ua.edu.chnu.kkn.quotely.ui.main

import androidx.compose.runtime.Immutable
import ua.edu.chnu.kkn.quotely.domain.settings.ThemeMode

@Immutable
data class MainState(
    /** Language tag chosen by the user, or `null` to follow the system language. */
    val language: String? = null,
    val themeMode: ThemeMode = ThemeMode.System,
)

sealed interface MainAction {
    data class ChangeLanguage(val languageTag: String) : MainAction
    data class ChangeThemeMode(val mode: ThemeMode) : MainAction

    /** The onboarding was finished or skipped (its result is already saved). */
    data object OnboardingFinished : MainAction
    data object SignedIn : MainAction
    data object SignedOut : MainAction
    data object ShowOnboardingAgain : MainAction
    data object NavigateBack : MainAction
}
