package ua.edu.chnu.kkn.quotely.ui.navigation

/** Destinations of the Navigation 3 back stack. */
sealed interface Route {
    data object Onboarding : Route
    data object Login : Route

    /** Quotes list: the list pane of the list-detail layout. */
    data object Home : Route

    /** One quote: the detail pane next to [Home] on wide windows, a full screen on phones. */
    data class QuoteDetail(val quoteId: Int) : Route
    data object Settings : Route
}
