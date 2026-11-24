package ua.edu.chnu.kkn.quotely.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ua.edu.chnu.kkn.quotely.resources.Res
import ua.edu.chnu.kkn.quotely.resources.ic_auto_awesome
import ua.edu.chnu.kkn.quotely.resources.ic_bookmark
import ua.edu.chnu.kkn.quotely.resources.ic_explore
import ua.edu.chnu.kkn.quotely.resources.ic_favorite
import ua.edu.chnu.kkn.quotely.resources.ic_format_quote
import ua.edu.chnu.kkn.quotely.resources.ic_shuffle
import ua.edu.chnu.kkn.quotely.resources.onboarding_daily_insight
import ua.edu.chnu.kkn.quotely.resources.onboarding_sample_author
import ua.edu.chnu.kkn.quotely.resources.onboarding_sample_quote
import ua.edu.chnu.kkn.quotely.resources.onboarding_saved_caption
import ua.edu.chnu.kkn.quotely.resources.onboarding_saved_quote
import ua.edu.chnu.kkn.quotely.resources.onboarding_tag_existentialism
import ua.edu.chnu.kkn.quotely.resources.onboarding_tag_poetry
import ua.edu.chnu.kkn.quotely.resources.onboarding_tag_stoicism

private val CircleSize = 184.dp

@Composable
fun OnboardingIllustrationView(illustration: OnboardingIllustration) {
    val colors = MaterialTheme.colorScheme
    when (illustration) {
        OnboardingIllustration.QuoteCard -> SampleQuoteCard()

        OnboardingIllustration.Explore -> GlowingCircle(colors.secondaryContainer) {
            Icon(painterResource(Res.drawable.ic_explore), null, tint = colors.secondary, modifier = Modifier.size(80.dp))
        }

        OnboardingIllustration.Random -> GlowingCircle(
            color = colors.tertiaryContainer,
            badges = {
                SmallBadge(
                    icon = Res.drawable.ic_auto_awesome,
                    containerColor = colors.surfaceContainerHigh,
                    contentColor = colors.secondary,
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = 10.dp),
                )
                SmallBadge(
                    icon = Res.drawable.ic_format_quote,
                    containerColor = colors.secondaryContainer,
                    contentColor = colors.onSecondaryContainer,
                    modifier = Modifier.align(Alignment.BottomStart).offset(x = (-2).dp, y = (-14).dp),
                )
            },
        ) {
            Icon(painterResource(Res.drawable.ic_shuffle), null, tint = colors.tertiary, modifier = Modifier.size(80.dp))
        }

        OnboardingIllustration.Favorites -> GlowingCircle(
            color = colors.primaryContainer,
            badges = {
                SmallBadge(
                    icon = Res.drawable.ic_favorite,
                    containerColor = colors.secondaryContainer,
                    contentColor = colors.onSecondaryContainer,
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-8).dp, y = (-12).dp),
                )
            },
        ) {
            Box(
                modifier = Modifier.size(88.dp).clip(CircleShape).background(colors.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(Res.drawable.ic_bookmark), null, tint = colors.onPrimary, modifier = Modifier.size(40.dp))
            }
        }
    }
}

/** Extra content shown under the slide description. */
@Composable
fun OnboardingExtraContent(illustration: OnboardingIllustration) {
    when (illustration) {
        OnboardingIllustration.Explore -> TopicTags()
        OnboardingIllustration.Favorites -> SavedQuotePreview()
        else -> Unit
    }
}

/** Tinted circle with a paper-colored ring and a soft radial glow behind it. */
@Composable
private fun GlowingCircle(
    color: Color,
    badges: @Composable BoxScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .size(CircleSize + 96.dp)
            .background(Brush.radialGradient(listOf(color.copy(alpha = 0.55f), Color.Transparent))),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(CircleSize)) {
            Box(
                modifier = Modifier
                    .size(CircleSize)
                    .clip(CircleShape)
                    .background(color)
                    .border(6.dp, MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape),
                contentAlignment = Alignment.Center,
                content = content,
            )
            badges()
        }
    }
}

@Composable
private fun SmallBadge(
    icon: DrawableResource,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(containerColor)
            .border(3.dp, MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icon), null, tint = contentColor, modifier = Modifier.size(size / 2))
    }
}

@Composable
private fun SampleQuoteCard() {
    val colors = MaterialTheme.colorScheme
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.primaryContainer, contentColor = colors.onPrimaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.widthIn(max = 360.dp),
    ) {
        Column(Modifier.padding(28.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(colors.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painterResource(Res.drawable.ic_format_quote),
                        null,
                        tint = colors.onPrimary,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(Res.string.onboarding_daily_insight).uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                    color = colors.primary,
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(Res.string.onboarding_sample_quote),
                style = MaterialTheme.typography.titleLarge.copy(fontStyle = FontStyle.Italic, lineHeight = 32.sp),
            )
            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = colors.onPrimaryContainer.copy(alpha = 0.15f))
            Spacer(Modifier.height(14.dp))
            Text(
                text = "— " + stringResource(Res.string.onboarding_sample_author),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun TopicTags() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TopicTag(stringResource(Res.string.onboarding_tag_stoicism), highlighted = false)
        TopicTag(stringResource(Res.string.onboarding_tag_poetry), highlighted = true)
        TopicTag(stringResource(Res.string.onboarding_tag_existentialism), highlighted = false)
    }
}

@Composable
private fun TopicTag(text: String, highlighted: Boolean) {
    val colors = MaterialTheme.colorScheme
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = if (highlighted) colors.onSecondaryContainer else colors.onSurfaceVariant,
        maxLines = 1,
        modifier = Modifier
            .clip(CircleShape)
            .background(if (highlighted) colors.secondaryContainer else colors.surfaceContainerHigh)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    )
}

@Composable
private fun SavedQuotePreview() {
    val colors = MaterialTheme.colorScheme
    Surface(
        color = colors.surfaceContainerLow,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 2.dp,
        modifier = Modifier.widthIn(max = 320.dp),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(colors.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(Res.drawable.ic_format_quote), null, tint = colors.primary, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(Res.string.onboarding_saved_quote),
                    style = MaterialTheme.typography.titleLarge.copy(fontStyle = FontStyle.Italic, fontSize = 15.sp, lineHeight = 20.sp),
                    color = colors.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(Res.string.onboarding_saved_caption),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}
