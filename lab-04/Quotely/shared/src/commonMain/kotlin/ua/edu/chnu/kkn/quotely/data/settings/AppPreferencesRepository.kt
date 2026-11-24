package ua.edu.chnu.kkn.quotely.data.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ua.edu.chnu.kkn.quotely.domain.settings.AppPreferences
import ua.edu.chnu.kkn.quotely.domain.settings.PreferencesRepository
import ua.edu.chnu.kkn.quotely.domain.settings.ThemeMode

/** Every change is persisted in [AppSettings] right away and published to observers. */
internal class AppPreferencesRepository(
    private val settings: AppSettings,
) : PreferencesRepository {

    private val _preferences = MutableStateFlow(
        AppPreferences(language = settings.language, themeMode = settings.themeMode)
    )
    override val preferences: StateFlow<AppPreferences> = _preferences.asStateFlow()

    override fun setLanguage(languageTag: String) {
        settings.language = languageTag
        _preferences.update { it.copy(language = languageTag) }
    }

    override fun setThemeMode(mode: ThemeMode) {
        settings.themeMode = mode
        _preferences.update { it.copy(themeMode = mode) }
    }
}
