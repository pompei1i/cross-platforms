package ua.edu.chnu.kkn.quotely.ui.main

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ua.edu.chnu.kkn.quotely.domain.onboarding.IsOnboardingCompletedUseCase
import ua.edu.chnu.kkn.quotely.domain.onboarding.ResetOnboardingUseCase
import ua.edu.chnu.kkn.quotely.domain.settings.AppPreferences
import ua.edu.chnu.kkn.quotely.domain.settings.ChangeLanguageUseCase
import ua.edu.chnu.kkn.quotely.domain.settings.ChangeThemeModeUseCase
import ua.edu.chnu.kkn.quotely.domain.settings.ObservePreferencesUseCase
import ua.edu.chnu.kkn.quotely.ui.navigation.Route

private val log = Logger.withTag("Main")

/**
 * ViewModel of the root ("main") screen: picks the start destination depending on whether
 * the onboarding was passed, owns the Navigation 3 back stack and the app-wide preferences.
 */
class MainViewModel(
    isOnboardingCompleted: IsOnboardingCompletedUseCase,
    observePreferences: ObservePreferencesUseCase,
    private val changeLanguage: ChangeLanguageUseCase,
    private val changeThemeMode: ChangeThemeModeUseCase,
    private val resetOnboarding: ResetOnboardingUseCase,
) : ViewModel() {

    /** Kept in the ViewModel so it survives configuration changes and in-app language switches. */
    val backStack: SnapshotStateList<Route> = mutableStateListOf(
        if (isOnboardingCompleted()) Route.Login else Route.Onboarding
    )

    val state: StateFlow<MainState> = observePreferences()
        .map { it.toState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, observePreferences().value.toState())

    init {
        log.i { "Start destination: ${backStack.first()}" }
    }

    fun onAction(action: MainAction) {
        when (action) {
            is MainAction.ChangeLanguage -> {
                log.i { "Language changed: ${state.value.language} -> ${action.languageTag}" }
                changeLanguage(action.languageTag)
            }
            is MainAction.ChangeThemeMode -> {
                log.i { "Theme changed: ${state.value.themeMode} -> ${action.mode}" }
                changeThemeMode(action.mode)
            }
            MainAction.OnboardingFinished -> replaceBackStack(Route.Login)
            MainAction.SignedIn -> replaceBackStack(Route.Home)
            MainAction.ShowOnboardingAgain -> {
                log.i { "Onboarding requested again" }
                resetOnboarding()
                replaceBackStack(Route.Onboarding)
            }
            MainAction.NavigateBack -> if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
        }
    }

    /** Opens [route] as the new root, so "back" does not return to the previous flow. */
    private fun replaceBackStack(route: Route) {
        log.d { "Navigate: ${backStack.lastOrNull()} -> $route" }
        if (backStack.singleOrNull() == route) return
        // Both changes land in one snapshot, NavDisplay never sees an empty back stack
        backStack.clear()
        backStack.add(route)
    }

    private fun AppPreferences.toState() = MainState(language = language, themeMode = themeMode)
}
