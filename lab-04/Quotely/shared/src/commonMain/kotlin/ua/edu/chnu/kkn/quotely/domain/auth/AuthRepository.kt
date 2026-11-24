package ua.edu.chnu.kkn.quotely.domain.auth

import kotlinx.coroutines.flow.Flow
import ua.edu.chnu.kkn.quotely.domain.common.Result

data class User(
    val id: Int,
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
) {
    val displayName: String get() = firstName.ifBlank { username }
}

interface AuthRepository {
    /** Signs in and stores the received tokens securely on the device. */
    suspend fun login(username: String, password: String): Result<User>

    /** Forgets the tokens. */
    suspend fun logout()

    /** `true` while a refresh token is stored. */
    suspend fun isAuthorized(): Boolean

    /** An authorized request; tokens are refreshed automatically when the access token expires. */
    suspend fun getCurrentUser(): Result<User>

    /** Emits when the tokens could not be refreshed and were dropped, so the user has to sign in again. */
    val sessionExpired: Flow<Unit>
}
