package dev.tulis.readbear.routes.menu.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.tulis.readbear.R
import dev.tulis.readbear.routes.menu.PdfReadingLayout
import dev.tulis.readbear.utils.sampleImages

@Composable
fun PdfLayout(
    pdfReadingLayout: PdfReadingLayout,
    onChangePdfReadingLayout: (PdfReadingLayout) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(stringResource(R.string.reading_layout))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
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
                Box {
                    val model = remember {
                        sampleImages.random()
                    }

                    AsyncImage(
                        model = model,
                        contentDescription = null,
                        modifier = Modifier
                            .width(55.dp)
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
                    }
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
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Color.White),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val model1 = remember {
                        sampleImages.random()
                    }

                    val model2 = remember {
                        sampleImages.filter { it != model1 }.random()
                    }

                    AsyncImage(
                        model = model1,
                        contentDescription = null,
                        modifier = Modifier
                            .width(55.dp)
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(
                                topStart = 10.dp,
                                bottomStart = 10.dp
                            )),
                        contentScale = ContentScale.Crop
                    )

                    AsyncImage(
                        model = model2,
                        contentDescription = null,
                        modifier = Modifier
                            .width(55.dp)
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(
                                topEnd = 10.dp,
                                bottomEnd = 10.dp
                            )),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(stringResource(R.string.side_by_side))

                RadioButton(
                    selected = pdfReadingLayout == PdfReadingLayout.SPREAD,
                    onClick = {
                        onChangePdfReadingLayout(PdfReadingLayout.SPREAD)
                    }
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
                Box {
                    val model = remember {
                        sampleImages.random()
                    }

                    AsyncImage(
                        model = model,
                        contentDescription = null,
                        modifier = Modifier
                            .width(55.dp)
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
                    }
                )
            }
        }
    }
}