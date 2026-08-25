package dev.tulis.readbear.routes.menu

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.tulis.readbear.R
import dev.tulis.readbear.routes.menu.settings.AlreadyRead

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSettingsSheet(
    sheetState: SheetState,
    columnCount: Int,
    onChangeColumnCount: (Int) -> Unit,
    tooLongTextOption: TooLongTextOption,
    onChangeTooLongTextOption: (TooLongTextOption) -> Unit,
    alreadyReadOption: AlreadyReadOption,
    onChangeAlreadyReadOption: (AlreadyReadOption) -> Unit,
    onSaveRequest: () -> Unit,
    onHide: () -> Unit
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            onHide()
            onSaveRequest()
        },
        modifier = Modifier
            .wrapContentHeight()
    ) {
        var selectedTab by remember { mutableIntStateOf(0) }

        val tabs = listOf(
            stringResource(R.string.library_settings)
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
                    onChangeColumnCount = onChangeColumnCount,
                    tooLongTextOption = tooLongTextOption,
                    onChangeTooLongTextOption = onChangeTooLongTextOption,
                    alreadyReadOption = alreadyReadOption,
                    onChangeAlreadyReadOption = onChangeAlreadyReadOption,
                )

                1 -> {
                    Text("uwu")
                }
            }

            Button(
                onClick = {
                    onHide()
                    onSaveRequest()
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

@Composable
fun LibrarySettings(
    columnCount: Int,
    onChangeColumnCount: (Int) -> Unit,
    tooLongTextOption: TooLongTextOption,
    onChangeTooLongTextOption: (TooLongTextOption) -> Unit,
    alreadyReadOption: AlreadyReadOption,
    onChangeAlreadyReadOption: (AlreadyReadOption) -> Unit
) {
    var sliderColumnValue by remember { mutableIntStateOf(columnCount) }
    var tooLongTextOptionValue by remember { mutableStateOf(tooLongTextOption) }

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
                value = sliderColumnValue.toFloat(),
                onValueChange = { value ->
                    onChangeColumnCount(value.toInt())
                    sliderColumnValue = value.toInt()
                },
                valueRange = 1f..5f,
                steps = 3
            )
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
                    selected = tooLongTextOptionValue == TooLongTextOption.BASIC_MARQUEE,
                    onClick = {
                        onChangeTooLongTextOption(TooLongTextOption.BASIC_MARQUEE)
                        tooLongTextOptionValue = TooLongTextOption.BASIC_MARQUEE
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
                    selected = tooLongTextOptionValue == TooLongTextOption.ELLIPSIS,
                    onClick = {
                        onChangeTooLongTextOption(TooLongTextOption.ELLIPSIS)
                        tooLongTextOptionValue = TooLongTextOption.ELLIPSIS
                    }
                )
            }
        }

        AlreadyRead(alreadyReadOption) {
            onChangeAlreadyReadOption(it)
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