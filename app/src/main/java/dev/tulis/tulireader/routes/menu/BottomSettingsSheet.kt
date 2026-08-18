package dev.tulis.tulireader.routes.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.tulis.tulireader.routes.menu.settings.AlreadyRead

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
    onHide: () -> Unit,
    onSaveRequest: () -> Unit,
) {
    var sliderColumnValue by remember { mutableIntStateOf(columnCount) }
    var tooLongTextOptionValue by remember { mutableStateOf(tooLongTextOption) }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onHide
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
                Text("Liczba kolumn:")

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

            val longText = "To jest bardzo długi tekst, który by się normalnie nie zmieścił"

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
                        modifier = Modifier.fillMaxWidth().basicMarquee()
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

            Button(
                onClick = onHide,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Zapisz")
            }
        }
    }

    LaunchedEffect(Unit) {
        sheetState.show()
    }

    LaunchedEffect(sheetState.currentValue) {
        if(sheetState.currentValue == SheetValue.Hidden) {
            onSaveRequest()
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