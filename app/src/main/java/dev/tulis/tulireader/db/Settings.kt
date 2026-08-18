package dev.tulis.tulireader.db

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.tulis.tulireader.routes.menu.AlreadyReadOption
import dev.tulis.tulireader.routes.menu.TooLongTextOption
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
    }


    fun getColumns(context: Context): Flow<Int> {
        return context.dataStore.data
            .map { preferences ->
                preferences[SettingsKeys.COLUMNS] ?: 3
            }
    }

    fun getTooLongTextOption(context: Context): Flow<TooLongTextOption> {
        return context.dataStore.data
            .map { preferences ->
                TooLongTextOption.valueOf(preferences[SettingsKeys.LONG_TEXT_OPTION] ?: "BASIC_MARQUEE")
            }
    }

    fun getAlreadyReadOption(context: Context): Flow<AlreadyReadOption> {
        return context.dataStore.data
            .map { preferences ->
                AlreadyReadOption.valueOf(preferences[SettingsKeys.ALREADY_READ_OPTION] ?: "TIMES_AND_CHECKMARK")
            }
    }
}