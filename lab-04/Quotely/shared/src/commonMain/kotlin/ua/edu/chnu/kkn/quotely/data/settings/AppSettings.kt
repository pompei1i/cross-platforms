package ua.edu.chnu.kkn.quotely.data.settings

import com.russhwolf.settings.Settings
import com.russhwolf.settings.boolean
import com.russhwolf.settings.nullableString
import com.russhwolf.settings.string
import ua.edu.chnu.kkn.quotely.domain.settings.ThemeMode

/**
 * User preferences persisted with multiplatform-settings
 * (SharedPreferences on Android, NSUserDefaults on iOS, java.util.prefs on desktop).
 * Only repositories of the data layer talk to it directly.
 */
class AppSettings(settings: Settings = Settings()) {

    var isOnboardingCompleted: Boolean by settings.boolean(KEY_ONBOARDING_COMPLETED, defaultValue = false)

    /** Language tag ("en", "uk") chosen by the user, or `null` to follow the system language. */
    var language: String? by settings.nullableString(KEY_LANGUAGE)

    private var themeModeName: String by settings.string(KEY_THEME_MODE, defaultValue = ThemeMode.System.name)

    var themeMode: ThemeMode
        get() = ThemeMode.entries.firstOrNull { it.name == themeModeName } ?: ThemeMode.System
        set(value) {
            themeModeName = value.name
        }

    private companion object {
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        const val KEY_LANGUAGE = "app_language"
        const val KEY_THEME_MODE = "theme_mode"
    }
}
