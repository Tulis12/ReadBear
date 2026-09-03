package dev.tulis.readbear.routes.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlexDirection.Companion.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import coil3.compose.AsyncImage
import dev.tulis.readbear.BuildConfig
import dev.tulis.readbear.R
import dev.tulis.readbear.db.Settings
import dev.tulis.readbear.db.Settings.dataStore
import dev.tulis.readbear.routes.info.InfoRow
import dev.tulis.readbear.routes.menu.settings.AlreadyRead
import dev.tulis.readbear.routes.menu.settings.PdfLayout
import dev.tulis.readbear.utils.sampleImages
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSettingsSheet(
    defaultTabOpen: Int = 0,
    sheetState: SheetState,
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
            stringResource(R.string.info)
        )

        Column {
            PrimaryTabRow(
                selectedTabIndex = selectedTab
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
                    }
                )

                1 -> PdfReaderSettings(
                    pdfReadingLayout = pdfReadingLayout,
                    onChangePdfReadingLayout = {
                        pdfReadingLayout = it
                    }
                )

                2 -> AppInfo()
            }

//            Button(
//                onClick = {
//                    onHide()
//                    onSaveRequest()
//                },
//                modifier = Modifier.align(Alignment.CenterHorizontally)
//            ) {
//                Text(stringResource(R.string.save))
//            }
        }
    }
}

@Composable
fun PdfReaderSettings(
    pdfReadingLayout: PdfReadingLayout,
    onChangePdfReadingLayout: (PdfReadingLayout) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        PdfLayout(pdfReadingLayout, onChangePdfReadingLayout)
    }
}

@Composable
fun LibrarySettings(
    columnCount: Int,
    onChangeColumnCount: (Int) -> Unit,

    tooLongTextOption: TooLongTextOption,
    onChangeTooLongTextOption: (TooLongTextOption) -> Unit,

    alreadyReadOption: AlreadyReadOption,
    onChangeAlreadyReadOption: (AlreadyReadOption) -> Unit,

    progressEnabled: Boolean,
    onChangeProgressEnabled: (Boolean) -> Unit,

    timeClockEnabled: Boolean,
    onChangeTimeClockEnabled: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        ) {
            Text(stringResource(R.string.column_count))

            Slider(
                value = columnCount.toFloat(),
                onValueChange = { value ->
                    onChangeColumnCount(value.toInt())
                },
                valueRange = 1f..5f,
                steps = 3
            )
        }

        Text(stringResource(R.string.show))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = progressEnabled,
                    onCheckedChange = {
                        onChangeProgressEnabled(it)
                    }
                )

                Text(stringResource(R.string.progress_enabled))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = timeClockEnabled,
                    onCheckedChange = {
                        onChangeTimeClockEnabled(it)
                    }
                )

                Text(stringResource(R.string.time_clock_enabled))
            }
        }

        val longText = stringResource(R.string.this_is_a_long_text)

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    longText,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .basicMarquee()
                )

                RadioButton(
                    selected = tooLongTextOption == TooLongTextOption.BASIC_MARQUEE,
                    onClick = {
                        onChangeTooLongTextOption(TooLongTextOption.BASIC_MARQUEE)
                    }
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    longText,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                    overflow = TextOverflow.Ellipsis
                )

                RadioButton(
                    selected = tooLongTextOption == TooLongTextOption.ELLIPSIS,
                    onClick = {
                        onChangeTooLongTextOption(TooLongTextOption.ELLIPSIS)
                    }
                )
            }
        }

        AlreadyRead(alreadyReadOption) {
            onChangeAlreadyReadOption(it)
        }
    }
}

@Composable
fun AppInfo() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(15.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
            modifier = Modifier.padding(5.dp)
        ) {
            AsyncImage(
                model = R.drawable.readbear_bg,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(0.3f)
            )

            Text(stringResource(R.string.base_app_name), modifier = Modifier.padding(top = 5.dp), fontSize = 20.sp)
            Text(BuildConfig.VERSION_NAME)
        }

        VerticalDivider(
            modifier = Modifier.fillMaxHeight(0.25f)
        )

        val locale = LocalConfiguration.current.locales[0]
        val context = LocalContext.current

        val languageName = locale.getDisplayLanguage(Locale.ENGLISH)
        var translators by remember { mutableStateOf(mutableListOf<String>()) }

        LaunchedEffect(Unit) {
            val jsonString =
                context.resources
                    .openRawResource(R.raw.translation_credits)
                    .bufferedReader()
                    .use { it.readText() }

            val jsonArray = JSONArray(jsonString)

            val usernames = mutableListOf<String>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)

                if (obj.has(languageName)) {
                    val users = obj.getJSONArray(languageName)

                    for (j in 0 until users.length()) {
                        usernames += users.getJSONObject(j).getString("username")
                    }

                    break
                }
            }

            translators = usernames
            println(translators)
        }



        Column {
            InfoRow(stringResource(R.string.author), "Tulis")
            InfoRow(stringResource(R.string.current_language), locale.getDisplayLanguage(locale))

            if(!translators.isEmpty()) {
                var translatorsText = ""
                var i = 0

                translators.forEach {
                    translatorsText += it
                    if(i != translators.count() - 1) translatorsText += ","
                    i++
                }

                if(translators.count() == 1) {
                    InfoRow(stringResource(R.string.translator), translators.first())
                } else {
                    Text(stringResource(R.string.translators))
                    Text(translatorsText, fontWeight = FontWeight.Medium)
                }
            }

            HorizontalDivider()

            Text(stringResource(R.string.thanks_for_using), autoSize = TextAutoSize.StepBased(), maxLines = 1)
            Text(text = buildAnnotatedString {
                withLink(
                    LinkAnnotation.Url("https://github.com/Tulis12/ReadBear")
                ) {
                    append("Visit the project on GitHub")
                }
            })


        }
    }
}

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