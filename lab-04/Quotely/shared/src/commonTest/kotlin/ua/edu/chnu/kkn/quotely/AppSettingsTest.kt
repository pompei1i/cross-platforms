package ua.edu.chnu.kkn.quotely

import com.russhwolf.settings.MapSettings
import ua.edu.chnu.kkn.quotely.data.settings.AppSettings
import ua.edu.chnu.kkn.quotely.domain.settings.ThemeMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppSettingsTest {

    @Test
    fun defaultsForFreshInstall() {
        val settings = AppSettings(MapSettings())
        assertFalse(settings.isOnboardingCompleted)
        assertNull(settings.language)
        assertEquals(ThemeMode.System, settings.themeMode)
    }

    @Test
    fun valuesArePersisted() {
        val storage = MapSettings()
        AppSettings(storage).apply {
            isOnboardingCompleted = true
            language = "uk"
            themeMode = ThemeMode.Dark
        }
        val restored = AppSettings(storage)
        assertTrue(restored.isOnboardingCompleted)
        assertEquals("uk", restored.language)
        assertEquals(ThemeMode.Dark, restored.themeMode)
    }

    @Test
    fun systemLocaleIsMappedToSupportedLanguage() {
        assertEquals("uk", resolveLanguage("uk_UA"))
        assertEquals("en", resolveLanguage("en-US"))
        assertEquals("en", resolveLanguage("de_DE"))
    }
}
