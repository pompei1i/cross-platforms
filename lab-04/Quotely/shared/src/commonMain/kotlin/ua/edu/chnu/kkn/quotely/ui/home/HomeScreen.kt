package ua.edu.chnu.kkn.quotely.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ua.edu.chnu.kkn.quotely.resources.common_retry
import ua.edu.chnu.kkn.quotely.resources.home_greeting
import ua.edu.chnu.kkn.quotely.resources.home_greeting_loading
import ua.edu.chnu.kkn.quotely.resources.home_profile_error
import ua.edu.chnu.kkn.quotely.resources.home_signed_in_as
import ua.edu.chnu.kkn.quotely.resources.ic_logout
import ua.edu.chnu.kkn.quotely.resources.settings_sign_out
import ua.edu.chnu.kkn.quotely.ui.common.ObserveEvents
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ua.edu.chnu.kkn.quotely.resources.Res
import ua.edu.chnu.kkn.quotely.resources.home_coming_soon
import ua.edu.chnu.kkn.quotely.resources.home_placeholder_text
import ua.edu.chnu.kkn.quotely.resources.home_placeholder_title
import ua.edu.chnu.kkn.quotely.resources.home_subtitle
import ua.edu.chnu.kkn.quotely.resources.home_title
import ua.edu.chnu.kkn.quotely.resources.ic_auto_awesome
import ua.edu.chnu.kkn.quotely.resources.ic_contrast
import ua.edu.chnu.kkn.quotely.resources.ic_dark_mode
import ua.edu.chnu.kkn.quotely.resources.ic_format_quote
import ua.edu.chnu.kkn.quotely.resources.ic_history
import ua.edu.chnu.kkn.quotely.resources.ic_language
import ua.edu.chnu.kkn.quotely.resources.ic_light_mode
import ua.edu.chnu.kkn.quotely.resources.ic_palette
import ua.edu.chnu.kkn.quotely.resources.settings_language
import ua.edu.chnu.kkn.quotely.resources.settings_show_onboarding
import ua.edu.chnu.kkn.quotely.resources.settings_subtitle
import ua.edu.chnu.kkn.quotely.resources.settings_theme
import ua.edu.chnu.kkn.quotely.resources.settings_theme_dark
import ua.edu.chnu.kkn.quotely.resources.settings_theme_light
import ua.edu.chnu.kkn.quotely.resources.settings_theme_system
import ua.edu.chnu.kkn.quotely.resources.settings_title
import ua.edu.chnu.kkn.quotely.domain.settings.ThemeMode
import ua.edu.chnu.kkn.quotely.ui.components.LanguageSwitcher
import ua.edu.chnu.kkn.quotely.ui.components.PillOption
import ua.edu.chnu.kkn.quotely.ui.components.PillSegmentedControl

/** Wires [HomeScreen] to its [HomeViewModel]. */
@Composable
fun HomeRoute(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onShowOnboarding: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.events) { event ->
        when (event) {
            HomeEvent.NavigateToLogin -> onSignedOut()
        }
    }
    HomeScreen(
        state = state,
        onAction = viewModel::onAction,
        currentLanguage = currentLanguage,
        onLanguageChange = onLanguageChange,
        themeMode = themeMode,
        onThemeModeChange = onThemeModeChange,
        onShowOnboarding = onShowOnboarding,
    )
}

/** Home page shown after a successful sign-in: profile greeting, a placeholder for the quotes feed and settings. */
@Composable
fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onShowOnboarding: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Text(
            text = stringResource(Res.string.home_title),
            style = MaterialTheme.typography.displaySmall,
            color = colors.primary,
        )
        Text(
            text = stringResource(Res.string.home_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        ProfileHeader(state = state, onRetry = { onAction(HomeAction.RetryProfile) })
        Spacer(Modifier.height(20.dp))

        PlaceholderCard()

        Spacer(Modifier.height(36.dp))
        Text(stringResource(Res.string.settings_title), style = MaterialTheme.typography.headlineSmall)
        Text(
            text = stringResource(Res.string.settings_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = colors.outlineVariant)
        Spacer(Modifier.height(20.dp))

        SettingLabel(Res.drawable.ic_language, stringResource(Res.string.settings_language))
        LanguageSwitcher(
            currentLanguage = currentLanguage,
            onLanguageChange = onLanguageChange,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(20.dp))

        SettingLabel(Res.drawable.ic_palette, stringResource(Res.string.settings_theme))
        PillSegmentedControl(
            options = listOf(
                PillOption(ThemeMode.System, stringResource(Res.string.settings_theme_system), Res.drawable.ic_contrast),
                PillOption(ThemeMode.Light, stringResource(Res.string.settings_theme_light), Res.drawable.ic_light_mode),
                PillOption(ThemeMode.Dark, stringResource(Res.string.settings_theme_dark), Res.drawable.ic_dark_mode),
            ),
            selected = themeMode,
            onSelect = onThemeModeChange,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(28.dp))

        OutlinedButton(
            onClick = onShowOnboarding,
            border = BorderStroke(1.dp, colors.primary),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Icon(painterResource(Res.drawable.ic_history), null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(stringResource(Res.string.settings_show_onboarding))
        }
        Spacer(Modifier.height(12.dp))
        TextButton(
            onClick = { onAction(HomeAction.SignOut) },
            colors = ButtonDefaults.textButtonColors(contentColor = colors.error),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Icon(painterResource(Res.drawable.ic_logout), null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(stringResource(Res.string.settings_sign_out))
        }
    }
}

@Composable
private fun ProfileHeader(state: HomeState, onRetry: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val user = state.user
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(52.dp).clip(CircleShape).background(colors.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            if (state.isLoading && user == null) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text(
                    text = user?.displayName?.take(1)?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onPrimaryContainer,
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = if (user != null) stringResource(Res.string.home_greeting, user.displayName)
                else stringResource(Res.string.home_greeting_loading),
                style = MaterialTheme.typography.titleLarge,
            )
            when {
                user != null -> Text(
                    text = stringResource(Res.string.home_signed_in_as, user.username),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
                state.error != null -> Text(
                    text = stringResource(Res.string.home_profile_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.error,
                )
            }
        }
        if (state.error != null && !state.isLoading) {
            TextButton(onClick = onRetry) { Text(stringResource(Res.string.common_retry)) }
        }
    }
}

@Composable
private fun PlaceholderCard() {
    val colors = MaterialTheme.colorScheme
    Surface(
        color = colors.secondaryContainer,
        contentColor = colors.onSecondaryContainer,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box {
            // Large faded quote mark in the corner
            Icon(
                painter = painterResource(Res.drawable.ic_format_quote),
                contentDescription = null,
                tint = colors.secondary.copy(alpha = 0.12f),
                modifier = Modifier.align(Alignment.BottomEnd).size(140.dp).offset(x = 16.dp, y = 28.dp),
            )
            Column(Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(colors.secondary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(painterResource(Res.drawable.ic_auto_awesome), null, tint = colors.secondary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.surface.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(colors.secondary))
                        Text(stringResource(Res.string.home_coming_soon), style = MaterialTheme.typography.labelMedium)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text(stringResource(Res.string.home_placeholder_title), style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(6.dp))
                Text(stringResource(Res.string.home_placeholder_text), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun SettingLabel(icon: DrawableResource, text: String) {
    Row(
        modifier = Modifier.padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(icon), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}
