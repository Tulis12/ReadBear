package dev.tulis.readbear.routes.menu

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import dev.tulis.readbear.db.Settings
import dev.tulis.readbear.utils.LongText
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun BookLibrary(
    viewModel: LibraryViewModel = hiltViewModel(),
    settings: Settings.SettingsState,
    padding: PaddingValues,
    selectedItems: List<Long>,
    onAddSelectedItem: (Long) -> Unit,
    onRemoveSelectedItem: (Long) -> Unit,
    selectionMode: Boolean,
    onChangeSelectionMode: (Boolean) -> Unit,
    onOpenBook: (Long) -> Unit
) {
    val books by viewModel.books.collectAsState()
    val context = LocalContext.current

    LazyVerticalGrid(
        columns = GridCells.Fixed(settings.columnCount),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        modifier = Modifier
            .padding(
                padding
            )
            .fillMaxSize()
    ) {
        items(books.size) { image ->
            val book = books[image]

            val scale by animateFloatAsState(
                targetValue = if (selectedItems.contains(book.id)) 0.88f else 1f,
                animationSpec = tween(200),
                label = "scale"
            )

            val alpha by animateFloatAsState(
                targetValue = if (selectedItems.contains(book.id)) 0.35f else 0f,
                animationSpec = tween(200),
                label = "glass"
            )

            Box(
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = alpha))
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                if(selectionMode) {
                                    if(selectedItems.contains(book.id)) {
                                        onRemoveSelectedItem(book.id)
                                    } else {
                                        onAddSelectedItem(book.id)
                                    }

                                    if(selectedItems.count() == 0) {
                                        onChangeSelectionMode(false)
                                    }

                                    return@combinedClickable;
                                }

                                onOpenBook(book.id)
                            },
                            onLongClick = {
                                if(selectionMode) return@combinedClickable

                                onChangeSelectionMode(true)
                                onAddSelectedItem(book.id)
                            },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple()
                        )
                        .then(
                            Modifier
                                .scale(scale)
                        )

                ) {
                    Box {
                        AsyncImage(
                            model = context.filesDir
                                .resolve(book.path)
                                .resolve(book.cover),
                            contentDescription = null,
                            modifier = Modifier
                                .aspectRatio(2f / 3f)
                                .clip(RoundedCornerShape(10.dp))
                                .fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )

                        if(book.readAlready > 0)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 5.dp, bottom = 5.dp)
                                    .clip(RoundedCornerShape(15.dp))
                                    .background(MaterialTheme.colorScheme.onPrimaryContainer)
                                    .then(
                                        if(book.readAlready > 1 && settings.alreadyReadOption == AlreadyReadOption.TIMES_AND_CHECKMARK) {
                                            Modifier.padding(5.dp)
                                        } else if(book.readAlready > 1) {
                                            Modifier.padding(3.dp)
                                        } else {
                                            Modifier
                                        }
                                    )

                            ) {
                                Row {
                                    if(book.readAlready > 1 && settings.alreadyReadOption == AlreadyReadOption.TIMES_AND_CHECKMARK) {
                                        Text(
                                            "${book.readAlready}x",
                                            color = MaterialTheme.colorScheme.onSecondary
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "",
                                        tint = MaterialTheme.colorScheme.primaryContainer
                                    )
                                }
                            }
                    }

                    Column {
                        LongText(book.title)

                        if(book.totalProgress != 0) {
                            var text = "${((book.progress / book.totalProgress.toFloat()) * 100).roundToInt()}%"
                            if(book.progress == 0 && book.readAlready > 0) {
                                text = "100%"
                            }

                            val readingTimeS = book.readingTime / 1000f
                            println(readingTimeS)
                            println(book.readingTime)

                            val h = (readingTimeS / 3600).toInt()
                            val m = ceil((readingTimeS - h * 3600) / 60).toInt()

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                if(settings.progressEnabled) {
                                    Text(
                                        text,
                                        style = TextStyle(
                                            fontSize = 12.sp
                                        )
                                    )
                                }

                                if(settings.timeClockEnabled) {
                                    Text(
                                        "${h}h ${m}m",
                                        style = TextStyle(
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}