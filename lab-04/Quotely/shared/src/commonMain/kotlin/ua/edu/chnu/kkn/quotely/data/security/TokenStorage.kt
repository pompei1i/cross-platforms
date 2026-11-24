package ua.edu.chnu.kkn.quotely.data.security

import eu.anifantakis.lib.ksafe.KSafe

data class AuthTokens(val accessToken: String, val refreshToken: String)

interface TokenStorage {
    suspend fun getTokens(): AuthTokens?
    suspend fun saveTokens(tokens: AuthTokens)
    suspend fun clear()
}

/**
 * Keeps the tokens in KSafe: values are encrypted with a key held by the platform keystore
 * (Android Keystore, iOS Keychain, an OS-protected key on desktop).
 */
internal class KSafeTokenStorage(private val ksafe: KSafe) : TokenStorage {

    override suspend fun getTokens(): AuthTokens? {
        val access = ksafe.get(ACCESS_TOKEN_KEY, "")
        val refresh = ksafe.get(REFRESH_TOKEN_KEY, "")
        return if (refresh.isEmpty()) null else AuthTokens(access, refresh)
    }

    override suspend fun saveTokens(tokens: AuthTokens) {
        ksafe.put(ACCESS_TOKEN_KEY, tokens.accessToken)
        ksafe.put(REFRESH_TOKEN_KEY, tokens.refreshToken)
    }

    override suspend fun clear() {
        ksafe.delete(ACCESS_TOKEN_KEY)
        ksafe.delete(REFRESH_TOKEN_KEY)
    }

    private companion object {
        const val ACCESS_TOKEN_KEY = "access_token"
        const val REFRESH_TOKEN_KEY = "refresh_token"
    }
}
