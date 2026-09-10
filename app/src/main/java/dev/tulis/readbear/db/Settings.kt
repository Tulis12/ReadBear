package dev.tulis.readbear.db

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.tulis.readbear.settings.AlreadyReadOption
import dev.tulis.readbear.settings.PdfReadingLayout
import dev.tulis.readbear.settings.TooLongTextOption
import dev.tulis.readbear.ui.theme.ThemeType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object Settings {
    val Context.dataStore by preferencesDataStore(
        name = "settings"
    )

    object SettingsKeys {
        val COLUMNS = intPreferencesKey("columns")
        val LONG_TEXT_OPTION = stringPreferencesKey("longTextOption")
        val ALREADY_READ_OPTION = stringPreferencesKey("alreadyReadOption")
        val PROGRESS_ENABLED = booleanPreferencesKey("progressEnabled")
        val TIME_CLOCK_ENABLED = booleanPreferencesKey("timeClockEnabled")
        val PDF_LAYOUT = stringPreferencesKey("pdfLayout")
        val THEME_TYPE = stringPreferencesKey("themeType")
        val THEME = stringPreferencesKey("theme")
    }

    fun getThemeMode(context: Context): Flow<ThemeType> {
        return context.dataStore.data
            .map { preferences ->
                ThemeType.valueOf(preferences[SettingsKeys.THEME_TYPE] ?: "SYSTEM")
            }
    }

    fun getTooLongTextOption(context: Context): Flow<TooLongTextOption> {
        return context.dataStore.data
            .map { preferences ->
                TooLongTextOption.valueOf(preferences[SettingsKeys.LONG_TEXT_OPTION] ?: "BASIC_MARQUEE")
            }
    }

    fun getSettings(context: Context): Flow<SettingsState> {
        return context.dataStore.data.map { preferences ->
            SettingsState(
                columnCount = preferences[SettingsKeys.COLUMNS] ?: 3,

                longTextOption = TooLongTextOption.valueOf(
                    preferences[SettingsKeys.LONG_TEXT_OPTION]
                        ?: "BASIC_MARQUEE"
                ),

                alreadyReadOption = AlreadyReadOption.valueOf(
                    preferences[SettingsKeys.ALREADY_READ_OPTION]
                        ?: "TIMES_AND_CHECKMARK"
                ),

                progressEnabled = preferences[SettingsKeys.PROGRESS_ENABLED] ?: true,
                timeClockEnabled = preferences[SettingsKeys.TIME_CLOCK_ENABLED] ?: true,

                pdfReadingLayout = PdfReadingLayout.valueOf(
                    preferences[SettingsKeys.PDF_LAYOUT]
                        ?: "CONTINUOUS"
                ),

                themeType = ThemeType.valueOf(
                    preferences[SettingsKeys.THEME_TYPE]
                        ?: "SYSTEM"
                ),

                theme = preferences[SettingsKeys.THEME] ?: "Bear"
            )
        }
    }

    data class SettingsState(
        val columnCount: Int,
        val longTextOption: TooLongTextOption,
        val alreadyReadOption: AlreadyReadOption,
        val progressEnabled: Boolean,
        val timeClockEnabled: Boolean,
        val pdfReadingLayout: PdfReadingLayout,
        val themeType: ThemeType,
        val theme: String
    )
}