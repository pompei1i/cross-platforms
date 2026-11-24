package ua.edu.chnu.kkn.quotely.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import ua.edu.chnu.kkn.quotely.resources.Res
import ua.edu.chnu.kkn.quotely.resources.lora_italic
import ua.edu.chnu.kkn.quotely.resources.lora_regular
import ua.edu.chnu.kkn.quotely.resources.lora_semibold
import ua.edu.chnu.kkn.quotely.resources.manrope_bold
import ua.edu.chnu.kkn.quotely.resources.manrope_medium
import ua.edu.chnu.kkn.quotely.resources.manrope_regular

/** Serif family for headings and quotes. */
@Composable
fun loraFontFamily() = FontFamily(
    Font(Res.font.lora_regular, FontWeight.Normal),
    Font(Res.font.lora_semibold, FontWeight.SemiBold),
    Font(Res.font.lora_italic, FontWeight.Normal, FontStyle.Italic),
)

/** Sans-serif family for body text and UI labels. */
@Composable
fun manropeFontFamily() = FontFamily(
    Font(Res.font.manrope_regular, FontWeight.Normal),
    Font(Res.font.manrope_medium, FontWeight.Medium),
    Font(Res.font.manrope_bold, FontWeight.Bold),
)

@Composable
fun quotelyTypography(): Typography {
    val serif = loraFontFamily()
    val sans = manropeFontFamily()
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = serif, fontWeight = FontWeight.SemiBold),
        displayMedium = base.displayMedium.copy(fontFamily = serif, fontWeight = FontWeight.SemiBold),
        displaySmall = base.displaySmall.copy(fontFamily = serif, fontWeight = FontWeight.SemiBold),
        headlineLarge = base.headlineLarge.copy(fontFamily = serif, fontWeight = FontWeight.SemiBold),
        headlineMedium = base.headlineMedium.copy(fontFamily = serif, fontWeight = FontWeight.SemiBold),
        headlineSmall = base.headlineSmall.copy(fontFamily = serif, fontWeight = FontWeight.SemiBold),
        titleLarge = base.titleLarge.copy(fontFamily = serif, fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontFamily = sans, fontWeight = FontWeight.Bold),
        titleSmall = base.titleSmall.copy(fontFamily = sans, fontWeight = FontWeight.Bold),
        bodyLarge = base.bodyLarge.copy(fontFamily = sans),
        bodyMedium = base.bodyMedium.copy(fontFamily = sans),
        bodySmall = base.bodySmall.copy(fontFamily = sans),
        labelLarge = base.labelLarge.copy(fontFamily = sans, fontWeight = FontWeight.Bold),
        labelMedium = base.labelMedium.copy(fontFamily = sans, fontWeight = FontWeight.Medium),
        labelSmall = base.labelSmall.copy(fontFamily = sans, fontWeight = FontWeight.Medium),
    )
}
