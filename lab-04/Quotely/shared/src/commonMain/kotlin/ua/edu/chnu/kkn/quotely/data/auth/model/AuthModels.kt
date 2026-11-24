package ua.edu.chnu.kkn.quotely.data.auth.model

import kotlinx.serialization.Serializable
import ua.edu.chnu.kkn.quotely.domain.auth.User

/** Token lifetime asked from dummyjson.com. Short on purpose, so the refresh flow is easy to see. */
internal const val TOKEN_LIFETIME_MINUTES = 30

@Serializable
internal data class LoginRequest(
    val username: String,
    val password: String,
    val expiresInMins: Int = TOKEN_LIFETIME_MINUTES,
)

@Serializable
internal data class LoginResponse(
    val id: Int,
    val username: String,
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val accessToken: String,
    val refreshToken: String,
)

@Serializable
internal data class RefreshRequest(
    val refreshToken: String,
    val expiresInMins: Int = TOKEN_LIFETIME_MINUTES,
)

@Serializable
internal data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
)

@Serializable
internal data class UserResponse(
    val id: Int,
    val username: String,
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
)

/** dummyjson.com error body: `{"message": "Invalid credentials"}`. */
@Serializable
internal data class ErrorResponse(val message: String? = null)

internal fun LoginResponse.toUser() = User(id, username, firstName, lastName, email)
internal fun UserResponse.toUser() = User(id, username, firstName, lastName, email)
