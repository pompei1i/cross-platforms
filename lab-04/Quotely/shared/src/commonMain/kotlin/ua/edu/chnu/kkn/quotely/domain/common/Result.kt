package ua.edu.chnu.kkn.quotely.domain.common

/** Outcome of a data operation: either the data or a typed [DataError] the UI can describe to the user. */
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Failure(val error: DataError) : Result<Nothing>
}

sealed interface DataError {
    data object NoInternet : DataError
    data object Timeout : DataError
    data object InvalidCredentials : DataError

    /** 401 that could not be fixed by refreshing the tokens: the session is over. */
    data object Unauthorized : DataError
    data object Serialization : DataError
    data class Server(val code: Int) : DataError
    data class Http(val code: Int, val message: String?) : DataError
    data class Unknown(val message: String?) : DataError
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onFailure(action: (DataError) -> Unit): Result<T> {
    if (this is Result.Failure) action(error)
    return this
}

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Failure -> this
}

inline fun <T> Result<T>.mapError(transform: (DataError) -> DataError): Result<T> = when (this) {
    is Result.Success -> this
    is Result.Failure -> Result.Failure(transform(error))
}
