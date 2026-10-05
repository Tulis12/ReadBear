package dev.tulis.readbear.routes.menu.snippets.utils

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
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
import dev.tulis.readbear.db.entities.books.Book
import dev.tulis.readbear.db.entities.snippets.Snippet
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import java.io.File
import java.util.UUID

@Composable
fun snippetScreenshooter(
    viewModel: SnippetViewModel = hiltViewModel(),
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
        var name: String? by remember { mutableStateOf(null) }
        var description: String? by remember { mutableStateOf(null) }

        val savedScreenshot = screenshot

        if (showDialog && savedScreenshot != null) {
            var nameError by remember { mutableStateOf(false) }

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
                            modifier = Modifier
                                .fillMaxHeight(0.4f)
                                .fillMaxWidth(),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = name ?: "",
                            onValueChange = { name = it },
                            label = { Text(stringResource(R.string.name)) },
                            isError = nameError,
                            supportingText = {
                                if(nameError) {
                                    Text(stringResource(R.string.name_not_empty))
                                } else {
                                    Text("")
                                }
                            }
                        )

                        OutlinedTextField(
                            value = description ?: "",
                            onValueChange = {
                                if(it.isBlank()) description = null
                                description = it
                            },
                            label = { Text(stringResource(R.string.description)) }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val savedName = name

                            if(savedName.isNullOrBlank()) {
                                nameError = true
                                return@TextButton
                            }

                            showDialog = false

                            val snippetFileName = UUID.randomUUID().toString()
                            val file = File(snippetsDir, "${snippetFileName}.png")

                            val snippet = Snippet(
                                name = savedName,
                                description = description,
                                path = "${snippetFileName}.png",
                                bookId = book.id,
//                                page = 0,
//                                pageProgress = 0, //progress.toLong()
                                progress = 0
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
        var activeHandle by remember { mutableStateOf<SelectionHandle?>(null) }
        var isDraggingRect by remember { mutableStateOf(false) }

        if (isSelecting && screenshot != null) {
            val bitmap = screenshot!!

            BackHandler {
                screenshot = null
                isSelecting = false
            }

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

                val strokeColor = MaterialTheme.colorScheme.outline

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                        .pointerInput(bitmap) {
                            detectDragGestures(
                                onDragStart = { start ->
                                    val rect = selection

                                    if(rect != null) {
                                        activeHandle = isHandle(
                                            rect = rect,
                                            point = start
                                        )

                                        if(activeHandle == null && rect.contains(start)) {
                                            isDraggingRect = true
                                        }

                                        return@detectDragGestures
                                    }

                                    dragStart = start

                                    selection = Rect(
                                        left = start.x,
                                        top = start.y,
                                        right = start.x,
                                        bottom = start.y
                                    )
                                },

                                onDrag = { change, _ ->
                                    val sel = selection

                                    if (sel != null) {
                                        if(activeHandle != null) {
                                            when (activeHandle) {
                                                SelectionHandle.TOP_LEFT -> {
                                                    selection = Rect(
                                                        left = change.position.x,
                                                        top = change.position.y,
                                                        right = sel.right,
                                                        bottom = sel.bottom
                                                    )
                                                }

                                                SelectionHandle.TOP_RIGHT -> {
                                                    selection = Rect(
                                                        left = sel.left,
                                                        top = change.position.y,
                                                        right = change.position.x,
                                                        bottom = sel.bottom
                                                    )
                                                }

                                                SelectionHandle.BOTTOM_LEFT -> {
                                                    selection = Rect(
                                                        left = change.position.x,
                                                        top = sel.top,
                                                        right = sel.right,
                                                        bottom = change.position.y
                                                    )
                                                }

                                                SelectionHandle.BOTTOM_RIGHT -> {
                                                    selection = Rect(
                                                        left = sel.left,
                                                        top = sel.top,
                                                        right = change.position.x,
                                                        bottom = change.position.y
                                                    )
                                                }

                                                else -> {}
                                            }

                                            return@detectDragGestures
                                        } else if(isDraggingRect) {
                                            println("Test")

                                            val delta = change.position - change.previousPosition

                                            selection = Rect(
                                                left = sel.left + delta.x,
                                                top = sel.top + delta.y,
                                                right = sel.right + delta.x,
                                                bottom = sel.bottom + delta.y
                                            )

                                            return@detectDragGestures
                                        }


                                    }

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
                                    activeHandle = null
                                    isDraggingRect = false
                                },

                                onDragCancel = {
                                    dragStart = null
                                    activeHandle = null
                                    isDraggingRect = false
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

                        listOf(
                            Pair(rect.topLeft, SelectionHandle.TOP_LEFT),
                            Pair(rect.topRight, SelectionHandle.TOP_RIGHT),
                            Pair(rect.bottomLeft, SelectionHandle.BOTTOM_LEFT),
                            Pair(rect.bottomRight, SelectionHandle.BOTTOM_RIGHT)
                        ).forEach {
                            val r = Rect(center = it.first, radius = 15f)

                            drawRect(
                                color = Color.White,
                                topLeft = r.topLeft,
                                size = r.size
                            )

                            drawRect(
                                color = strokeColor,
                                topLeft = r.topLeft,
                                size = r.size,
                                style = Stroke(3f)
                            )
                        }
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

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(80.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(15.dp),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(15.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(8.dp)
                    ) {
                        TextButton(
                            onClick = {
                                screenshot = null
                                isSelecting = false
                            })
                        {
                            Text(stringResource(R.string.cancel))
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
                            })
                        {
                            Text(stringResource(R.string.save))
                        }
                    }
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

enum class SelectionHandle {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
}


fun isHandle(rect: Rect, point: Offset, size: Float = 80f): SelectionHandle? {
    val topLeftHitbox = Rect(center = rect.topLeft, radius = size)
    val topRightHitbox = Rect(center = rect.topRight, radius = size)
    val bottomLeftHitbox = Rect(center = rect.bottomLeft, radius = size)
    val bottomRightHitbox = Rect(center = rect.bottomRight, radius = size)

    if(topLeftHitbox.contains(point)) {
        return SelectionHandle.TOP_LEFT
    }

    if(topRightHitbox.contains(point)) {
        return SelectionHandle.TOP_RIGHT
    }

    if(bottomLeftHitbox.contains(point)) {
        return SelectionHandle.BOTTOM_LEFT
    }

    if(bottomRightHitbox.contains(point)) {
        return SelectionHandle.BOTTOM_RIGHT
    }

    return null
}