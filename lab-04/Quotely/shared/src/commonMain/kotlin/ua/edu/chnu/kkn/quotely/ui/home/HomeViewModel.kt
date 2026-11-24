package ua.edu.chnu.kkn.quotely.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import ua.edu.chnu.kkn.quotely.domain.quotes.GetQuotesPageUseCase
import ua.edu.chnu.kkn.quotely.domain.quotes.Quote
import ua.edu.chnu.kkn.quotely.ui.quotes.QuotesPagingSource

/** Home page: the endless, lazily loaded list of quotes. */
class HomeViewModel(
    getQuotesPage: GetQuotesPageUseCase,
) : ViewModel() {

    /** Cached in the ViewModel, so pages survive rotation and returning from the detail screen. */
    val quotes: Flow<PagingData<Quote>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            initialLoadSize = INITIAL_LOAD_SIZE,
            prefetchDistance = PREFETCH_DISTANCE,
            enablePlaceholders = false,
        ),
        // A new PagingSource for every refresh(): an invalidated source can't be reused
        pagingSourceFactory = { QuotesPagingSource(getQuotesPage) },
    ).flow.cachedIn(viewModelScope)

    companion object {
        const val PAGE_SIZE = 20
        const val INITIAL_LOAD_SIZE = PAGE_SIZE * 2
        const val PREFETCH_DISTANCE = 5
    }
}
