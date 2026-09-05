package dev.tulis.readbear.settings.tabs

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.tulis.readbear.R
import dev.tulis.readbear.settings.AlreadyReadOption
import dev.tulis.readbear.settings.TooLongTextOption
import dev.tulis.readbear.settings.composables.AlreadyRead
import dev.tulis.readbear.settings.composables.Models


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
    onChangeTimeClockEnabled: (Boolean) -> Unit,
    models: Models
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
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

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
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

        AlreadyRead(models = models, alreadyReadOption = alreadyReadOption) {
            onChangeAlreadyReadOption(it)
        }
    }
}