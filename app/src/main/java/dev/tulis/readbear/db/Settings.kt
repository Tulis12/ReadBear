package dev.tulis.readbear.db

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.tulis.readbear.routes.menu.AlreadyReadOption
import dev.tulis.readbear.routes.menu.PdfReadingLayout
import dev.tulis.readbear.routes.menu.TooLongTextOption
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
    }


//    fun getColumns(context: Context): Flow<Int> {
//        return context.dataStore.data
//            .map { preferences ->
//                preferences[SettingsKeys.COLUMNS] ?: 3
//            }
//    }
//
    fun getTooLongTextOption(context: Context): Flow<TooLongTextOption> {
        return context.dataStore.data
            .map { preferences ->
                TooLongTextOption.valueOf(preferences[SettingsKeys.LONG_TEXT_OPTION] ?: "BASIC_MARQUEE")
            }
    }
//
//    fun getAlreadyReadOption(context: Context): Flow<AlreadyReadOption> {
//        return context.dataStore.data
//            .map { preferences ->
//                AlreadyReadOption.valueOf(preferences[SettingsKeys.ALREADY_READ_OPTION] ?: "TIMES_AND_CHECKMARK")
//            }
//    }

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
                )
            )
        }
    }

    data class SettingsState(
        val columnCount: Int,
        val longTextOption: TooLongTextOption,
        val alreadyReadOption: AlreadyReadOption,
        val progressEnabled: Boolean,
        val timeClockEnabled: Boolean,
        val pdfReadingLayout: PdfReadingLayout
    )
}