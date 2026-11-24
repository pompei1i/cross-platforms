package ua.edu.chnu.kkn.quotely

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import co.touchlab.kermit.Logger
import ua.edu.chnu.kkn.quotely.locale.AppLocaleProvider
import ua.edu.chnu.kkn.quotely.locale.LocalAppLocale
import ua.edu.chnu.kkn.quotely.locale.SupportedLanguages
import ua.edu.chnu.kkn.quotely.settings.AppSettings
import ua.edu.chnu.kkn.quotely.settings.ThemeMode
import ua.edu.chnu.kkn.quotely.ui.home.HomeScreen
import ua.edu.chnu.kkn.quotely.ui.onboarding.OnboardingScreen
import ua.edu.chnu.kkn.quotely.ui.theme.QuotelyTheme

private val log = Logger.withTag("App")

/** UI state backed by [AppSettings]: every change is persisted immediately. */
class AppState(private val settings: AppSettings) {
    var isOnboardingCompleted by mutableStateOf(settings.isOnboardingCompleted)
        private set
    var language by mutableStateOf(settings.language)
        private set
    var themeMode by mutableStateOf(settings.themeMode)
        private set

    /** Kept outside the onboarding screen so the page survives a language switch. */
    var onboardingPage by mutableIntStateOf(0)

    fun completeOnboarding() {
        settings.isOnboardingCompleted = true
        isOnboardingCompleted = true
    }

    fun showOnboardingAgain() {
        log.i { "Onboarding requested again" }
        onboardingPage = 0
        settings.isOnboardingCompleted = false
        isOnboardingCompleted = false
    }

    fun changeLanguage(languageTag: String) {
        log.i { "Language changed: $language -> $languageTag" }
        settings.language = languageTag
        language = languageTag
    }

    fun changeThemeMode(mode: ThemeMode) {
        log.i { "Theme changed: $themeMode -> $mode" }
        settings.themeMode = mode
        themeMode = mode
    }
}

@Composable
@Preview
fun App() {
    val state = remember { AppState(AppSettings()) }

    LaunchedEffect(Unit) {
        log.i { "Quotely started on ${getPlatform().name}, onboarding completed: ${state.isOnboardingCompleted}" }
    }

    AppLocaleProvider(state.language) {
        QuotelyTheme(themeMode = state.themeMode) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                val currentLanguage = state.language ?: resolveLanguage(LocalAppLocale.current)
                Crossfade(targetState = state.isOnboardingCompleted) { completed ->
                    if (completed) {
                        HomeScreen(
                            currentLanguage = currentLanguage,
                            onLanguageChange = state::changeLanguage,
                            themeMode = state.themeMode,
                            onThemeModeChange = state::changeThemeMode,
                            onShowOnboarding = state::showOnboardingAgain,
                        )
                    } else {
                        OnboardingScreen(
                            currentLanguage = currentLanguage,
                            onLanguageChange = state::changeLanguage,
                            onFinish = state::completeOnboarding,
                            initialPage = state.onboardingPage,
                            onPageChange = { state.onboardingPage = it },
                        )
                    }
                }
            }
        }
    }
}

/** Maps a system locale like "uk_UA" or "en-US" to one of [SupportedLanguages], English by default. */
internal fun resolveLanguage(localeTag: String): String {
    val language = localeTag.substringBefore('_').substringBefore('-').lowercase()
    return if (language in SupportedLanguages) language else SupportedLanguages.first()
}
