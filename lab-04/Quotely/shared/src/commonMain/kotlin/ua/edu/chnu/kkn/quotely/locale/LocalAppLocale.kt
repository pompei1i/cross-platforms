package ua.edu.chnu.kkn.quotely.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.key

/** Languages the app is localized into (see composeResources/values*). */
val SupportedLanguages = listOf("en", "uk")

/**
 * Overrides the locale used by Compose resources, so the language can be switched
 * inside the app without changing the system settings.
 */
expect object LocalAppLocale {
    val current: String @Composable get

    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}

/** Provides [languageTag] (or the system locale when `null`) to [content] and recreates it on change. */
@Composable
fun AppLocaleProvider(languageTag: String?, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppLocale provides languageTag) {
        key(languageTag) {
            content()
        }
    }
}
