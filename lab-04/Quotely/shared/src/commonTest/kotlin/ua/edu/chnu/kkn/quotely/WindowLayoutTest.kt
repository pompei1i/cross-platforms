package ua.edu.chnu.kkn.quotely

import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.computeWindowSizeClass
import ua.edu.chnu.kkn.quotely.ui.common.WindowLayout
import ua.edu.chnu.kkn.quotely.ui.common.WindowWidth
import ua.edu.chnu.kkn.quotely.ui.common.toWindowLayout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WindowLayoutTest {

    private fun layoutFor(widthDp: Int, heightDp: Int): WindowLayout =
        WindowSizeClass.BREAKPOINTS_V1.computeWindowSizeClass(widthDp, heightDp).toWindowLayout()

    @Test
    fun phonePortraitIsSinglePane() {
        val layout = layoutFor(widthDp = 411, heightDp = 891)
        assertEquals(WindowWidth.Compact, layout.width)
        assertFalse(layout.isHeightCompact)
        assertFalse(layout.useTwoPanes)
    }

    @Test
    fun phoneLandscapeIsTwoPanesWithCompactHeight() {
        val layout = layoutFor(widthDp = 891, heightDp = 411)
        assertTrue(layout.isHeightCompact)
        assertTrue(layout.useTwoPanes)
    }

    @Test
    fun tabletAndDesktop() {
        assertEquals(WindowWidth.Medium, layoutFor(widthDp = 700, heightDp = 1000).width)
        assertEquals(WindowWidth.Expanded, layoutFor(widthDp = 1280, heightDp = 800).width)
        assertTrue(layoutFor(widthDp = 1280, heightDp = 800).useTwoPanes)
    }
}
