package dev.tulis.readbear.routes.menu.quotes.utils

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.tulis.readbear.R
import dev.tulis.readbear.db.books.Book
import dev.tulis.readbear.db.quotes.snippets.Snippet
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@Composable
fun quoteScreenshooter(
    viewModel: QuoteViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    book: Book,
    progress: Int,
    content: @Composable () -> Unit
): () -> Unit {
    var screenshot by remember { mutableStateOf<Bitmap?>(null) }
    var selection by remember { mutableStateOf<Rect?>(null) }

    var isSelecting by remember { mutableStateOf(false) }

    val graphicsLayer = rememberGraphicsLayer()
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val filesDir = LocalContext.current.filesDir
    val snippetsDir = filesDir.resolve("snippets")
    snippetsDir.mkdirs()

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .onSizeChanged {
                containerSize = it
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }

                    drawLayer(graphicsLayer)
                }
                .background(Color.White)
        ) {
            content()
        }

        var showDialog by remember { mutableStateOf(false) }
        var name by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }

        val savedScreenshot = screenshot

        if (showDialog && savedScreenshot != null) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                },
                title = {
                    Text(stringResource(R.string.add_snippet))
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            bitmap = savedScreenshot.asImageBitmap(),
                            stringResource(R.string.photo_snippet),
                            modifier = Modifier.fillMaxHeight(0.4f)
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(stringResource(R.string.name)) }
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text(stringResource(R.string.description)) }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDialog = false

                            val snippetFileName = UUID.randomUUID().toString()
                            val file = File(snippetsDir, "${snippetFileName}.png")

                            val snippet = Snippet(
                                name = name,
                                description = description,
                                path = "${snippetFileName}.png",
                                bookId = book.id,
                                progress = progress.toLong()
                            )

                            file.outputStream().use { output ->
                                savedScreenshot.compress(
                                    Bitmap.CompressFormat.PNG,
                                    100,
                                    output
                                )
                            }

                            viewModel.createSnippet(snippet)

                        }
                    ) {
                        Text(stringResource(R.string.save))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        var dragStart by remember { mutableStateOf<Offset?>(null) }

        if (isSelecting && screenshot != null) {
            val bitmap = screenshot!!

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                        .pointerInput(bitmap) {
                            detectDragGestures(
                                onDragStart = { start ->
                                    dragStart = start

                                    selection = Rect(
                                        left = start.x,
                                        top = start.y,
                                        right = start.x,
                                        bottom = start.y
                                    )
                                },

                                onDrag = { change, _ ->
                                    val start = dragStart ?: return@detectDragGestures

                                    selection = Rect(
                                        left = minOf(start.x, change.position.x),
                                        top = minOf(start.y, change.position.y),
                                        right = maxOf(start.x, change.position.x),
                                        bottom = maxOf(start.y, change.position.y)
                                    )
                                },

                                onDragEnd = {
                                    dragStart = null
                                },

                                onDragCancel = {
                                    dragStart = null
                                }
                            )
                        }
                ) {
                    drawRect(
                        Color.Black.copy(alpha = 0.5f)
                    )

                    selection?.let { rect ->
                        drawRect(
                            color = Color.Transparent,
                            topLeft = rect.topLeft,
                            size = rect.size,
                            blendMode = BlendMode.Clear
                        )

                        drawRect(
                            color = Color.White,
                            topLeft = rect.topLeft,
                            size = rect.size,
                            style = Stroke(
                                width = 2.dp.toPx()
                            )
                        )
                    }
                }

                if(selection == null) {
                    Text(
                        text = stringResource(R.string.selection_prompt),
                        color = Color.White,
                        autoSize = TextAutoSize.StepBased(),
                        maxLines = 1,
                        modifier = Modifier
                            .padding(top = 200.dp)
                            .fillMaxWidth(0.75f)
                            .align(Alignment.TopCenter)
                    )
                }

                Button(
                    enabled = selection != null,
                    onClick = {

                        val rect = selection
                            ?: return@Button

                        val imageSize = bitmap
                            .asImageBitmap()
                            .let {
                                Size(
                                    it.width.toFloat(),
                                    it.height.toFloat()
                                )
                            }

                        val containerSize = Size(
                            containerSize.width.toFloat(),
                            containerSize.height.toFloat()
                        )

                        val displayedWidth =
                            containerSize.width // - with(density) { 32.dp.toPx() }

                        val displayedHeight =
                            containerSize.height // - with(density) { 64.dp.toPx() }

                        val scale = minOf(
                            displayedWidth / imageSize.width,
                            displayedHeight / imageSize.height
                        )

                        val actualWidth =
                            imageSize.width * scale

                        val actualHeight =
                            imageSize.height * scale

                        val offsetX =
                            (containerSize.width - actualWidth) / 2f

                        val offsetY =
                            (containerSize.height - actualHeight) / 2f

                        val left = (
                                (rect.left - offsetX) / scale
                                ).coerceIn(
                                0f,
                                bitmap.width.toFloat()
                            )

                        val top = (
                                (rect.top - offsetY) / scale
                                ).coerceIn(
                                0f,
                                bitmap.height.toFloat()
                            )

                        val right = (
                                (rect.right - offsetX) / scale
                                ).coerceIn(
                                0f,
                                bitmap.width.toFloat()
                            )

                        val bottom = (
                                (rect.bottom - offsetY) / scale
                                ).coerceIn(
                                0f,
                                bitmap.height.toFloat()
                            )

                        val cropped = Bitmap.createBitmap(
                            bitmap,
                            left.toInt(),
                            top.toInt(),
                            (right - left).toInt(),
                            (bottom - top).toInt()
                        )

                        screenshot = cropped
                        isSelecting = false

                        showDialog = true
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(80.dp)
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }

    return invokeScreenshot@{
        if(!isSelecting) {
            scope.launch {
                screenshot = graphicsLayer
                    .toImageBitmap()
                    .asAndroidBitmap()

                selection = null
                isSelecting = true
            }
        }
    }
}