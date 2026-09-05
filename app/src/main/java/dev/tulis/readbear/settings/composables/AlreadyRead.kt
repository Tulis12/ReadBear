package dev.tulis.readbear.settings.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.tulis.readbear.R
import dev.tulis.readbear.settings.AlreadyReadOption
import dev.tulis.readbear.utils.sampleImages

@Composable
fun AlreadyRead(
    models: Models,
    alreadyReadOption: AlreadyReadOption,
    onChangeAlreadyReadOption: (AlreadyReadOption) -> Unit,
) {
    var alreadyReadOptionValue by remember { mutableStateOf(alreadyReadOption) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(stringResource(R.string.when_you_read_the_book_already))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {

            AlreadyReadOptionRadio(
                modifier = Modifier.weight(1f),
                additionalBoxModifier = Modifier
                    .clip(RoundedCornerShape(15.dp))
                    .background(MaterialTheme.colorScheme.onPrimaryContainer)
                    .padding(5.dp),
                alreadyReadOption = AlreadyReadOption.TIMES_AND_CHECKMARK,
                alreadyReadOptionValue = alreadyReadOptionValue,
                onChangeAlreadyReadOption = {
                    onChangeAlreadyReadOption(it)
                    alreadyReadOptionValue = it
                },
                model = models.model1
            ) {
                Row {
                    Text(
                        "4x",
                        color = MaterialTheme.colorScheme.onSecondary
                    )

                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            }

            AlreadyReadOptionRadio(
                modifier = Modifier.weight(1f),
                additionalBoxModifier = Modifier
                    .clip(RoundedCornerShape(15.dp))
                    .background(MaterialTheme.colorScheme.onPrimaryContainer)
                    .padding(3.dp),
                alreadyReadOption = AlreadyReadOption.CHECKMARK,
                alreadyReadOptionValue = alreadyReadOptionValue,
                onChangeAlreadyReadOption = {
                    alreadyReadOptionValue = it
                    onChangeAlreadyReadOption(it)
                },
                model = models.model2
            ) {
                Row {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun AlreadyReadOptionRadio(
    modifier: Modifier,
    additionalBoxModifier: Modifier = Modifier,
    alreadyReadOption: AlreadyReadOption,
    alreadyReadOptionValue: AlreadyReadOption,
    onChangeAlreadyReadOption: (AlreadyReadOption) -> Unit,
    model: Int,
    checkmark: @Composable () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable (
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onChangeAlreadyReadOption(alreadyReadOption)
            }
    ) {
        Box {
            AsyncImage(
                model = model,
                contentDescription = null,
                modifier = Modifier
                    .width(75.dp)
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 5.dp, bottom = 5.dp)
                    .then(additionalBoxModifier)
            ) {
                checkmark()
            }
        }

        RadioButton(
            selected = alreadyReadOptionValue == alreadyReadOption,
            onClick = {
                onChangeAlreadyReadOption(alreadyReadOption)
            }
        )
    }
}