package ua.edu.chnu.kkn.quotely.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ua.edu.chnu.kkn.quotely.locale.SupportedLanguages
import ua.edu.chnu.kkn.quotely.resources.Res
import ua.edu.chnu.kkn.quotely.resources.language_en
import ua.edu.chnu.kkn.quotely.resources.language_uk

/**
 * EN / UK switch.
 * [compact] shows short codes (for the onboarding top bar) instead of full language names.
 */
@Composable
fun LanguageSwitcher(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    PillSegmentedControl(
        options = SupportedLanguages.map { language ->
            PillOption(
                value = language,
                label = if (compact) language.uppercase() else stringResource(languageName(language)),
            )
        },
        selected = currentLanguage,
        onSelect = onLanguageChange,
        modifier = modifier,
        compact = compact,
        showCheckOnSelected = !compact,
    )
}

private fun languageName(language: String): StringResource = when (language) {
    "uk" -> Res.string.language_uk
    else -> Res.string.language_en
}
