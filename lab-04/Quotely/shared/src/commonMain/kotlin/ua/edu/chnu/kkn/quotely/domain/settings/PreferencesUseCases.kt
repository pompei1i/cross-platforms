package ua.edu.chnu.kkn.quotely.domain.settings

import kotlinx.coroutines.flow.StateFlow

class ObservePreferencesUseCase(private val repository: PreferencesRepository) {
    operator fun invoke(): StateFlow<AppPreferences> = repository.preferences
}

class ChangeLanguageUseCase(private val repository: PreferencesRepository) {
    operator fun invoke(languageTag: String) = repository.setLanguage(languageTag)
}

class ChangeThemeModeUseCase(private val repository: PreferencesRepository) {
    operator fun invoke(mode: ThemeMode) = repository.setThemeMode(mode)
}
