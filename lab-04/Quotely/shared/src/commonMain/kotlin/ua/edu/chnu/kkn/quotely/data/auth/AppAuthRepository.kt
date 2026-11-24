package ua.edu.chnu.kkn.quotely.data.auth

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import ua.edu.chnu.kkn.quotely.data.auth.model.toUser
import ua.edu.chnu.kkn.quotely.data.security.AuthTokens
import ua.edu.chnu.kkn.quotely.data.security.SessionEvents
import ua.edu.chnu.kkn.quotely.data.security.TokenStorage
import ua.edu.chnu.kkn.quotely.domain.auth.AuthRepository
import ua.edu.chnu.kkn.quotely.domain.auth.User
import ua.edu.chnu.kkn.quotely.domain.common.Result
import ua.edu.chnu.kkn.quotely.domain.common.map

private val log = Logger.withTag("Auth")

internal class AppAuthRepository(
    private val apiService: AuthApiService,
    private val tokenStorage: TokenStorage,
    private val sessionEvents: SessionEvents,
) : AuthRepository {

    override val sessionExpired: Flow<Unit> = sessionEvents.expired

    override suspend fun login(username: String, password: String): Result<User> {
        val result = apiService.login(username, password)
        if (result is Result.Success) {
            tokenStorage.saveTokens(AuthTokens(result.data.accessToken, result.data.refreshToken))
            apiService.resetCachedTokens()
            log.i { "Signed in as ${result.data.username}" }
        }
        return result.map { it.toUser() }
    }

    override suspend fun logout() {
        tokenStorage.clear()
        apiService.resetCachedTokens()
        log.i { "Signed out" }
    }

    override suspend fun isAuthorized(): Boolean = tokenStorage.getTokens() != null

    override suspend fun getCurrentUser(): Result<User> = apiService.getCurrentUser().map { it.toUser() }
}
