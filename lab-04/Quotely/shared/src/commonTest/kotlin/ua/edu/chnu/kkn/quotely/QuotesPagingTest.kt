package ua.edu.chnu.kkn.quotely

import androidx.paging.PagingSource
import kotlinx.coroutines.test.runTest
import ua.edu.chnu.kkn.quotely.data.quotes.AppQuotesApiService
import ua.edu.chnu.kkn.quotely.data.quotes.AppQuotesRepository
import ua.edu.chnu.kkn.quotely.data.remote.createHttpClient
import ua.edu.chnu.kkn.quotely.data.security.SessionEvents
import ua.edu.chnu.kkn.quotely.domain.common.DataError
import ua.edu.chnu.kkn.quotely.domain.common.Result
import ua.edu.chnu.kkn.quotely.domain.quotes.GetQuotesPageUseCase
import ua.edu.chnu.kkn.quotely.ui.quotes.QuotesLoadException
import ua.edu.chnu.kkn.quotely.ui.quotes.QuotesPagingSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class QuotesPagingTest {

    private val server = FakeDummyJson().apply { totalQuotes = 45 }
    private val repository = AppQuotesRepository(
        AppQuotesApiService(createHttpClient(server.engine, InMemoryTokenStorage(), SessionEvents()))
    )
    private val pagingSource = QuotesPagingSource(GetQuotesPageUseCase(repository))

    private suspend fun load(key: Int?, loadSize: Int = 20) = pagingSource.load(
        PagingSource.LoadParams.Refresh(key = key, loadSize = loadSize, placeholdersEnabled = false)
    )

    @Test
    fun firstPageStartsAtZeroAndPointsToTheNextOne() = runTest {
        val page = assertIs<PagingSource.LoadResult.Page<Int, *>>(load(key = null))
        assertEquals(20, page.data.size)
        assertNull(page.prevKey)
        assertEquals(20, page.nextKey)
    }

    @Test
    fun lastPageHasNoNextKey() = runTest {
        val page = assertIs<PagingSource.LoadResult.Page<Int, *>>(load(key = 40))
        assertEquals(5, page.data.size)
        assertNull(page.nextKey)
    }

    @Test
    fun errorsBecomeLoadResultError() = runTest {
        server.failQuotes = true
        val error = assertIs<PagingSource.LoadResult.Error<Int, *>>(load(key = null))
        assertEquals(DataError.Server(503), assertIs<QuotesLoadException>(error.throwable).error)
    }

    @Test
    fun refreshKeyRestartsFromTheTop() {
        assertNull(pagingSource.getRefreshKey(androidx.paging.PagingState(emptyList(), null, androidx.paging.PagingConfig(20), 0)))
    }

    @Test
    fun singleQuoteAndMissingQuote() = runTest {
        assertEquals("Quote 5", assertIs<Result.Success<ua.edu.chnu.kkn.quotely.domain.quotes.Quote>>(repository.getQuote(5)).data.text)
        assertIs<DataError.Http>(assertIs<Result.Failure>(repository.getQuote(999)).error)
    }
}
