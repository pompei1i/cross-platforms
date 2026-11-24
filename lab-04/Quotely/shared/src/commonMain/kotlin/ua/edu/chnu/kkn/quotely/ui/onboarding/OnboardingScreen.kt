package ua.edu.chnu.kkn.quotely.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ua.edu.chnu.kkn.quotely.resources.Res
import ua.edu.chnu.kkn.quotely.resources.ic_arrow_back
import ua.edu.chnu.kkn.quotely.resources.ic_arrow_forward
import ua.edu.chnu.kkn.quotely.resources.onboarding_back
import ua.edu.chnu.kkn.quotely.resources.onboarding_get_started
import ua.edu.chnu.kkn.quotely.resources.onboarding_next
import ua.edu.chnu.kkn.quotely.resources.onboarding_page_counter
import ua.edu.chnu.kkn.quotely.resources.onboarding_skip
import ua.edu.chnu.kkn.quotely.ui.components.LanguageSwitcher
import kotlin.math.absoluteValue

private val log = Logger.withTag("Onboarding")

@Composable
fun OnboardingScreen(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    onFinish: () -> Unit,
    initialPage: Int = 0,
    onPageChange: (Int) -> Unit = {},
    pages: List<OnboardingPage> = onboardingPages,
) {
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            log.d { "Page ${page + 1}/${pages.size} shown" }
            onPageChange(page)
        }
    }

    fun finish(skipped: Boolean) {
        log.i { if (skipped) "Onboarding skipped on page ${pagerState.currentPage + 1}" else "Onboarding completed" }
        onFinish()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top bar: language switch + skip
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LanguageSwitcher(
                currentLanguage = currentLanguage,
                onLanguageChange = onLanguageChange,
                compact = true,
            )
            Spacer(Modifier.weight(1f))
            TextButton(
                onClick = { finish(skipped = true) },
                enabled = !isLastPage,
                modifier = Modifier.alpha(if (isLastPage) 0f else 1f),
            ) {
                Text(stringResource(Res.string.onboarding_skip), color = MaterialTheme.colorScheme.onSurface)
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) { pageIndex ->
            OnboardingPageContent(
                page = pages[pageIndex],
                pageOffset = pagerState.offsetForPage(pageIndex),
            )
        }

        Text(
            text = stringResource(Res.string.onboarding_page_counter, pagerState.currentPage + 1, pages.size),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        PagerIndicator(pageCount = pages.size, currentPage = pagerState.currentPage)
        Spacer(Modifier.height(24.dp))

        // Bottom navigation: back / next or get started
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val canGoBack = pagerState.currentPage > 0
            TextButton(
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                enabled = canGoBack,
                modifier = Modifier.alpha(if (canGoBack) 1f else 0f),
            ) {
                Icon(painterResource(Res.drawable.ic_arrow_back), null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(Res.string.onboarding_back))
            }
            Spacer(Modifier.width(16.dp))
            if (!isLastPage) {
                Spacer(Modifier.weight(1f))
            }
            Button(
                onClick = {
                    if (isLastPage) {
                        finish(skipped = false)
                    } else {
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                contentPadding = ButtonDefaults.ContentPadding,
                modifier = Modifier
                    .then(if (isLastPage) Modifier.weight(1f) else Modifier)
                    .height(52.dp),
            ) {
                Text(
                    stringResource(if (isLastPage) Res.string.onboarding_get_started else Res.string.onboarding_next)
                )
                Spacer(Modifier.width(8.dp))
                Icon(painterResource(Res.drawable.ic_arrow_forward), null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

/** Signed distance of [page] from the current scroll position: 0 when centered, ±1 one page away. */
private fun PagerState.offsetForPage(page: Int): Float =
    (currentPage - page) + currentPageOffsetFraction

@Composable
private fun OnboardingPageContent(page: OnboardingPage, pageOffset: Float) {
    val fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp)
            .graphicsLayer {
                // Fade and shrink the pages that are moving off screen
                alpha = lerp(0.2f, 1f, fraction)
                val scale = lerp(0.85f, 1f, fraction)
                scaleX = scale
                scaleY = scale
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.graphicsLayer {
                // Parallax: the illustration lags behind the text
                translationX = pageOffset * size.width * 0.35f
            },
            contentAlignment = Alignment.Center,
        ) {
            OnboardingIllustrationView(page.illustration)
        }
        Spacer(Modifier.height(32.dp))
        Text(
            text = stringResource(page.title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(page.text),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        OnboardingExtraContent(page.illustration)
    }
}

@Composable
private fun PagerIndicator(pageCount: Int, currentPage: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            val width by animateDpAsState(if (selected) 28.dp else 8.dp)
            val color by animateColorAsState(
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
