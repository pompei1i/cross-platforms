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
import kotlinx.coroutines.launch
import ua.edu.chnu.kkn.quotely.domain.auth.IsAuthorizedUseCase
import ua.edu.chnu.kkn.quotely.domain.auth.ObserveSessionExpiredUseCase
import ua.edu.chnu.kkn.quotely.domain.onboarding.IsOnboardingCompletedUseCase
import ua.edu.chnu.kkn.quotely.domain.onboarding.ResetOnboardingUseCase
import ua.edu.chnu.kkn.quotely.domain.settings.AppPreferences
import ua.edu.chnu.kkn.quotely.domain.settings.ChangeLanguageUseCase
import ua.edu.chnu.kkn.quotely.domain.settings.ChangeThemeModeUseCase
import ua.edu.chnu.kkn.quotely.domain.settings.ObservePreferencesUseCase
import ua.edu.chnu.kkn.quotely.ui.navigation.Route

private val log = Logger.withTag("Main")

/**
 * ViewModel of the root ("main") screen: picks the start destination from the onboarding flag and
 * the stored session, owns the Navigation 3 back stack and the app-wide preferences.
 */
class MainViewModel(
    private val isOnboardingCompleted: IsOnboardingCompletedUseCase,
    private val isAuthorized: IsAuthorizedUseCase,
    observeSessionExpired: ObserveSessionExpiredUseCase,
    observePreferences: ObservePreferencesUseCase,
    private val changeLanguage: ChangeLanguageUseCase,
    private val changeThemeMode: ChangeThemeModeUseCase,
    private val resetOnboarding: ResetOnboardingUseCase,
) : ViewModel() {

    /**
     * Kept in the ViewModel so it survives configuration changes and in-app language switches.
     * Empty until the start destination is known (reading the secure storage is asynchronous).
     */
    val backStack: SnapshotStateList<Route> = mutableStateListOf()

    val state: StateFlow<MainState> = observePreferences()
        .map { it.toState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, observePreferences().value.toState())

    init {
        viewModelScope.launch {
            val start = destinationAfterOnboarding().takeIf { isOnboardingCompleted() } ?: Route.Onboarding
            log.i { "Start destination: $start" }
            replaceBackStack(start)
        }
        viewModelScope.launch {
            observeSessionExpired().collect {
                log.w { "Session expired" }
                replaceBackStack(Route.Login)
            }
        }
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
            // A signed-in user who replays the onboarding goes straight back home
            MainAction.OnboardingFinished -> viewModelScope.launch { replaceBackStack(destinationAfterOnboarding()) }
            MainAction.SignedIn -> replaceBackStack(Route.Home)
            MainAction.SignedOut -> replaceBackStack(Route.Login)
            MainAction.ShowOnboardingAgain -> {
                log.i { "Onboarding requested again" }
                resetOnboarding()
                replaceBackStack(Route.Onboarding)
            }
            is MainAction.OpenQuote -> openQuote(action.quoteId)
            MainAction.OpenSettings -> if (backStack.lastOrNull() != Route.Settings) backStack.add(Route.Settings)
            MainAction.NavigateBack -> if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
        }
    }

    /** Picking another quote swaps the open detail instead of stacking details on top of each other. */
    private fun openQuote(quoteId: Int) {
        val route = Route.QuoteDetail(quoteId)
        when (backStack.lastOrNull()) {
            route -> Unit
            is Route.QuoteDetail -> backStack[backStack.lastIndex] = route
            else -> backStack.add(route)
        }
    }

    private suspend fun destinationAfterOnboarding(): Route = if (isAuthorized()) Route.Home else Route.Login

    /** Opens [route] as the new root, so "back" does not return to the previous flow. */
    private fun replaceBackStack(route: Route) {
        if (backStack.singleOrNull() == route) return
        log.d { "Navigate: ${backStack.lastOrNull()} -> $route" }
        backStack.clear()
        backStack.add(route)
    }

    private fun AppPreferences.toState() = MainState(language = language, themeMode = themeMode)
}
