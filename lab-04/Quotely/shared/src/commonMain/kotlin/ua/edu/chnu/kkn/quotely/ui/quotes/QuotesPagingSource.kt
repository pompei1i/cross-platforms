package ua.edu.chnu.kkn.quotely.ui.quotes

import androidx.paging.PagingSource
import androidx.paging.PagingState
import co.touchlab.kermit.Logger
import ua.edu.chnu.kkn.quotely.domain.common.DataError
import ua.edu.chnu.kkn.quotely.domain.common.Result
import ua.edu.chnu.kkn.quotely.domain.quotes.GetQuotesPageUseCase
import ua.edu.chnu.kkn.quotely.domain.quotes.Quote

private val log = Logger.withTag("QuotesPaging")

/** Key = `skip` offset of the page in the dummyjson.com quotes collection. */
class QuotesPagingSource(
    private val getQuotesPage: GetQuotesPageUseCase,
) : PagingSource<Int, Quote>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Quote> {
        val skip = params.key ?: FIRST_PAGE_SKIP
        log.d { "Loading ${params.loadSize} quotes from $skip" }
        return when (val result = getQuotesPage(limit = params.loadSize, skip = skip)) {
            is Result.Success -> LoadResult.Page(
                data = result.data.quotes,
                prevKey = null, // only forward paging: refresh always restarts from the top
                nextKey = result.data.nextSkip,
            )
            is Result.Failure -> LoadResult.Error(QuotesLoadException(result.error))
        }
    }

    /** `null` restarts the list from the first page on refresh(). */
    override fun getRefreshKey(state: PagingState<Int, Quote>): Int? = null

    companion object {
        const val FIRST_PAGE_SKIP = 0
    }
}

/** Carries the typed [DataError] through Paging, so the UI can show a localized message. */
class QuotesLoadException(val error: DataError) : Exception(error.toString())
