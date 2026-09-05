package dev.tulis.readbear.settings.tabs

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import dev.tulis.readbear.R
import dev.tulis.readbear.db.pdfs.Pdf
import dev.tulis.readbear.settings.PdfReadingLayout
import dev.tulis.readbear.settings.PdfSettingsContext
import dev.tulis.readbear.settings.SettingsViewModel
import dev.tulis.readbear.settings.composables.Models
import dev.tulis.readbear.settings.composables.PdfLayout
import dev.tulis.readbear.utils.sampleImages
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderSettings(
    viewModel: SettingsViewModel = hiltViewModel(),
    pdfReadingLayout: PdfReadingLayout,
    pdfSettingsContext: PdfSettingsContext? = null,
    onChangePdfReadingLayout: (PdfReadingLayout) -> Unit,
    models: Models
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if(pdfSettingsContext != null) {
            val pdf: Pdf? by viewModel.getPdfFlow(pdfSettingsContext.pdfId).collectAsState(null)

            PdfLayout(
                pdfReadingLayout = if(pdf == null || pdf?.splitPages != true) {
                    pdfReadingLayout
                } else {
                    PdfReadingLayout.PAGED
                },
                onChangePdfReadingLayout = if(pdf == null || pdf?.splitPages != true) {
                    onChangePdfReadingLayout
                } else {
                    {}
                },
                models = models,
                disabled = pdf != null && pdf?.splitPages == true
            )
        } else {
            PdfLayout(
                pdfReadingLayout = pdfReadingLayout,
                onChangePdfReadingLayout = onChangePdfReadingLayout,
                models = models
            )
        }


        pdfSettingsContext?.let {
            val pdf: Pdf? by viewModel.getPdfFlow(pdfSettingsContext.pdfId).collectAsState(null)

            val savedPdf = pdf ?: return@let
            val scope = rememberCoroutineScope()

            Text(stringResource(R.string.this_pdf_settings))

            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = savedPdf.splitPages, onCheckedChange = {
                    savedPdf.splitPages = it
                    viewModel.updatePdf(savedPdf)
                })

                Text(
                    stringResource(R.string.split_pages),
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        savedPdf.splitPages = !savedPdf.splitPages
                        viewModel.updatePdf(savedPdf)
                    }
                )

                val context = LocalContext.current
                val info = stringResource(R.string.info)
                val tooltipState = rememberTooltipState(isPersistent = true)

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        TooltipAnchorPosition.Above,
                        10.dp
                    ),
                    tooltip = {
                        RichTooltip(
                            title = {
                                Text(stringResource(R.string.split_pages))
                            },
                            action = {
                                Button(onClick = {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        "https://tulis12.github.io/readbear/split_pages".toUri()
                                    )

                                    context.startActivity(intent)
                                }) {
                                    Text(stringResource(R.string.read_docs))
                                }
                            }
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(stringResource(R.string.split_pages_tooltip), textAlign = TextAlign.Justify)

                                val example1 = remember {
                                    sampleImages.random()
                                }

                                val example2 = remember {
                                    sampleImages.filter { it != example1 }.random()
                                }

                                Row(
                                    modifier = Modifier.height(IntrinsicSize.Min),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(Color.White),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        AsyncImage(
                                            model = example1,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(2f / 3f)
                                                .clip(RoundedCornerShape(
                                                    topStart = 10.dp,
                                                    bottomStart = 10.dp
                                                )),
                                            contentScale = ContentScale.Crop
                                        )

                                        AsyncImage(
                                            model = example2,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(2f / 3f)
                                                .clip(RoundedCornerShape(
                                                    topEnd = 10.dp,
                                                    bottomEnd = 10.dp
                                                )),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    Text("→", fontSize = 40.sp)

                                    var page by remember { mutableIntStateOf(0) }

                                    LaunchedEffect(Unit) {
                                        while(true) {
                                            delay(3.seconds)
                                            page = if(page == 0) 1 else 0
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    ) {
                                        AnimatedContent(
                                            targetState = page,
                                            transitionSpec = {
                                                slideInHorizontally { width -> width } togetherWith
                                                        slideOutHorizontally { width -> -width }
                                            },
                                            label = "page"
                                        ) { targetPage ->
                                            when (targetPage) {
                                                0 -> AsyncImage(
                                                    model = example1,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .fillMaxWidth(0.5f)
                                                        .aspectRatio(2f / 3f)
                                                        .clip(RoundedCornerShape(10.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                                1 -> AsyncImage(
                                                    model = example2,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .fillMaxWidth(0.5f)
                                                        .aspectRatio(2f / 3f)
                                                        .clip(RoundedCornerShape(10.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    },
                    state = tooltipState
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                tooltipState.show()
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = info
                        )
                    }
                }
            }

        }
    }
}