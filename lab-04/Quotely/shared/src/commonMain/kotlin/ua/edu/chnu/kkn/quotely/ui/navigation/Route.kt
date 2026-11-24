package ua.edu.chnu.kkn.quotely.ui.navigation

/** Destinations of the Navigation 3 back stack. */
sealed interface Route {
    data object Onboarding : Route
    data object Login : Route
    data object Home : Route
}
