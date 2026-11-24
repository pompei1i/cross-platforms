package ua.edu.chnu.kkn.quotely.domain.onboarding

interface OnboardingRepository {
    fun isOnboardingCompleted(): Boolean
    fun setOnboardingCompleted(completed: Boolean)
}
