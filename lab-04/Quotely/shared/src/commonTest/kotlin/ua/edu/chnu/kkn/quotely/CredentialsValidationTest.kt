package ua.edu.chnu.kkn.quotely

import ua.edu.chnu.kkn.quotely.domain.auth.validation.ValidatePasswordUseCase
import ua.edu.chnu.kkn.quotely.domain.auth.validation.ValidateUsernameUseCase
import ua.edu.chnu.kkn.quotely.domain.auth.validation.ValidationError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CredentialsValidationTest {

    private val validateUsername = ValidateUsernameUseCase()
    private val validatePassword = ValidatePasswordUseCase()

    @Test
    fun validUsernames() {
        assertNull(validateUsername("emilys"))
        assertNull(validateUsername("john.doe_99"))
        assertNull(validateUsername("  emilys  ")) // surrounding spaces are ignored
    }

    @Test
    fun invalidUsernames() {
        assertEquals(ValidationError.Empty, validateUsername("   "))
        assertEquals(ValidationError.TooShort(3), validateUsername("ab"))
        assertEquals(ValidationError.TooLong(32), validateUsername("a".repeat(33)))
        assertEquals(ValidationError.InvalidCharacters, validateUsername("john doe"))
        assertEquals(ValidationError.InvalidCharacters, validateUsername("олег"))
    }

    @Test
    fun passwords() {
        assertNull(validatePassword("emilyspass"))
        assertEquals(ValidationError.Empty, validatePassword(""))
        assertEquals(ValidationError.TooShort(6), validatePassword("12345"))
        assertEquals(ValidationError.ContainsWhitespace, validatePassword("pass word"))
        assertEquals(ValidationError.TooLong(64), validatePassword("p".repeat(65)))
    }
}
