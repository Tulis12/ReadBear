package dev.tulis.readbear.utils

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.intl.Locale
import kotlin.time.Instant

@Composable
fun formatDate(instant: Instant): String {
    val locale = java.util.Locale.forLanguageTag(
        Locale.current.toLanguageTag()
    )

    val pattern = DateFormat.getBestDateTimePattern(
        locale,
        "d MMM yyyy HH:mm"
    )

    return DateFormat
        .format(pattern, instant.toEpochMilliseconds())
        .toString()
}