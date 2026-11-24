package ua.edu.chnu.kkn.quotely.data.remote

import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.ContentConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import ua.edu.chnu.kkn.quotely.data.auth.model.ErrorResponse
import ua.edu.chnu.kkn.quotely.domain.common.DataError
import ua.edu.chnu.kkn.quotely.domain.common.Result

/** Runs a request and turns every outcome (HTTP status, missing network, bad JSON) into a [Result]. */
internal suspend inline fun <reified T> safeRequest(block: () -> HttpResponse): Result<T> {
    val response = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (_: HttpRequestTimeoutException) {
        return Result.Failure(DataError.Timeout)
    } catch (_: IOException) {
        return Result.Failure(DataError.NoInternet)
    } catch (e: Exception) {
        return Result.Failure(DataError.Unknown(e.message))
    }
    return response.toResult()
}

internal suspend inline fun <reified T> HttpResponse.toResult(): Result<T> {
    val code = status.value
    return when {
        code in 200..299 -> try {
            Result.Success(body<T>())
        } catch (_: SerializationException) {
            Result.Failure(DataError.Serialization)
        } catch (_: ContentConvertException) {
            Result.Failure(DataError.Serialization)
        }
        code == 401 -> Result.Failure(DataError.Unauthorized)
        code >= 500 -> Result.Failure(DataError.Server(code))
        else -> Result.Failure(DataError.Http(code, errorMessage()))
    }
}

internal suspend fun HttpResponse.errorMessage(): String? = try {
    body<ErrorResponse>().message
} catch (_: Exception) {
    null
}
