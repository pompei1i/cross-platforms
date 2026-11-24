package ua.edu.chnu.kkn.quotely.domain.onboarding

/** Saves that the user went through (or skipped) the onboarding. */
class CompleteOnboardingUseCase(private val repository: OnboardingRepository) {
    operator fun invoke() = repository.setOnboardingCompleted(true)
}

/** Tells whether the onboarding was already passed, so it is not shown on the next launch. */
class IsOnboardingCompletedUseCase(private val repository: OnboardingRepository) {
    operator fun invoke(): Boolean = repository.isOnboardingCompleted()
}

/** Makes the onboarding show up again (the "Show onboarding again" setting). */
class ResetOnboardingUseCase(private val repository: OnboardingRepository) {
    operator fun invoke() = repository.setOnboardingCompleted(false)
}
