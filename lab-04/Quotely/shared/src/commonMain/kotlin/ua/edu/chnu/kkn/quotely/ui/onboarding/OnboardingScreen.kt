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
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.Dp
import ua.edu.chnu.kkn.quotely.ui.common.ScaleDownToFit
import ua.edu.chnu.kkn.quotely.ui.common.WindowLayout
import ua.edu.chnu.kkn.quotely.ui.common.WindowWidth
import ua.edu.chnu.kkn.quotely.ui.common.currentWindowLayout
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ua.edu.chnu.kkn.quotely.resources.Res
import ua.edu.chnu.kkn.quotely.resources.ic_arrow_back
import ua.edu.chnu.kkn.quotely.resources.ic_arrow_forward
import ua.edu.chnu.kkn.quotely.resources.onboarding_back
import ua.edu.chnu.kkn.quotely.resources.onboarding_get_started
import ua.edu.chnu.kkn.quotely.resources.onboarding_next
import ua.edu.chnu.kkn.quotely.resources.onboarding_page_counter
import ua.edu.chnu.kkn.quotely.resources.onboarding_skip
import ua.edu.chnu.kkn.quotely.ui.common.ObserveEvents
import ua.edu.chnu.kkn.quotely.ui.components.LanguageSwitcher
import kotlin.math.absoluteValue

/** Wires [OnboardingScreen] to its [OnboardingViewModel]. */
@Composable
fun OnboardingRoute(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.events) { event ->
        when (event) {
            OnboardingEvent.NavigateToLogin -> onFinished()
        }
    }
    OnboardingScreen(
        state = state,
        onAction = viewModel::onAction,
        currentLanguage = currentLanguage,
        onLanguageChange = onLanguageChange,
    )
}

@Composable
fun OnboardingScreen(
    state: OnboardingState,
    onAction: (OnboardingAction) -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    pages: List<OnboardingPage> = onboardingPages,
    layout: WindowLayout = currentWindowLayout(),
) {
    // The ViewModel keeps the page, so it survives a language switch and configuration changes
    val pagerState = rememberPagerState(initialPage = state.currentPage, pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex
    val horizontalPadding = if (layout.width == WindowWidth.Compact) 20.dp else 40.dp

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onAction(OnboardingAction.PageChanged(page))
        }
    }

    val goBack: () -> Unit = { coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }
    val goNext: () -> Unit = {
        if (isLastPage) {
            onAction(OnboardingAction.GetStarted)
        } else {
            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .displayCutoutPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                // Desktop and large tablets: keep lines readable instead of stretching across the screen
                .widthIn(max = 1100.dp)
                .padding(vertical = if (layout.isHeightCompact) 8.dp else 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top bar: language switch + skip
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LanguageSwitcher(
                    currentLanguage = currentLanguage,
                    onLanguageChange = onLanguageChange,
                    compact = true,
                )
                Spacer(Modifier.weight(1f))
                TextButton(
                    onClick = { onAction(OnboardingAction.Skip) },
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
                    layout = layout,
                    horizontalPadding = horizontalPadding,
                )
            }

            if (layout.isHeightCompact) {
                // Landscape phone: every pixel of height counts, so the indicator moves into the button row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BackButton(visible = pagerState.currentPage > 0, onClick = goBack)
                    Spacer(Modifier.weight(1f))
                    PageProgress(currentPage = pagerState.currentPage, pageCount = pages.size)
                    Spacer(Modifier.weight(1f))
                    NextButton(isLastPage = isLastPage, onClick = goNext)
                }
            } else {
                PageProgress(currentPage = pagerState.currentPage, pageCount = pages.size)
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .then(if (layout.width == WindowWidth.Compact) Modifier.fillMaxWidth() else Modifier.widthIn(max = 560.dp))
                        .padding(horizontal = horizontalPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BackButton(visible = pagerState.currentPage > 0, onClick = goBack)
                    Spacer(Modifier.width(16.dp))
                    if (!isLastPage) {
                        Spacer(Modifier.weight(1f))
                    }
                    NextButton(
                        isLastPage = isLastPage,
                        onClick = goNext,
                        modifier = if (isLastPage) Modifier.weight(1f) else Modifier,
                    )
                }
            }
        }
    }
}

@Composable
private fun PageProgress(currentPage: Int, pageCount: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(Res.string.onboarding_page_counter, currentPage + 1, pageCount),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        PagerIndicator(pageCount = pageCount, currentPage = currentPage)
    }
}

@Composable
private fun BackButton(visible: Boolean, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        enabled = visible,
        modifier = Modifier.alpha(if (visible) 1f else 0f),
    ) {
        Icon(painterResource(Res.drawable.ic_arrow_back), null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(stringResource(Res.string.onboarding_back))
    }
}

@Composable
private fun NextButton(isLastPage: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        contentPadding = ButtonDefaults.ContentPadding,
        modifier = modifier.height(52.dp),
    ) {
        Text(stringResource(if (isLastPage) Res.string.onboarding_get_started else Res.string.onboarding_next))
        Spacer(Modifier.width(8.dp))
        Icon(painterResource(Res.drawable.ic_arrow_forward), null, modifier = Modifier.size(18.dp))
    }
}

/** Signed distance of [page] from the current scroll position: 0 when centered, ±1 one page away. */
private fun PagerState.offsetForPage(page: Int): Float =
    (currentPage - page) + currentPageOffsetFraction

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    pageOffset: Float,
    layout: WindowLayout,
    horizontalPadding: Dp,
) {
    val fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
    val pageModifier = Modifier
        .fillMaxSize()
        .padding(horizontal = horizontalPadding + 8.dp)
        .graphicsLayer {
            // Fade and shrink the pages that are moving off screen
            alpha = lerp(0.2f, 1f, fraction)
            val scale = lerp(0.85f, 1f, fraction)
            scaleX = scale
            scaleY = scale
        }
    // Parallax: the illustration lags behind the text
    val parallax = Modifier.graphicsLayer { translationX = pageOffset * size.width * 0.35f }

    if (layout.useTwoPanes) {
        // Landscape phones, tablets, desktop: illustration and text side by side
        Row(
            modifier = pageModifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (layout.isHeightCompact) 24.dp else 48.dp),
        ) {
            ScaleDownToFit(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(vertical = 8.dp)
                    .then(parallax),
            ) {
                OnboardingIllustrationView(page.illustration)
            }
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            ) {
                PageTexts(page, textAlign = TextAlign.Start, large = layout.width == WindowWidth.Expanded)
                Spacer(Modifier.height(if (layout.isHeightCompact) 12.dp else 24.dp))
                OnboardingExtraContent(page.illustration)
            }
        }
    } else {
        Column(
            modifier = pageModifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Shrinks on short phones instead of pushing the text off screen
            ScaleDownToFit(modifier = Modifier.weight(1f, fill = false).then(parallax)) {
                OnboardingIllustrationView(page.illustration)
            }
            Spacer(Modifier.height(32.dp))
            PageTexts(page, textAlign = TextAlign.Center, large = false)
            Spacer(Modifier.height(24.dp))
            OnboardingExtraContent(page.illustration)
        }
    }
}

@Composable
private fun PageTexts(page: OnboardingPage, textAlign: TextAlign, large: Boolean) {
    Text(
        text = stringResource(page.title),
        style = if (large) MaterialTheme.typography.displaySmall else MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = textAlign,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(12.dp))
    Text(
        text = stringResource(page.text),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = textAlign,
        modifier = Modifier.fillMaxWidth(),
    )
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
