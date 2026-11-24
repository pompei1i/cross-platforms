package ua.edu.chnu.kkn.quotely.ui.common

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.window.core.layout.WindowSizeClass
import kotlin.math.min
import kotlin.math.roundToInt

enum class WindowWidth { Compact, Medium, Expanded }

/** What the screens need to know about the window, derived from its [WindowSizeClass]. */
@Immutable
data class WindowLayout(
    val width: WindowWidth,
    /** Phones in landscape: under 480dp of height. */
    val isHeightCompact: Boolean,
) {
    /** Content goes side by side: tablets, desktop windows and phones in landscape. */
    val useTwoPanes: Boolean get() = width != WindowWidth.Compact || isHeightCompact
}

fun WindowSizeClass.toWindowLayout(): WindowLayout = WindowLayout(
    width = when {
        isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> WindowWidth.Expanded
        isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> WindowWidth.Medium
        else -> WindowWidth.Compact
    },
    isHeightCompact = !isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND),
)

/** Current [WindowLayout]; recomposes when the window is resized or the device is rotated. */
@Composable
fun currentWindowLayout(): WindowLayout = currentWindowAdaptiveInfo().windowSizeClass.toWindowLayout()

/**
 * Lays [content] out at its natural size and shrinks it uniformly when it doesn't fit the
 * available space (illustrations on short landscape screens). Never scales up.
 */
@Composable
fun ScaleDownToFit(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val placeable = measurables.single().measure(Constraints())
        val scaleX = if (constraints.hasBoundedWidth && placeable.width > 0) constraints.maxWidth.toFloat() / placeable.width else 1f
        val scaleY = if (constraints.hasBoundedHeight && placeable.height > 0) constraints.maxHeight.toFloat() / placeable.height else 1f
        val scale = min(1f, min(scaleX, scaleY)).coerceAtLeast(0f)
        val width = (placeable.width * scale).roundToInt().coerceIn(constraints.minWidth, constraints.maxWidth)
        val height = (placeable.height * scale).roundToInt().coerceIn(constraints.minHeight, constraints.maxHeight)
        layout(width, height) {
            placeable.placeWithLayer(
                x = (width - placeable.width) / 2,
                y = (height - placeable.height) / 2,
            ) {
                this.scaleX = scale
                this.scaleY = scale
            }
        }
    }
}
