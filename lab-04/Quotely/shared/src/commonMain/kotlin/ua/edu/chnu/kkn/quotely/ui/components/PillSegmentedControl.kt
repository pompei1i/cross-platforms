package ua.edu.chnu.kkn.quotely.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import ua.edu.chnu.kkn.quotely.resources.Res
import ua.edu.chnu.kkn.quotely.resources.ic_check

data class PillOption<T>(
    val value: T,
    val label: String,
    val icon: DrawableResource? = null,
)

/**
 * Segmented control from the Stitch design: a pill-shaped track with a filled pill
 * behind the selected option. [compact] sizes segments to their content (onboarding top bar),
 * otherwise segments share the full width equally.
 */
@Composable
fun <T> PillSegmentedControl(
    options: List<PillOption<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    showCheckOnSelected: Boolean = false,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(4.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEach { option ->
            val isSelected = option.value == selected
            val containerColor by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
            )
            val contentColor by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            val icon = option.icon ?: if (showCheckOnSelected && isSelected) Res.drawable.ic_check else null

            Row(
                modifier = Modifier
                    .then(if (compact) Modifier else Modifier.weight(1f))
                    .clip(CircleShape)
                    .background(containerColor)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(option.value) })
                    .padding(
                        horizontal = if (compact) 14.dp else 8.dp,
                        vertical = if (compact) 6.dp else 10.dp,
                    ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                }
                val style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge
                Text(
                    text = option.label,
                    color = contentColor,
                    style = style,
                    maxLines = 1,
                    // Longer Ukrainian labels shrink a little instead of being cut off on narrow screens
                    autoSize = TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = style.fontSize),
                )
            }
        }
    }
}
