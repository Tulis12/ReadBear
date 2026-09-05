package dev.tulis.readbear.settings.tabs

import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.core.os.LocaleListCompat
import dev.tulis.readbear.R

@Composable
fun Appearance() {
    val languages = mapOf(
        Pair("en", AppLanguage(Locale("en"), "English")),
        Pair("pl", AppLanguage(Locale("pl"), "Polski")),
        Pair("ru", AppLanguage(Locale("ru"), "Русский"))
    )

    val locale = LocalConfiguration.current.locales[0]
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(
        locale.language
    ) }

    val scope = rememberCoroutineScope()

    var showAlert by remember { mutableStateOf(false) }
    var changingLanguage by remember { mutableStateOf(false) }


    OutlinedButton(
        onClick = { expanded = true },
        enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    ) {
        languages[selected]?.name?.let { Text(it) }
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        languages.forEach { locale ->
            DropdownMenuItem(
                text = {
                    Text(
                        locale.value.name
                    )
                },
                onClick = {
                    selected = locale.key
                    expanded = false

                    val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(locale.key)
                    AppCompatDelegate.setApplicationLocales(appLocale)
                }
            )
        }
    }
}

data class AppLanguage(
    val locale: Locale,
    val name: String
)