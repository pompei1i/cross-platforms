package ua.edu.chnu.kkn.quotely.data.remote

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import ua.edu.chnu.kkn.quotely.data.auth.model.RefreshRequest
import ua.edu.chnu.kkn.quotely.data.auth.model.RefreshResponse
import ua.edu.chnu.kkn.quotely.data.security.AuthTokens
import io.ktor.client.plugins.logging.Logger as KtorLogger
import ua.edu.chnu.kkn.quotely.data.security.SessionEvents
import ua.edu.chnu.kkn.quotely.data.security.TokenStorage

internal const val BASE_URL = "https://dummyjson.com/"
internal const val LOGIN_PATH = "auth/login"
internal const val REFRESH_PATH = "auth/refresh"
internal const val CURRENT_USER_PATH = "auth/me"

private val log = Logger.withTag("Http")

internal val AppJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
}

/**
 * The single HTTP client of the app. The Auth plugin adds `Authorization: Bearer <access token>`
 * to every request except login/refresh. On a 401 it refreshes the tokens once (concurrent requests
 * wait for the same refresh), saves the new pair and retries the request. When the refresh token
 * is rejected too, the tokens are dropped and [SessionEvents] tells the app to show the sign-in screen.
 */
internal fun createHttpClient(
    engine: HttpClientEngine,
    tokenStorage: TokenStorage,
    sessionEvents: SessionEvents,
): HttpClient = HttpClient(engine) {
    expectSuccess = false

    defaultRequest { url(BASE_URL) }

    install(ContentNegotiation) { json(AppJson) }

    install(HttpTimeout) {
        connectTimeoutMillis = 10_000
        requestTimeoutMillis = 20_000
    }

    install(Logging) {
        level = LogLevel.INFO
        logger = object : KtorLogger {
            override fun log(message: String) = log.d { message }
        }
        sanitizeHeader { it == HttpHeaders.Authorization }
    }

    install(Auth) {
        bearer {
            loadTokens {
                tokenStorage.getTokens()?.let { BearerTokens(it.accessToken, it.refreshToken) }
            }
            refreshTokens {
                val refreshToken = oldTokens?.refreshToken ?: tokenStorage.getTokens()?.refreshToken
                if (refreshToken.isNullOrEmpty()) {
                    null
                } else {
                    try {
                        log.i { "Access token expired, refreshing" }
                        val response = client.post(REFRESH_PATH) {
                            markAsRefreshTokenRequest()
                            contentType(ContentType.Application.Json)
                            setBody(RefreshRequest(refreshToken))
                        }
                        if (response.status.isSuccess()) {
                            val tokens = response.body<RefreshResponse>()
                            tokenStorage.saveTokens(AuthTokens(tokens.accessToken, tokens.refreshToken))
                            BearerTokens(tokens.accessToken, tokens.refreshToken)
                        } else {
                            log.w { "Refresh rejected with ${response.status}, signing out" }
                            tokenStorage.clear()
                            sessionEvents.notifyExpired()
                            null
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        // No network: keep the tokens, the request just fails with 401
                        log.w(e) { "Refresh failed" }
                        null
                    }
                }
            }
            sendWithoutRequest { request ->
                val path = request.url.encodedPath
                !path.endsWith(LOGIN_PATH) && !path.endsWith(REFRESH_PATH)
            }
        }
    }
}
