package ua.edu.chnu.kkn.quotely

import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import ua.edu.chnu.kkn.quotely.data.settings.AppSettings
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

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        startKoin {
            allowOverride(true)
            modules(appModules + module { single { settings } })
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
    }
}
