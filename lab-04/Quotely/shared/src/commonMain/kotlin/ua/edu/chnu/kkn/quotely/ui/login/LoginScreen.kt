package ua.edu.chnu.kkn.quotely.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ua.edu.chnu.kkn.quotely.domain.auth.validation.ValidationError
import ua.edu.chnu.kkn.quotely.resources.Res
import ua.edu.chnu.kkn.quotely.resources.ic_format_quote
import ua.edu.chnu.kkn.quotely.resources.ic_lock
import ua.edu.chnu.kkn.quotely.resources.ic_person
import ua.edu.chnu.kkn.quotely.resources.ic_visibility
import ua.edu.chnu.kkn.quotely.resources.ic_visibility_off
import ua.edu.chnu.kkn.quotely.resources.login_hide_password
import ua.edu.chnu.kkn.quotely.resources.login_password
import ua.edu.chnu.kkn.quotely.resources.login_show_password
import ua.edu.chnu.kkn.quotely.resources.login_sign_in
import ua.edu.chnu.kkn.quotely.resources.login_subtitle
import ua.edu.chnu.kkn.quotely.resources.login_title
import ua.edu.chnu.kkn.quotely.resources.login_username
import ua.edu.chnu.kkn.quotely.resources.validation_password_empty
import ua.edu.chnu.kkn.quotely.resources.validation_password_too_long
import ua.edu.chnu.kkn.quotely.resources.validation_password_too_short
import ua.edu.chnu.kkn.quotely.resources.validation_password_whitespace
import ua.edu.chnu.kkn.quotely.resources.validation_username_empty
import ua.edu.chnu.kkn.quotely.resources.validation_username_invalid
import ua.edu.chnu.kkn.quotely.resources.validation_username_too_long
import ua.edu.chnu.kkn.quotely.resources.validation_username_too_short
import ua.edu.chnu.kkn.quotely.ui.common.ObserveEvents
import ua.edu.chnu.kkn.quotely.ui.components.LanguageSwitcher

/** Wires [LoginScreen] to its [LoginViewModel]. */
@Composable
fun LoginRoute(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    onSignedIn: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequesters = remember { LoginFocusRequesters() }

    ObserveEvents(viewModel.events) { event ->
        when (event) {
            is LoginEvent.FocusField -> focusRequesters[event.field].requestFocus()
            LoginEvent.NavigateToHome -> onSignedIn()
        }
    }

    LoginScreen(
        state = state,
        onAction = viewModel::onAction,
        currentLanguage = currentLanguage,
        onLanguageChange = onLanguageChange,
        snackbarHostState = snackbarHostState,
        focusRequesters = focusRequesters,
    )
}

class LoginFocusRequesters {
    val username = FocusRequester()
    val password = FocusRequester()

    operator fun get(field: LoginField): FocusRequester = when (field) {
        LoginField.Username -> username
        LoginField.Password -> password
    }
}

@Composable
fun LoginScreen(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    focusRequesters: LoginFocusRequesters = remember { LoginFocusRequesters() },
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                LanguageSwitcher(
                    currentLanguage = currentLanguage,
                    onLanguageChange = onLanguageChange,
                    compact = true,
                )
            }
            Spacer(Modifier.height(40.dp))
            Column(
                modifier = Modifier.widthIn(max = 440.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LoginHeader()
                Spacer(Modifier.height(32.dp))
                LoginForm(state = state, onAction = onAction, focusRequesters = focusRequesters)
            }
        }
    }
}

@Composable
internal fun LoginHeader(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(72.dp).clip(CircleShape).background(colors.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_format_quote),
                contentDescription = null,
                tint = colors.secondary,
                modifier = Modifier.size(36.dp),
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(Res.string.login_title),
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.login_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun LoginForm(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
    focusRequesters: LoginFocusRequesters,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val signIn = {
        focusManager.clearFocus() // hides the soft keyboard
        onAction(LoginAction.SignIn)
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = state.username,
            onValueChange = { onAction(LoginAction.UsernameChanged(it)) },
            label = { Text(stringResource(Res.string.login_username)) },
            leadingIcon = { Icon(painterResource(Res.drawable.ic_person), contentDescription = null) },
            isError = state.usernameError != null,
            supportingText = state.usernameError?.let { error -> { Text(usernameErrorText(error)) } },
            singleLine = true,
            enabled = !state.isLoading,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Next,
            ),
            // Keyboard "Next" moves straight to the password field
            keyboardActions = KeyboardActions(onNext = { focusRequesters.password.requestFocus() }),
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequesters.username),
        )

        OutlinedTextField(
            value = state.password,
            onValueChange = { onAction(LoginAction.PasswordChanged(it)) },
            label = { Text(stringResource(Res.string.login_password)) },
            leadingIcon = { Icon(painterResource(Res.drawable.ic_lock), contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { onAction(LoginAction.TogglePasswordVisibility) }) {
                    Icon(
                        painter = painterResource(
                            if (state.isPasswordVisible) Res.drawable.ic_visibility_off else Res.drawable.ic_visibility
                        ),
                        contentDescription = stringResource(
                            if (state.isPasswordVisible) Res.string.login_hide_password else Res.string.login_show_password
                        ),
                    )
                }
            },
            isError = state.passwordError != null,
            supportingText = state.passwordError?.let { error -> { Text(passwordErrorText(error)) } },
            singleLine = true,
            enabled = !state.isLoading,
            shape = MaterialTheme.shapes.medium,
            visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            // Keyboard "Done" signs in, exactly like the button
            keyboardActions = KeyboardActions(onDone = { signIn() }),
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequesters.password),
        )

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = signIn,
            enabled = state.isSignInEnabled,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(stringResource(Res.string.login_sign_in))
            }
        }
    }
}

@Composable
private fun usernameErrorText(error: ValidationError): String = when (error) {
    ValidationError.Empty -> stringResource(Res.string.validation_username_empty)
    is ValidationError.TooShort -> stringResource(Res.string.validation_username_too_short, error.minLength)
    is ValidationError.TooLong -> stringResource(Res.string.validation_username_too_long, error.maxLength)
    ValidationError.InvalidCharacters,
    ValidationError.ContainsWhitespace -> stringResource(Res.string.validation_username_invalid)
}

@Composable
private fun passwordErrorText(error: ValidationError): String = when (error) {
    ValidationError.Empty -> stringResource(Res.string.validation_password_empty)
    is ValidationError.TooShort -> stringResource(Res.string.validation_password_too_short, error.minLength)
    is ValidationError.TooLong -> stringResource(Res.string.validation_password_too_long, error.maxLength)
    ValidationError.ContainsWhitespace,
    ValidationError.InvalidCharacters -> stringResource(Res.string.validation_password_whitespace)
}
