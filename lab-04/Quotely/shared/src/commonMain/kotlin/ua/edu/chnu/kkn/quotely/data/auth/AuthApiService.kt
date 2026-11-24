package ua.edu.chnu.kkn.quotely.data.auth

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ua.edu.chnu.kkn.quotely.data.auth.model.LoginRequest
import ua.edu.chnu.kkn.quotely.data.auth.model.LoginResponse
import ua.edu.chnu.kkn.quotely.data.auth.model.UserResponse
import ua.edu.chnu.kkn.quotely.data.remote.CURRENT_USER_PATH
import ua.edu.chnu.kkn.quotely.data.remote.LOGIN_PATH
import ua.edu.chnu.kkn.quotely.data.remote.safeRequest
import ua.edu.chnu.kkn.quotely.domain.common.DataError
import ua.edu.chnu.kkn.quotely.domain.common.Result
import ua.edu.chnu.kkn.quotely.domain.common.mapError

internal interface AuthApiService {
    suspend fun login(username: String, password: String): Result<LoginResponse>
    suspend fun getCurrentUser(): Result<UserResponse>

    /** Drops the tokens cached by the Auth plugin, so the next request loads them from storage again. */
    fun resetCachedTokens()
}

internal class AppAuthApiService(private val client: HttpClient) : AuthApiService {

    override suspend fun login(username: String, password: String): Result<LoginResponse> =
        safeRequest<LoginResponse> {
            client.post(LOGIN_PATH) {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username, password))
            }
        }.mapError { error ->
            // dummyjson.com answers 400 "Invalid credentials" for a wrong username or password
            if (error is DataError.Http && error.code == 400 || error == DataError.Unauthorized) {
                DataError.InvalidCredentials
            } else {
                error
            }
        }

    override suspend fun getCurrentUser(): Result<UserResponse> =
        safeRequest { client.get(CURRENT_USER_PATH) }

    override fun resetCachedTokens() {
        client.authProvider<BearerAuthProvider>()?.clearToken()
    }
}
