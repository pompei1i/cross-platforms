package ua.edu.chnu.kkn.quotely.domain.settings

import kotlinx.coroutines.flow.StateFlow

/** Appearance preferences chosen by the user. */
data class AppPreferences(
    /** Language tag ("en", "uk"), or `null` to follow the system language. */
    val language: String? = null,
    val themeMode: ThemeMode = ThemeMode.System,
)

interface PreferencesRepository {
    val preferences: StateFlow<AppPreferences>
    fun setLanguage(languageTag: String)
    fun setThemeMode(mode: ThemeMode)
}
