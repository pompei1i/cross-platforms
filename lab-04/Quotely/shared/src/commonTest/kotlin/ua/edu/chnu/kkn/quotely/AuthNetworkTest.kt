package ua.edu.chnu.kkn.quotely

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import ua.edu.chnu.kkn.quotely.data.auth.AppAuthApiService
import ua.edu.chnu.kkn.quotely.data.auth.AppAuthRepository
import ua.edu.chnu.kkn.quotely.data.remote.createHttpClient
import ua.edu.chnu.kkn.quotely.data.security.AuthTokens
import ua.edu.chnu.kkn.quotely.data.security.SessionEvents
import ua.edu.chnu.kkn.quotely.domain.auth.User
import ua.edu.chnu.kkn.quotely.domain.common.DataError
import ua.edu.chnu.kkn.quotely.domain.common.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthNetworkTest {

    private val server = FakeDummyJson()
    private val storage = InMemoryTokenStorage()
    private val sessionEvents = SessionEvents()
    private val repository = AppAuthRepository(
        apiService = AppAuthApiService(createHttpClient(server.engine, storage, sessionEvents)),
        tokenStorage = storage,
        sessionEvents = sessionEvents,
    )

    @Test
    fun loginStoresTokens() = runTest {
        val result = repository.login("emilys", "emilyspass")

        assertEquals("Emily", assertIs<Result.Success<User>>(result).data.firstName)
        assertEquals(AuthTokens("access-1", "refresh-1"), storage.tokens)
        assertTrue(repository.isAuthorized())
    }

    @Test
    fun wrongPasswordIsInvalidCredentials() = runTest {
        val result = repository.login("emilys", "wrong-pass")

        assertEquals(Result.Failure(DataError.InvalidCredentials), result)
        assertNull(storage.tokens)
    }

    @Test
    fun authorizedRequestSendsBearerToken() = runTest {
        repository.login("emilys", "emilyspass")

        assertIs<Result.Success<*>>(repository.getCurrentUser())
        assertEquals(0, server.refreshCalls)
    }

    @Test
    fun expiredAccessTokenIsRefreshedAutomatically() = runTest {
        repository.login("emilys", "emilyspass")
        repository.getCurrentUser() // the Auth plugin now caches access-1
        server.expireAccessToken()

        val result = repository.getCurrentUser()

        assertIs<Result.Success<*>>(result)
        assertEquals(1, server.refreshCalls)
        assertEquals(AuthTokens("access-2", "refresh-2"), storage.tokens)
    }

    @Test
    fun rejectedRefreshEndsTheSession() = runTest {
        repository.login("emilys", "emilyspass")
        repository.getCurrentUser()
        server.expireAccessToken()
        server.rejectRefresh = true

        val expired = async(start = CoroutineStart.UNDISPATCHED) { sessionEvents.expired.first() }
        val result = repository.getCurrentUser()

        assertEquals(Result.Failure(DataError.Unauthorized), result)
        assertNull(storage.tokens)
        assertFalse(repository.isAuthorized())
        expired.await()
    }

    @Test
    fun logoutForgetsTokens() = runTest {
        repository.login("emilys", "emilyspass")
        repository.logout()

        assertFalse(repository.isAuthorized())
    }
}
