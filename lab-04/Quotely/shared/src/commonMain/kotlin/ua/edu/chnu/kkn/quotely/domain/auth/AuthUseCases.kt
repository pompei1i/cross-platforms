package ua.edu.chnu.kkn.quotely.domain.auth

import kotlinx.coroutines.flow.Flow
import ua.edu.chnu.kkn.quotely.domain.common.Result

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(username: String, password: String): Result<User> =
        repository.login(username.trim(), password)
}

class LogoutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.logout()
}

class IsAuthorizedUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Boolean = repository.isAuthorized()
}

class GetCurrentUserUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Result<User> = repository.getCurrentUser()
}

class ObserveSessionExpiredUseCase(private val repository: AuthRepository) {
    operator fun invoke(): Flow<Unit> = repository.sessionExpired
}
