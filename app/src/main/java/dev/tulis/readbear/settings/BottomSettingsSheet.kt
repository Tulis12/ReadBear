package dev.tulis.readbear.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SheetState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import dev.tulis.readbear.R
import dev.tulis.readbear.db.Settings
import dev.tulis.readbear.db.Settings.dataStore
import dev.tulis.readbear.settings.composables.Models
import dev.tulis.readbear.settings.tabs.AppInfo
import dev.tulis.readbear.settings.tabs.Appearance
import dev.tulis.readbear.settings.tabs.LibrarySettings
import dev.tulis.readbear.settings.tabs.PdfReaderSettings
import kotlinx.coroutines.launch

val models = Models()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSettingsSheet(
    defaultTabOpen: Int = 0,
    sheetState: SheetState,
    additionalContext: AdditionalSettingsContext? = null,
    onHide: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val settingsFlow by Settings.getSettings(context).collectAsState(null)
    val settings = settingsFlow ?: return

    var columnCount by remember { mutableIntStateOf(settings.columnCount) }
    var longTextOption by remember { mutableStateOf(settings.longTextOption) }
    var alreadyReadOption by remember { mutableStateOf(settings.alreadyReadOption) }
    var progressEnabled by remember { mutableStateOf(settings.progressEnabled) }
    var timeClockEnabled by remember { mutableStateOf(settings.timeClockEnabled) }
    var pdfReadingLayout by remember { mutableStateOf(settings.pdfReadingLayout) }

    val onSaveRequest = {
        scope.launch {
            context.dataStore.edit { settings ->
                settings[Settings.SettingsKeys.COLUMNS] = columnCount
                settings[Settings.SettingsKeys.LONG_TEXT_OPTION] = longTextOption.name
                settings[Settings.SettingsKeys.ALREADY_READ_OPTION] = alreadyReadOption.name
                settings[Settings.SettingsKeys.PROGRESS_ENABLED] = progressEnabled
                settings[Settings.SettingsKeys.TIME_CLOCK_ENABLED] = timeClockEnabled
                settings[Settings.SettingsKeys.PDF_LAYOUT] = pdfReadingLayout.name
            }
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            onSaveRequest()
            onHide()
        },
        modifier = Modifier
            .wrapContentHeight()
    ) {
        var selectedTab by remember { mutableIntStateOf(defaultTabOpen) }

        val tabs = listOf(
            stringResource(R.string.library_settings),
            stringResource(R.string.pdf),
            stringResource(R.string.appearance),
            stringResource(R.string.info),
        )

        Column {
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 2.dp,
                minTabWidth = 100.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                        },
                        text = {
                            Text(title)
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> LibrarySettings(
                    columnCount = columnCount,
                    onChangeColumnCount = {
                        columnCount = it
                    },
                    tooLongTextOption = longTextOption,
                    onChangeTooLongTextOption = {
                        longTextOption = it
                    },
                    alreadyReadOption = alreadyReadOption,
                    onChangeAlreadyReadOption = {
                        alreadyReadOption = it
                    },
                    progressEnabled = progressEnabled,
                    onChangeProgressEnabled = {
                        progressEnabled = it
                    },
                    timeClockEnabled = timeClockEnabled,
                    onChangeTimeClockEnabled = {
                        timeClockEnabled = it
                    },
                    models = models
                )

                1 -> PdfReaderSettings(
                    pdfReadingLayout = pdfReadingLayout,
                    pdfSettingsContext = additionalContext as? PdfSettingsContext,
                    onChangePdfReadingLayout = {
                        pdfReadingLayout = it
                    },
                    models = models
                )

                2 -> Appearance()

                3 -> AppInfo()
            }
        }
    }
}

open class AdditionalSettingsContext {}

data class PdfSettingsContext(
    var pdfId: Long
) : AdditionalSettingsContext()

enum class TooLongTextOption {
    BASIC_MARQUEE,
    ELLIPSIS
}

enum class AlreadyReadOption {
    TIMES_AND_CHECKMARK,
    CHECKMARK
}

enum class PdfReadingLayout {
    CONTINUOUS,
    SPREAD,
    PAGED
}