package ua.edu.chnu.kkn.quotely.domain.quotes

import ua.edu.chnu.kkn.quotely.domain.common.Result

data class Quote(
    val id: Int,
    val text: String,
    val author: String,
)

/** One page of quotes and what is needed to tell whether more pages exist. */
data class QuotesPage(
    val quotes: List<Quote>,
    val skip: Int,
    val total: Int,
) {
    /** Offset of the next page, or `null` when this page is the last one. */
    val nextSkip: Int?
        get() = (skip + quotes.size).takeIf { quotes.isNotEmpty() && it < total }
}

interface QuotesRepository {
    suspend fun getQuotesPage(limit: Int, skip: Int): Result<QuotesPage>
    suspend fun getQuote(id: Int): Result<Quote>
}

/** Loads a single page; the presentation layer decides how pages are combined. */
class GetQuotesPageUseCase(private val repository: QuotesRepository) {
    suspend operator fun invoke(limit: Int, skip: Int): Result<QuotesPage> = repository.getQuotesPage(limit, skip)
}

class GetQuoteUseCase(private val repository: QuotesRepository) {
    suspend operator fun invoke(id: Int): Result<Quote> = repository.getQuote(id)
}
