package ua.edu.chnu.kkn.quotely.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ua.edu.chnu.kkn.quotely.domain.common.DataError
import ua.edu.chnu.kkn.quotely.domain.quotes.Quote
import ua.edu.chnu.kkn.quotely.resources.Res
import ua.edu.chnu.kkn.quotely.resources.app_name
import ua.edu.chnu.kkn.quotely.resources.common_retry
import ua.edu.chnu.kkn.quotely.resources.home_refresh
import ua.edu.chnu.kkn.quotely.resources.home_subtitle
import ua.edu.chnu.kkn.quotely.resources.ic_format_quote
import ua.edu.chnu.kkn.quotely.resources.ic_refresh
import ua.edu.chnu.kkn.quotely.resources.ic_settings
import ua.edu.chnu.kkn.quotely.resources.quotes_empty
import ua.edu.chnu.kkn.quotely.resources.quotes_end
import ua.edu.chnu.kkn.quotely.resources.quotes_error_title
import ua.edu.chnu.kkn.quotely.resources.settings_title
import ua.edu.chnu.kkn.quotely.ui.common.text
import ua.edu.chnu.kkn.quotely.ui.quotes.QuotesLoadException
import ua.edu.chnu.kkn.quotely.ui.theme.loraFontFamily

/** Wires [HomeScreen] to its [HomeViewModel]. */
@Composable
fun HomeRoute(
    selectedQuoteId: Int?,
    onQuoteClick: (Int) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    HomeScreen(
        quotes = viewModel.quotes.collectAsLazyPagingItems(),
        selectedQuoteId = selectedQuoteId,
        onQuoteClick = onQuoteClick,
        onOpenSettings = onOpenSettings,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    quotes: LazyPagingItems<Quote>,
    selectedQuoteId: Int?,
    onQuoteClick: (Int) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.titleLarge, color = colors.primary)
                        Text(
                            text = stringResource(Res.string.home_subtitle),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    // Reload from the first page
                    IconButton(onClick = { quotes.refresh() }) {
                        Icon(painterResource(Res.drawable.ic_refresh), stringResource(Res.string.home_refresh))
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(painterResource(Res.drawable.ic_settings), stringResource(Res.string.settings_title))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background),
            )
        },
    ) { innerPadding ->
        val refresh = quotes.loadState.refresh
        PullToRefreshBox(
            isRefreshing = refresh is LoadState.Loading && quotes.itemCount > 0,
            onRefresh = { quotes.refresh() },
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            when {
                refresh is LoadState.Loading && quotes.itemCount == 0 -> CenteredBox { CircularProgressIndicator() }

                refresh is LoadState.Error && quotes.itemCount == 0 -> CenteredBox {
                    LoadError(
                        title = stringResource(Res.string.quotes_error_title),
                        error = refresh.error.toDataError(),
                        onRetry = { quotes.retry() },
                    )
                }

                refresh is LoadState.NotLoading && quotes.itemCount == 0 -> CenteredBox {
                    Text(stringResource(Res.string.quotes_empty), color = colors.onSurfaceVariant)
                }

                else -> QuotesList(quotes = quotes, selectedQuoteId = selectedQuoteId, onQuoteClick = onQuoteClick)
            }
        }
    }
}

@Composable
private fun QuotesList(
    quotes: LazyPagingItems<Quote>,
    selectedQuoteId: Int?,
    onQuoteClick: (Int) -> Unit,
) {
    LazyColumn(
        state = rememberLazyListState(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        // A refresh failed while old items are still shown: keep them, offer a retry on top
        val refresh = quotes.loadState.refresh
        if (refresh is LoadState.Error) {
            item(key = "refresh-error", contentType = "status") {
                LoadError(
                    title = stringResource(Res.string.quotes_error_title),
                    error = refresh.error.toDataError(),
                    onRetry = { quotes.retry() },
                )
            }
        }

        items(
            count = quotes.itemCount,
            key = quotes.itemKey { it.id },
            contentType = quotes.itemContentType { "quote" },
        ) { index ->
            val quote = quotes[index] ?: return@items
            QuoteCard(
                quote = quote,
                selected = quote.id == selectedQuoteId,
                onClick = { onQuoteClick(quote.id) },
            )
        }

        when (val append = quotes.loadState.append) {
            is LoadState.Loading -> item(key = "append-loading", contentType = "status") {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp).size(28.dp), strokeWidth = 3.dp)
            }
            is LoadState.Error -> item(key = "append-error", contentType = "status") {
                LoadError(
                    title = stringResource(Res.string.quotes_error_title),
                    error = append.error.toDataError(),
                    onRetry = { quotes.retry() },
                )
            }
            is LoadState.NotLoading -> if (append.endOfPaginationReached && quotes.itemCount > 0) {
                item(key = "end", contentType = "status") {
                    Text(
                        text = stringResource(Res.string.quotes_end),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun QuoteCard(quote: Quote, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = if (selected) colors.secondaryContainer else colors.surfaceContainerLow,
        contentColor = if (selected) colors.onSecondaryContainer else colors.onSurface,
        border = if (selected) BorderStroke(1.dp, colors.secondary) else null,
        modifier = Modifier.fillMaxWidth().widthIn(max = 720.dp),
    ) {
        Row(modifier = Modifier.padding(20.dp)) {
            Icon(
                painter = painterResource(Res.drawable.ic_format_quote),
                contentDescription = null,
                tint = colors.secondary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = quote.text,
                    style = TextStyle(fontFamily = loraFontFamily(), fontSize = 17.sp, lineHeight = 25.sp),
                    maxLines = 5,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "— ${quote.author}",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) colors.onSecondaryContainer else colors.secondary,
                )
            }
        }
    }
}

@Composable
internal fun LoadError(title: String, error: DataError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp).widthIn(max = 420.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            text = error.text(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(14.dp))
        FilledTonalButton(onClick = onRetry) { Text(stringResource(Res.string.common_retry)) }
    }
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}

private fun Throwable.toDataError(): DataError = (this as? QuotesLoadException)?.error ?: DataError.Unknown(message)
