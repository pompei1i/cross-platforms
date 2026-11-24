package ua.edu.chnu.kkn.quotely.data.quotes

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable
import ua.edu.chnu.kkn.quotely.data.remote.safeRequest
import ua.edu.chnu.kkn.quotely.domain.common.Result
import ua.edu.chnu.kkn.quotely.domain.common.map
import ua.edu.chnu.kkn.quotely.domain.quotes.Quote
import ua.edu.chnu.kkn.quotely.domain.quotes.QuotesPage
import ua.edu.chnu.kkn.quotely.domain.quotes.QuotesRepository

internal const val QUOTES_PATH = "quotes"

@Serializable
internal data class QuoteResponse(
    val id: Int,
    val quote: String,
    val author: String,
)

/** `GET quotes?limit=&skip=` → `{"quotes": [...], "total": 1454, "skip": 0, "limit": 20}` */
@Serializable
internal data class QuotesPageResponse(
    val quotes: List<QuoteResponse>,
    val total: Int,
    val skip: Int,
    val limit: Int,
)

internal interface QuotesApiService {
    suspend fun getQuotes(limit: Int, skip: Int): Result<QuotesPageResponse>
    suspend fun getQuote(id: Int): Result<QuoteResponse>
}

internal class AppQuotesApiService(private val client: HttpClient) : QuotesApiService {

    override suspend fun getQuotes(limit: Int, skip: Int): Result<QuotesPageResponse> = safeRequest {
        client.get(QUOTES_PATH) {
            parameter("limit", limit)
            parameter("skip", skip)
        }
    }

    override suspend fun getQuote(id: Int): Result<QuoteResponse> = safeRequest {
        client.get("$QUOTES_PATH/$id")
    }
}

internal class AppQuotesRepository(private val apiService: QuotesApiService) : QuotesRepository {

    override suspend fun getQuotesPage(limit: Int, skip: Int): Result<QuotesPage> =
        apiService.getQuotes(limit, skip).map { page ->
            QuotesPage(quotes = page.quotes.map { it.toQuote() }, skip = page.skip, total = page.total)
        }

    override suspend fun getQuote(id: Int): Result<Quote> = apiService.getQuote(id).map { it.toQuote() }
}

private fun QuoteResponse.toQuote() = Quote(id = id, text = quote, author = author)
