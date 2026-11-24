package ua.edu.chnu.kkn.quotely.ui.onboarding

import org.jetbrains.compose.resources.StringResource
import ua.edu.chnu.kkn.quotely.resources.Res
import ua.edu.chnu.kkn.quotely.resources.onboarding_explore_text
import ua.edu.chnu.kkn.quotely.resources.onboarding_explore_title
import ua.edu.chnu.kkn.quotely.resources.onboarding_favorites_text
import ua.edu.chnu.kkn.quotely.resources.onboarding_favorites_title
import ua.edu.chnu.kkn.quotely.resources.onboarding_random_text
import ua.edu.chnu.kkn.quotely.resources.onboarding_random_title
import ua.edu.chnu.kkn.quotely.resources.onboarding_welcome_text
import ua.edu.chnu.kkn.quotely.resources.onboarding_welcome_title

/** Illustration (and the extra content under the text) of each slide, as designed in Stitch. */
enum class OnboardingIllustration { QuoteCard, Explore, Random, Favorites }

data class OnboardingPage(
    val title: StringResource,
    val text: StringResource,
    val illustration: OnboardingIllustration,
)

val onboardingPages = listOf(
    OnboardingPage(
        title = Res.string.onboarding_welcome_title,
        text = Res.string.onboarding_welcome_text,
        illustration = OnboardingIllustration.QuoteCard,
    ),
    OnboardingPage(
        title = Res.string.onboarding_explore_title,
        text = Res.string.onboarding_explore_text,
        illustration = OnboardingIllustration.Explore,
    ),
    OnboardingPage(
        title = Res.string.onboarding_random_title,
        text = Res.string.onboarding_random_text,
        illustration = OnboardingIllustration.Random,
    ),
    OnboardingPage(
        title = Res.string.onboarding_favorites_title,
        text = Res.string.onboarding_favorites_text,
        illustration = OnboardingIllustration.Favorites,
    ),
)
