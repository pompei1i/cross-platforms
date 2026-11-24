package ua.edu.chnu.kkn.quotely.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ua.edu.chnu.kkn.quotely.data.auth.AppAuthApiService
import ua.edu.chnu.kkn.quotely.data.auth.AppAuthRepository
import ua.edu.chnu.kkn.quotely.data.auth.AuthApiService
import ua.edu.chnu.kkn.quotely.data.remote.createHttpClient
import ua.edu.chnu.kkn.quotely.data.security.KSafeTokenStorage
import ua.edu.chnu.kkn.quotely.data.security.SessionEvents
import ua.edu.chnu.kkn.quotely.data.security.TokenStorage
import ua.edu.chnu.kkn.quotely.domain.auth.AuthRepository
import ua.edu.chnu.kkn.quotely.domain.auth.GetCurrentUserUseCase
import ua.edu.chnu.kkn.quotely.domain.auth.IsAuthorizedUseCase
import ua.edu.chnu.kkn.quotely.domain.auth.LoginUseCase
import ua.edu.chnu.kkn.quotely.domain.auth.LogoutUseCase
import ua.edu.chnu.kkn.quotely.domain.auth.ObserveSessionExpiredUseCase
import ua.edu.chnu.kkn.quotely.data.quotes.AppQuotesApiService
import ua.edu.chnu.kkn.quotely.data.quotes.AppQuotesRepository
import ua.edu.chnu.kkn.quotely.data.quotes.QuotesApiService
import ua.edu.chnu.kkn.quotely.domain.quotes.GetQuoteUseCase
import ua.edu.chnu.kkn.quotely.domain.quotes.GetQuotesPageUseCase
import ua.edu.chnu.kkn.quotely.domain.quotes.QuotesRepository
import ua.edu.chnu.kkn.quotely.ui.home.HomeViewModel
import ua.edu.chnu.kkn.quotely.ui.quotes.QuoteDetailViewModel
import ua.edu.chnu.kkn.quotely.ui.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
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

val networkModule = module {
    singleOf(::SessionEvents)
    single<TokenStorage> { KSafeTokenStorage(get()) }
    single { createHttpClient(engine = get(), tokenStorage = get(), sessionEvents = get()) }
    singleOf(::AppAuthApiService) { bind<AuthApiService>() }
    singleOf(::AppAuthRepository) { bind<AuthRepository>() }
    singleOf(::AppQuotesApiService) { bind<QuotesApiService>() }
    singleOf(::AppQuotesRepository) { bind<QuotesRepository>() }
}

val domainModule = module {
    factoryOf(::LoginUseCase)
    factoryOf(::LogoutUseCase)
    factoryOf(::IsAuthorizedUseCase)
    factoryOf(::GetCurrentUserUseCase)
    factoryOf(::ObserveSessionExpiredUseCase)
    factoryOf(::GetQuotesPageUseCase)
    factoryOf(::GetQuoteUseCase)
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
    viewModelOf(::HomeViewModel)
    viewModelOf(::SettingsViewModel)
    viewModel { params -> QuoteDetailViewModel(quoteId = params.get(), getQuote = get()) }
}

val appModules = listOf(platformModule, dataModule, networkModule, domainModule, presentationModule)
