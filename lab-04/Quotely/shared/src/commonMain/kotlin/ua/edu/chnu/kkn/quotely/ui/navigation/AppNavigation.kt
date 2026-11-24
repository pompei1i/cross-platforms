package ua.edu.chnu.kkn.quotely.ui.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import ua.edu.chnu.kkn.quotely.domain.settings.ThemeMode
import ua.edu.chnu.kkn.quotely.ui.home.HomeRoute
import ua.edu.chnu.kkn.quotely.ui.login.LoginRoute
import ua.edu.chnu.kkn.quotely.ui.main.MainAction
import ua.edu.chnu.kkn.quotely.ui.onboarding.OnboardingRoute
import ua.edu.chnu.kkn.quotely.ui.quotes.QuoteDetailPlaceholder
import ua.edu.chnu.kkn.quotely.ui.quotes.QuoteDetailRoute
import ua.edu.chnu.kkn.quotely.ui.settings.SettingsRoute

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AppNavigation(
    backStack: List<Route>,
    currentLanguage: String,
    themeMode: ThemeMode,
    onAction: (MainAction) -> Unit,
) {
    val onLanguageChange: (String) -> Unit = { onAction(MainAction.ChangeLanguage(it)) }
    val onBack: () -> Unit = { onAction(MainAction.NavigateBack) }

    NavDisplay(
        backStack = backStack,
        onBack = onBack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            // Every destination gets its own ViewModelStore, cleared when it leaves the back stack
            rememberViewModelStoreNavEntryDecorator(),
        ),
        // Wide windows show the quotes list and the opened quote side by side;
        // when there's room for one pane only, the regular single-pane strategy takes over.
        sceneStrategies = listOf(rememberListDetailSceneStrategy(), SinglePaneSceneStrategy()),
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
            entry<Route.Home>(
                metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { QuoteDetailPlaceholder() }),
            ) {
                HomeRoute(
                    selectedQuoteId = (backStack.lastOrNull() as? Route.QuoteDetail)?.quoteId,
                    onQuoteClick = { onAction(MainAction.OpenQuote(it)) },
                    onOpenSettings = { onAction(MainAction.OpenSettings) },
                )
            }
            entry<Route.QuoteDetail>(metadata = ListDetailSceneStrategy.detailPane()) { route ->
                QuoteDetailRoute(quoteId = route.quoteId, onBack = onBack)
            }
            entry<Route.Settings> {
                SettingsRoute(
                    currentLanguage = currentLanguage,
                    onLanguageChange = onLanguageChange,
                    themeMode = themeMode,
                    onThemeModeChange = { onAction(MainAction.ChangeThemeMode(it)) },
                    onShowOnboarding = { onAction(MainAction.ShowOnboardingAgain) },
                    onSignedOut = { onAction(MainAction.SignedOut) },
                    onBack = onBack,
                )
            }
        },
    )
}
