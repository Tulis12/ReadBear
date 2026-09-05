package dev.tulis.readbear.settings.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.tulis.readbear.R
import dev.tulis.readbear.settings.PdfReadingLayout
import dev.tulis.readbear.settings.PdfSettingsContext
import dev.tulis.readbear.utils.sampleImages

@Composable
fun PdfLayout(
    pdfReadingLayout: PdfReadingLayout,
    onChangePdfReadingLayout: (PdfReadingLayout) -> Unit,
    models: Models,
    disabled: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(stringResource(R.string.reading_layout))

        Row(
            horizontalArrangement = Arrangement.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
                    .clickable (
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onChangePdfReadingLayout(PdfReadingLayout.CONTINUOUS)
                    }
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(0.5f)
                ) {
                    AsyncImage(
                        model = models.model1,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(stringResource(R.string.scrollable))

                RadioButton(
                    selected = pdfReadingLayout == PdfReadingLayout.CONTINUOUS,
                    onClick = {
                        onChangePdfReadingLayout(PdfReadingLayout.CONTINUOUS)
                    },
                    enabled = !disabled
                )
            }


            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
                    .clickable (
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onChangePdfReadingLayout(PdfReadingLayout.SPREAD)
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    AsyncImage(
                        model = models.model2,
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(2f / 3f)
                        ,contentScale = ContentScale.Crop
                    )

                    AsyncImage(
                        model = models.model3,
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(2f / 3f)
                        ,contentScale = ContentScale.Crop
                    )
                }

                Text(stringResource(R.string.side_by_side))

                RadioButton(
                    selected = pdfReadingLayout == PdfReadingLayout.SPREAD,
                    onClick = {
                        onChangePdfReadingLayout(PdfReadingLayout.SPREAD)
                    },
                    enabled = !disabled
                )
            }


            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
                    .clickable (
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onChangePdfReadingLayout(PdfReadingLayout.PAGED)
                    }
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(0.5f)
                ) {
                    AsyncImage(
                        model = models.model4,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(stringResource(R.string.flipping))

                RadioButton(
                    selected = pdfReadingLayout == PdfReadingLayout.PAGED,
                    onClick = {
                        onChangePdfReadingLayout(PdfReadingLayout.PAGED)
                    },
                    enabled = !disabled
                )
            }
        }
    }
}

class Models {
    val model1: Int = sampleImages.random()
    val model2: Int = sampleImages.filter { it != model1 }.random()
    val model3: Int = sampleImages.filter { it != model1 && it != model2 }.random()
    val model4: Int = sampleImages.filter { it != model1 && it != model2 && it != model3 }.random()
}
