package ua.edu.chnu.kkn.quotely

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import ua.edu.chnu.kkn.quotely.data.security.AuthTokens
import ua.edu.chnu.kkn.quotely.data.security.TokenStorage

class InMemoryTokenStorage(var tokens: AuthTokens? = null) : TokenStorage {
    override suspend fun getTokens(): AuthTokens? = tokens
    override suspend fun saveTokens(tokens: AuthTokens) {
        this.tokens = tokens
    }
    override suspend fun clear() {
        tokens = null
    }
}

/**
 * A tiny stand-in for dummyjson.com: accepts emilys/emilyspass, issues numbered tokens
 * and only accepts the latest access token on `auth/me`.
 */
class FakeDummyJson {
    var validAccessToken = "access-1"
    var validRefreshToken = "refresh-1"
    var refreshCalls = 0
    var rejectRefresh = false
    var totalQuotes = 45
    var failQuotes = false
    val requests = mutableListOf<String>()

    val engine = MockEngine { request -> handle(request) }

    /** Makes the current access token stale, like after its lifetime has passed. */
    fun expireAccessToken() {
        validAccessToken = "access-expired-${validAccessToken}"
    }

    private fun MockRequestHandleScope.handle(request: HttpRequestData): HttpResponseData {
        val path = request.url.encodedPath
        requests += path
        val body = (request.body as? TextContent)?.text.orEmpty()
        return when {
            path.endsWith("auth/login") ->
                if ("\"emilys\"" in body && "\"emilyspass\"" in body) json(loginJson())
                else json("""{"message":"Invalid credentials"}""", HttpStatusCode.BadRequest)

            path.endsWith("auth/refresh") -> {
                refreshCalls++
                if (rejectRefresh || "\"$validRefreshToken\"" !in body) {
                    json("""{"message":"Invalid refresh token"}""", HttpStatusCode.Forbidden)
                } else {
                    validAccessToken = "access-${refreshCalls + 1}"
                    validRefreshToken = "refresh-${refreshCalls + 1}"
                    json("""{"accessToken":"$validAccessToken","refreshToken":"$validRefreshToken"}""")
                }
            }

            path.endsWith("auth/me") ->
                if (request.headers[HttpHeaders.Authorization] == "Bearer $validAccessToken") json(userJson())
                else json("""{"message":"Token Expired!"}""", HttpStatusCode.Unauthorized)

            path.endsWith("quotes") -> {
                if (failQuotes) return json("""{"message":"Down"}""", HttpStatusCode.ServiceUnavailable)
                val limit = request.url.parameters["limit"]!!.toInt()
                val skip = request.url.parameters["skip"]!!.toInt()
                val ids = (skip + 1..minOf(skip + limit, totalQuotes)).toList()
                val items = ids.joinToString(",") { """{"id":$it,"quote":"Quote $it","author":"Author $it"}""" }
                json("""{"quotes":[$items],"total":$totalQuotes,"skip":$skip,"limit":$limit}""")
            }

            path.contains("quotes/") -> {
                val id = path.substringAfterLast('/').toInt()
                if (id in 1..totalQuotes) json("""{"id":$id,"quote":"Quote $id","author":"Author $id"}""")
                else json("""{"message":"Quote with id '$id' not found"}""", HttpStatusCode.NotFound)
            }

            else -> json("""{"message":"Not found"}""", HttpStatusCode.NotFound)
        }
    }

    private fun loginJson() = """
        {"id":1,"username":"emilys","email":"emily@x.dummyjson.com","firstName":"Emily","lastName":"Johnson",
         "gender":"female","image":"","accessToken":"$validAccessToken","refreshToken":"$validRefreshToken"}
    """.trimIndent()

    private fun userJson() =
        """{"id":1,"username":"emilys","email":"emily@x.dummyjson.com","firstName":"Emily","lastName":"Johnson"}"""

    private fun MockRequestHandleScope.json(content: String, status: HttpStatusCode = HttpStatusCode.OK) =
        respond(content, status, headersOf(HttpHeaders.ContentType, "application/json"))
}
