package ua.edu.chnu.kkn.quotely.ui.common

import org.jetbrains.compose.resources.getString
import ua.edu.chnu.kkn.quotely.domain.common.DataError
import ua.edu.chnu.kkn.quotely.resources.Res
import ua.edu.chnu.kkn.quotely.resources.error_bad_data
import ua.edu.chnu.kkn.quotely.resources.error_invalid_credentials
import ua.edu.chnu.kkn.quotely.resources.error_no_internet
import ua.edu.chnu.kkn.quotely.resources.error_server
import ua.edu.chnu.kkn.quotely.resources.error_session_expired
import ua.edu.chnu.kkn.quotely.resources.error_timeout
import ua.edu.chnu.kkn.quotely.resources.error_unknown

/** Localized, user-facing description of [DataError] (suspend, so it can be used from event handlers). */
suspend fun DataError.message(): String = when (this) {
    DataError.NoInternet -> getString(Res.string.error_no_internet)
    DataError.Timeout -> getString(Res.string.error_timeout)
    DataError.InvalidCredentials -> getString(Res.string.error_invalid_credentials)
    DataError.Unauthorized -> getString(Res.string.error_session_expired)
    DataError.Serialization -> getString(Res.string.error_bad_data)
    is DataError.Server -> getString(Res.string.error_server, code)
    is DataError.Http -> message ?: getString(Res.string.error_unknown)
    is DataError.Unknown -> getString(Res.string.error_unknown)
}
