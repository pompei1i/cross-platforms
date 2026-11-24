package ua.edu.chnu.kkn.quotely.domain.auth.validation

sealed interface ValidationError {
    data object Empty : ValidationError
    data class TooShort(val minLength: Int) : ValidationError
    data class TooLong(val maxLength: Int) : ValidationError
    data object InvalidCharacters : ValidationError
    data object ContainsWhitespace : ValidationError
}

/**
 * Username: 3–32 characters, Latin letters, digits, dots, hyphens and underscores
 * (the same rules dummyjson.com usernames follow).
 * Returns `null` when the value is valid.
 */
class ValidateUsernameUseCase {
    operator fun invoke(username: String): ValidationError? {
        val value = username.trim()
        return when {
            value.isEmpty() -> ValidationError.Empty
            value.length < MIN_LENGTH -> ValidationError.TooShort(MIN_LENGTH)
            value.length > MAX_LENGTH -> ValidationError.TooLong(MAX_LENGTH)
            !value.all { it.isAsciiLetterOrDigit() || it in ALLOWED_SYMBOLS } -> ValidationError.InvalidCharacters
            else -> null
        }
    }

    companion object {
        const val MIN_LENGTH = 3
        const val MAX_LENGTH = 32
        private const val ALLOWED_SYMBOLS = "._-"
    }
}

/** Password: 6–64 characters without whitespace. Returns `null` when the value is valid. */
class ValidatePasswordUseCase {
    operator fun invoke(password: String): ValidationError? = when {
        password.isEmpty() -> ValidationError.Empty
        password.any { it.isWhitespace() } -> ValidationError.ContainsWhitespace
        password.length < MIN_LENGTH -> ValidationError.TooShort(MIN_LENGTH)
        password.length > MAX_LENGTH -> ValidationError.TooLong(MAX_LENGTH)
        else -> null
    }

    companion object {
        const val MIN_LENGTH = 6
        const val MAX_LENGTH = 64
    }
}

private fun Char.isAsciiLetterOrDigit(): Boolean = this in 'a'..'z' || this in 'A'..'Z' || this in '0'..'9'
