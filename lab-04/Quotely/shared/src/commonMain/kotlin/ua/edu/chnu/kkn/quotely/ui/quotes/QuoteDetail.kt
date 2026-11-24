package ua.edu.chnu.kkn.quotely.ui.quotes

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ua.edu.chnu.kkn.quotely.domain.common.DataError
import ua.edu.chnu.kkn.quotely.domain.common.onFailure
import ua.edu.chnu.kkn.quotely.domain.common.onSuccess
import ua.edu.chnu.kkn.quotely.domain.quotes.GetQuoteUseCase
import ua.edu.chnu.kkn.quotely.domain.quotes.Quote
import ua.edu.chnu.kkn.quotely.resources.Res
import ua.edu.chnu.kkn.quotely.resources.common_back
import ua.edu.chnu.kkn.quotely.resources.common_retry
import ua.edu.chnu.kkn.quotely.resources.ic_arrow_back
import ua.edu.chnu.kkn.quotely.resources.ic_format_quote
import ua.edu.chnu.kkn.quotely.resources.quote_error
import ua.edu.chnu.kkn.quotely.resources.quote_number
import ua.edu.chnu.kkn.quotely.resources.quote_placeholder_text
import ua.edu.chnu.kkn.quotely.resources.quote_placeholder_title
import ua.edu.chnu.kkn.quotely.ui.common.text
import ua.edu.chnu.kkn.quotely.ui.theme.loraFontFamily

// ---- MVI ----

@Immutable
data class QuoteDetailState(
    val quote: Quote? = null,
    val isLoading: Boolean = false,
    val error: DataError? = null,
)

sealed interface QuoteDetailAction {
    data object Retry : QuoteDetailAction
}

/** Loads one quote by id, so the detail pane also works when opened on its own (e.g. after process death). */
class QuoteDetailViewModel(
    private val quoteId: Int,
    private val getQuote: GetQuoteUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(QuoteDetailState())
    val state: StateFlow<QuoteDetailState> = _state.asStateFlow()

    init {
        load()
    }

    fun onAction(action: QuoteDetailAction) {
        when (action) {
            QuoteDetailAction.Retry -> load()
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getQuote(quoteId)
                .onSuccess { quote -> _state.update { it.copy(quote = quote) } }
                .onFailure { error -> _state.update { it.copy(error = error) } }
            _state.update { it.copy(isLoading = false) }
        }
    }
}

// ---- UI ----

@Composable
fun QuoteDetailRoute(
    quoteId: Int,
    onBack: () -> Unit,
    viewModel: QuoteDetailViewModel = koinViewModel { parametersOf(quoteId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    QuoteDetailScreen(quoteId = quoteId, state = state, onAction = viewModel::onAction, onBack = onBack)
}

@Composable
fun QuoteDetailScreen(
    quoteId: Int,
    state: QuoteDetailState,
    onAction: (QuoteDetailAction) -> Unit,
    onBack: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(color = colors.surfaceContainerLow, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().systemBarsPadding().padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(painterResource(Res.drawable.ic_arrow_back), stringResource(Res.string.common_back))
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(Res.string.quote_number, quoteId),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurfaceVariant,
                )
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                val quote = state.quote
                when {
                    quote != null -> QuoteBody(quote)
                    state.error != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(Res.string.quote_error), style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = state.error.text(),
                            color = colors.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                        Spacer(Modifier.height(14.dp))
                        FilledTonalButton(onClick = { onAction(QuoteDetailAction.Retry) }) {
                            Text(stringResource(Res.string.common_retry))
                        }
                    }
                    else -> CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun QuoteBody(quote: Quote) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .widthIn(max = 640.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape).background(colors.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(Res.drawable.ic_format_quote), null, tint = colors.secondary, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(28.dp))
        Text(
            text = quote.text,
            style = TextStyle(
                fontFamily = loraFontFamily(),
                fontStyle = FontStyle.Italic,
                fontSize = 26.sp,
                lineHeight = 36.sp,
            ),
            textAlign = TextAlign.Center,
            color = colors.onSurface,
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "— ${quote.author}",
            style = MaterialTheme.typography.titleMedium,
            color = colors.secondary,
        )
    }
}

/** Shown in the detail pane of the two-pane layout until a quote is picked. */
@Composable
fun QuoteDetailPlaceholder() {
    val colors = MaterialTheme.colorScheme
    Surface(color = colors.surfaceContainerLow, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_format_quote),
                contentDescription = null,
                tint = colors.outlineVariant,
                modifier = Modifier.size(96.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text(stringResource(Res.string.quote_placeholder_title), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(Res.string.quote_placeholder_text),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
