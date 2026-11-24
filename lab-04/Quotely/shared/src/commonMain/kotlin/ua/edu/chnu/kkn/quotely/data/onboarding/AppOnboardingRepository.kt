package ua.edu.chnu.kkn.quotely.data.onboarding

import ua.edu.chnu.kkn.quotely.data.settings.AppSettings
import ua.edu.chnu.kkn.quotely.domain.onboarding.OnboardingRepository

internal class AppOnboardingRepository(
    private val settings: AppSettings,
) : OnboardingRepository {

    override fun isOnboardingCompleted(): Boolean = settings.isOnboardingCompleted

    override fun setOnboardingCompleted(completed: Boolean) {
        settings.isOnboardingCompleted = completed
    }
}
