package ua.edu.chnu.kkn.quotely.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ua.edu.chnu.kkn.quotely.data.onboarding.AppOnboardingRepository
import ua.edu.chnu.kkn.quotely.data.settings.AppPreferencesRepository
import ua.edu.chnu.kkn.quotely.data.settings.AppSettings
import ua.edu.chnu.kkn.quotely.domain.auth.validation.ValidatePasswordUseCase
import ua.edu.chnu.kkn.quotely.domain.auth.validation.ValidateUsernameUseCase
import ua.edu.chnu.kkn.quotely.domain.onboarding.CompleteOnboardingUseCase
import ua.edu.chnu.kkn.quotely.domain.onboarding.IsOnboardingCompletedUseCase
import ua.edu.chnu.kkn.quotely.domain.onboarding.OnboardingRepository
import ua.edu.chnu.kkn.quotely.domain.onboarding.ResetOnboardingUseCase
import ua.edu.chnu.kkn.quotely.domain.settings.ChangeLanguageUseCase
import ua.edu.chnu.kkn.quotely.domain.settings.ChangeThemeModeUseCase
import ua.edu.chnu.kkn.quotely.domain.settings.ObservePreferencesUseCase
import ua.edu.chnu.kkn.quotely.domain.settings.PreferencesRepository
import ua.edu.chnu.kkn.quotely.ui.login.LoginViewModel
import ua.edu.chnu.kkn.quotely.ui.main.MainViewModel
import ua.edu.chnu.kkn.quotely.ui.onboarding.OnboardingViewModel

val dataModule = module {
    single { AppSettings() }
    singleOf(::AppOnboardingRepository) { bind<OnboardingRepository>() }
    singleOf(::AppPreferencesRepository) { bind<PreferencesRepository>() }
}

val domainModule = module {
    factoryOf(::CompleteOnboardingUseCase)
    factoryOf(::IsOnboardingCompletedUseCase)
    factoryOf(::ResetOnboardingUseCase)
    factoryOf(::ObservePreferencesUseCase)
    factoryOf(::ChangeLanguageUseCase)
    factoryOf(::ChangeThemeModeUseCase)
    factoryOf(::ValidateUsernameUseCase)
    factoryOf(::ValidatePasswordUseCase)
}

val presentationModule = module {
    viewModelOf(::MainViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::LoginViewModel)
}

val appModules = listOf(dataModule, domainModule, presentationModule)
