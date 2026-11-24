package ua.edu.chnu.kkn.quotely

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.touchlab.kermit.Logger
import org.koin.compose.viewmodel.koinViewModel
import ua.edu.chnu.kkn.quotely.locale.AppLocaleProvider
import ua.edu.chnu.kkn.quotely.locale.LocalAppLocale
import ua.edu.chnu.kkn.quotely.locale.SupportedLanguages
import ua.edu.chnu.kkn.quotely.ui.main.MainViewModel
import ua.edu.chnu.kkn.quotely.ui.navigation.AppNavigation
import ua.edu.chnu.kkn.quotely.ui.theme.QuotelyTheme

private val log = Logger.withTag("App")

/** Root composable. Koin must be started by the platform entry point before it is shown. */
@Composable
fun App(viewModel: MainViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        log.i { "Quotely started on ${getPlatform().name}" }
    }

    AppLocaleProvider(state.language) {
        QuotelyTheme(themeMode = state.themeMode) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                if (viewModel.backStack.isEmpty()) {
                    // The start destination is being resolved (reading the secure storage)
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    return@Surface
                }
                AppNavigation(
                    // The back stack lives in MainViewModel, so it survives the recomposition caused by a language switch
                    backStack = viewModel.backStack,
                    currentLanguage = state.language ?: resolveLanguage(LocalAppLocale.current),
                    themeMode = state.themeMode,
                    onAction = viewModel::onAction,
                )
            }
        }
    }
}

/** Maps a system locale like "uk_UA" or "en-US" to one of [SupportedLanguages], English by default. */
internal fun resolveLanguage(localeTag: String): String {
    val language = localeTag.substringBefore('_').substringBefore('-').lowercase()
    return if (language in SupportedLanguages) language else SupportedLanguages.first()
}
