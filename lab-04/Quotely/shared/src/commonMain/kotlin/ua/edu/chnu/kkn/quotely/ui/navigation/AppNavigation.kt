package ua.edu.chnu.kkn.quotely.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import ua.edu.chnu.kkn.quotely.domain.settings.ThemeMode
import ua.edu.chnu.kkn.quotely.ui.home.HomeScreen
import ua.edu.chnu.kkn.quotely.ui.login.LoginRoute
import ua.edu.chnu.kkn.quotely.ui.main.MainAction
import ua.edu.chnu.kkn.quotely.ui.onboarding.OnboardingRoute

@Composable
fun AppNavigation(
    backStack: List<Route>,
    currentLanguage: String,
    themeMode: ThemeMode,
    onAction: (MainAction) -> Unit,
) {
    val onLanguageChange: (String) -> Unit = { onAction(MainAction.ChangeLanguage(it)) }

    NavDisplay(
        backStack = backStack,
        onBack = { onAction(MainAction.NavigateBack) },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            // Every destination gets its own ViewModelStore, cleared when it leaves the back stack
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<Route.Onboarding> {
                OnboardingRoute(
                    currentLanguage = currentLanguage,
                    onLanguageChange = onLanguageChange,
                    onFinished = { onAction(MainAction.OnboardingFinished) },
                )
            }
            entry<Route.Login> {
                LoginRoute(
                    currentLanguage = currentLanguage,
                    onLanguageChange = onLanguageChange,
                    onSignedIn = { onAction(MainAction.SignedIn) },
                )
            }
            entry<Route.Home> {
                HomeScreen(
                    currentLanguage = currentLanguage,
                    onLanguageChange = onLanguageChange,
                    themeMode = themeMode,
                    onThemeModeChange = { onAction(MainAction.ChangeThemeMode(it)) },
                    onShowOnboarding = { onAction(MainAction.ShowOnboardingAgain) },
                )
            }
        },
    )
}
