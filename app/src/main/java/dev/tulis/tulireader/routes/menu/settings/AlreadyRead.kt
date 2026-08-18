package dev.tulis.tulireader.routes.menu.settings

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.tulis.tulireader.routes.menu.AlreadyReadOption
import dev.tulis.tulireader.routes.menu.TooLongTextOption

@Composable
fun AlreadyRead(
    alreadyReadOption: AlreadyReadOption,
    onChangeAlreadyReadOption: (AlreadyReadOption) -> Unit
) {
    var alreadyReadOptionValue by remember { mutableStateOf(alreadyReadOption) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Gdy przeczytałeś już książkę wyświetl:")

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
                }
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
                alreadyReadOption = AlreadyReadOption.CHECKMARK,
                alreadyReadOptionValue = alreadyReadOptionValue,
                onChangeAlreadyReadOption = {
                    onChangeAlreadyReadOption(it)
                }
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
    checkmark: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box {
            AsyncImage(
                model = "https://picsum.photos/200/300",
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