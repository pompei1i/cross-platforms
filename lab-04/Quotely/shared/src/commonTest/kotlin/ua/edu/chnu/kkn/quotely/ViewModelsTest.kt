package ua.edu.chnu.kkn.quotely

import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import ua.edu.chnu.kkn.quotely.data.security.AuthTokens
import ua.edu.chnu.kkn.quotely.data.security.TokenStorage
import ua.edu.chnu.kkn.quotely.data.settings.AppSettings
import ua.edu.chnu.kkn.quotely.domain.common.DataError
import ua.edu.chnu.kkn.quotely.ui.home.HomeAction
import ua.edu.chnu.kkn.quotely.ui.home.HomeEvent
import ua.edu.chnu.kkn.quotely.ui.home.HomeViewModel
import ua.edu.chnu.kkn.quotely.di.appModules
import ua.edu.chnu.kkn.quotely.domain.auth.validation.ValidationError
import ua.edu.chnu.kkn.quotely.domain.onboarding.OnboardingRepository
import ua.edu.chnu.kkn.quotely.domain.settings.ThemeMode
import ua.edu.chnu.kkn.quotely.ui.login.LoginAction
import ua.edu.chnu.kkn.quotely.ui.login.LoginEvent
import ua.edu.chnu.kkn.quotely.ui.login.LoginField
import ua.edu.chnu.kkn.quotely.ui.login.LoginViewModel
import ua.edu.chnu.kkn.quotely.ui.main.MainAction
import ua.edu.chnu.kkn.quotely.ui.main.MainViewModel
import ua.edu.chnu.kkn.quotely.ui.navigation.Route
import ua.edu.chnu.kkn.quotely.ui.onboarding.OnboardingAction
import ua.edu.chnu.kkn.quotely.ui.onboarding.OnboardingEvent
import ua.edu.chnu.kkn.quotely.ui.onboarding.OnboardingViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Runs the real Koin graph with in-memory settings. */
@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelsTest : KoinTest {

    private val settings = AppSettings(MapSettings())
    private val server = FakeDummyJson()
    private val tokenStorage = InMemoryTokenStorage()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        startKoin {
            allowOverride(true)
            modules(
                appModules + module {
                    single { settings }
                    single<TokenStorage> { tokenStorage }
                    single<HttpClientEngine> { server.engine }
                }
            )
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    private val onboardingRepository: OnboardingRepository by inject()
    private fun mainViewModel(): MainViewModel = getKoin().get()
    private fun onboardingViewModel(): OnboardingViewModel = getKoin().get()
    private fun loginViewModel(): LoginViewModel = getKoin().get()

    @Test
    fun startsWithOnboardingOnFreshInstall() {
        assertEquals(listOf<Route>(Route.Onboarding), mainViewModel().backStack.toList())
    }

    @Test
    fun startsWithLoginWhenOnboardingWasPassed() {
        settings.isOnboardingCompleted = true
        assertEquals(listOf<Route>(Route.Login), mainViewModel().backStack.toList())
    }

    @Test
    fun startsAtHomeWhenSignedIn() {
        settings.isOnboardingCompleted = true
        tokenStorage.tokens = AuthTokens("access-1", "refresh-1")
        assertEquals(listOf<Route>(Route.Home), mainViewModel().backStack.toList())
    }

    @Test
    fun replayedOnboardingReturnsSignedInUserHome() {
        tokenStorage.tokens = AuthTokens("access-1", "refresh-1")
        val viewModel = mainViewModel()
        viewModel.onAction(MainAction.OnboardingFinished)
        assertEquals(listOf<Route>(Route.Home), viewModel.backStack.toList())
    }

    @Test
    fun onboardingSavesResultAndNavigatesToLogin() = runTest {
        val viewModel = onboardingViewModel()
        viewModel.onAction(OnboardingAction.PageChanged(2))
        assertEquals(2, viewModel.state.value.currentPage)

        viewModel.onAction(OnboardingAction.Skip)
        assertEquals(OnboardingEvent.NavigateToLogin, viewModel.events.first())
        assertTrue(onboardingRepository.isOnboardingCompleted())
    }

    @Test
    fun mainNavigationReplacesTheBackStack() {
        val viewModel = mainViewModel()
        viewModel.onAction(MainAction.OnboardingFinished)
        assertEquals(listOf<Route>(Route.Login), viewModel.backStack.toList())
        viewModel.onAction(MainAction.SignedIn)
        assertEquals(listOf<Route>(Route.Home), viewModel.backStack.toList())

        viewModel.onAction(MainAction.ShowOnboardingAgain)
        assertEquals(listOf<Route>(Route.Onboarding), viewModel.backStack.toList())
        assertEquals(false, settings.isOnboardingCompleted)
    }

    @Test
    fun preferencesArePersisted() {
        val viewModel = mainViewModel()
        viewModel.onAction(MainAction.ChangeLanguage("uk"))
        viewModel.onAction(MainAction.ChangeThemeMode(ThemeMode.Dark))
        assertEquals("uk", viewModel.state.value.language)
        assertEquals(ThemeMode.Dark, viewModel.state.value.themeMode)
        assertEquals("uk", settings.language)
        assertEquals(ThemeMode.Dark, settings.themeMode)
    }

    @Test
    fun loginShowsErrorsAndFocusesFirstInvalidField() = runTest {
        val viewModel = loginViewModel()
        viewModel.onAction(LoginAction.PasswordChanged("123"))
        viewModel.onAction(LoginAction.SignIn)

        assertEquals(ValidationError.Empty, viewModel.state.value.usernameError)
        assertEquals(ValidationError.TooShort(6), viewModel.state.value.passwordError)
        assertEquals(LoginEvent.FocusField(LoginField.Username), viewModel.events.first())

        // The shown error goes away as soon as the value is fixed
        viewModel.onAction(LoginAction.UsernameChanged("emilys"))
        assertNull(viewModel.state.value.usernameError)
    }

    @Test
    fun loginWithValidCredentialsNavigatesHome() = runTest {
        val viewModel = loginViewModel()
        viewModel.onAction(LoginAction.UsernameChanged("emilys"))
        viewModel.onAction(LoginAction.PasswordChanged("emilyspass"))
        viewModel.onAction(LoginAction.SignIn)
        assertEquals(LoginEvent.NavigateToHome, viewModel.events.first())
        assertEquals("refresh-1", tokenStorage.tokens?.refreshToken)
    }

    @Test
    fun loginWithWrongPasswordShowsError() = runTest {
        val viewModel = loginViewModel()
        viewModel.onAction(LoginAction.UsernameChanged("emilys"))
        viewModel.onAction(LoginAction.PasswordChanged("wrongpass"))
        viewModel.onAction(LoginAction.SignIn)
        assertEquals(LoginEvent.ShowError(DataError.InvalidCredentials), viewModel.events.first())
        assertNull(tokenStorage.tokens)
    }

    @Test
    fun homeLoadsProfileAndSignsOut() = runTest {
        tokenStorage.tokens = AuthTokens(server.validAccessToken, server.validRefreshToken)
        val viewModel: HomeViewModel = getKoin().get()
        viewModel.state.first { it.user != null }
        assertEquals("Emily", viewModel.state.value.user?.firstName)

        viewModel.onAction(HomeAction.SignOut)
        assertEquals(HomeEvent.NavigateToLogin, viewModel.events.first())
        assertNull(tokenStorage.tokens)
    }
}
