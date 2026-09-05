package dev.tulis.readbear.routes.reader.comic

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import dev.tulis.readbear.db.books.Book
import dev.tulis.readbear.db.comics.pages.ComicPage
import dev.tulis.readbear.utils.zip.ZipImage
import dev.tulis.readbear.utils.zip.ZipImageFetcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebtoonReader(
    comicId: Long,
    viewModel: WebtoonReaderViewModel = hiltViewModel(),
    returnToMenu: () -> Unit,
) {
    var panels by remember { mutableStateOf<List<ComicPage>>(emptyList()) }
    val flowComicWithBookmark by viewModel
        .getComicWithBookmark(comicId)
        .collectAsStateWithLifecycle(null)

    val view = LocalView.current
    var topBarVisible by remember { mutableStateOf(false) }

    DisposableEffect(topBarVisible) {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)

        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if(!topBarVisible) {
            controller.hide(WindowInsetsCompat.Type.navigationBars())
        } else {
            controller.show(WindowInsetsCompat.Type.navigationBars())
        }

        onDispose {
            controller.show(WindowInsetsCompat.Type.navigationBars())
        }
    }

    val comicWithBookmark = flowComicWithBookmark ?: return

    var suspendBook: Book? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        panels = viewModel.getPanels(comicId)
        suspendBook = viewModel.getBook(comicWithBookmark.comic.bookId)
    }

    val book = suspendBook ?: return

    var finished by remember { mutableStateOf(false) }
    var focusedPanel: ZipImage? by remember { mutableStateOf(null) }

    Box {
        val listState = rememberLazyListState()

        LaunchedEffect(Unit) {
            finished = false

            listState.scrollToItem(
                index = comicWithBookmark.bookmark.panelNumber,
                scrollOffset = comicWithBookmark.bookmark.panelOffset
            )
        }

        LaunchedEffect(listState) {
            snapshotFlow {
                Triple(
                    listState.firstVisibleItemIndex,
                    listState.firstVisibleItemScrollOffset,
                    listState.layoutInfo.visibleItemsInfo.lastOrNull()
                )
            }
                .distinctUntilChanged()
                .collect { (index, offset, lastElement) ->
                    topBarVisible = false

                    if(finished) return@collect

                    if(lastElement != null && lastElement.index == comicWithBookmark.comic.panels - 1) {
                        finished = true

                        val bookmark = comicWithBookmark.bookmark
                        bookmark.panelNumber = 0
                        bookmark.panelOffset = 0

                        viewModel.updateBookmark(bookmark)

                        book.progress = 0
                        book.readAlready++
                        viewModel.updateBookProgress(book)
                        return@collect
                    }

                    val bookmark = comicWithBookmark.bookmark

                    if (bookmark.panelNumber >= index) {
                        if(bookmark.panelNumber == index) {
                            if(bookmark.panelOffset > offset) return@collect
                        } else {
                            return@collect
                        }
                    }

                    bookmark.panelNumber = index
                    bookmark.panelOffset = offset
                    viewModel.updateBookmark(bookmark)

                    book.progress = index
                    viewModel.updateBookProgress(book)
                }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize(),
            userScrollEnabled = focusedPanel == null
        ) {
            items(panels) { page ->
                val imageLoader = ImageLoader.Builder(LocalContext.current)
                    .components {
                        add(ZipImageFetcher.Factory())
                    }
                    .build()


                val currentPanel = ZipImage(
                    zipFile = viewModel.zipFile!!,
                    path = page.path
                )

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(
                            currentPanel
                        )
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(page.width.toFloat() / page.height.toFloat())
                        .combinedClickable(
                            onClick = {
                                if(focusedPanel == null) topBarVisible = !topBarVisible
                            },
                            onLongClick = {
                                if(focusedPanel == null) focusedPanel = currentPanel
                            },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ),
                    imageLoader = imageLoader,
                    contentScale = ContentScale.FillWidth
                )
            }
        }

        AnimatedVisibility(
            visible = topBarVisible,
            enter = slideInVertically(
                initialOffsetY = { -it }
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { -it }
            ) + fadeOut()
        ) {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = returnToMenu) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "")
                    }
                },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(book.title)
                    }
                }
            )
        }

        val savedFocusedPanel = focusedPanel

        AnimatedVisibility(
            savedFocusedPanel != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)

            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.show(WindowInsetsCompat.Type.navigationBars())

            BackHandler {
                focusedPanel = null
            }

            DisposableEffect(Unit) {
                onDispose {
                    controller.hide(WindowInsetsCompat.Type.navigationBars())
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusedPanel = null
                    },
                verticalArrangement = Arrangement.Center
            ) {
                var scale by remember { mutableFloatStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }

                val scope = rememberCoroutineScope()

                val scaleAnimation = remember { Animatable(1f) }
                val offsetXAnimation = remember { Animatable(0f) }
                val offsetYAnimation = remember { Animatable(0f) }

                val minScale = 1f
                val maxScale = 8f

                fun calculateMaxOffset(
                    scale: Float,
                    width: Float,
                    height: Float
                ): Offset {
                    val maxX = width * (scale - 1f) / 2f
                    val maxY = height * (scale - 1f) / 2f
                    return Offset(maxX, maxY)
                }

                Box(
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTransformGestures(
                                panZoomLock = false
                            ) { centroid, pan, zoom, _ ->

                                if (scaleAnimation.isRunning) {
                                    scope.launch {
                                        scaleAnimation.stop()
                                        offsetXAnimation.stop()
                                        offsetYAnimation.stop()
                                    }
                                }

                                val oldScale = scale

                                val newScale = (
                                        oldScale * zoom
                                        ).coerceIn(
                                        minScale,
                                        maxScale
                                    )

                                val scaleChange = newScale / oldScale
                                val center = Offset(
                                    size.width / 2f,
                                    size.height / 2f
                                )

                                val centroidFromCenter =
                                    centroid - center

                                var newOffset =
                                    offset -
                                            centroidFromCenter * (scaleChange - 1f) +
                                            pan

                                if (newScale <= minScale) {

                                    newOffset = Offset.Zero

                                } else {

                                    val maxOffset = calculateMaxOffset(
                                        newScale,
                                        size.width.toFloat(),
                                        size.height.toFloat()
                                    )

                                    newOffset = Offset(
                                        x = newOffset.x.coerceIn(
                                            -maxOffset.x,
                                            maxOffset.x
                                        ),
                                        y = newOffset.y.coerceIn(
                                            -maxOffset.y,
                                            maxOffset.y
                                        )
                                    )
                                }

                                scale = newScale
                                offset = newOffset

                                scope.launch {
                                    scaleAnimation.snapTo(newScale)
                                    offsetXAnimation.snapTo(newOffset.x)
                                    offsetYAnimation.snapTo(newOffset.y)
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = { tapPosition ->
                                    val currentScale = scale

                                    val targetScale =
                                        if (currentScale <= minScale + 0.01f) {
                                            2f
                                        } else {
                                            1f
                                        }

                                    scope.launch {
                                        scaleAnimation.stop()
                                        offsetXAnimation.stop()
                                        offsetYAnimation.stop()

                                        scaleAnimation.snapTo(currentScale)
                                        offsetXAnimation.snapTo(offset.x)
                                        offsetYAnimation.snapTo(offset.y)

                                        val center = Offset(
                                            size.width / 2f,
                                            size.height / 2f
                                        )

                                        val scaleChange =
                                            targetScale / currentScale

                                        val tapFromCenter =
                                            tapPosition - center

                                        var targetOffset =
                                            offset -
                                                    tapFromCenter *
                                                    (scaleChange - 1f)

                                        if (targetScale <= minScale) {

                                            targetOffset = Offset.Zero

                                        } else {

                                            val maxOffset = calculateMaxOffset(
                                                targetScale,
                                                size.width.toFloat(),
                                                size.height.toFloat()
                                            )

                                            targetOffset = Offset(
                                                x = targetOffset.x.coerceIn(
                                                    -maxOffset.x,
                                                    maxOffset.x
                                                ),
                                                y = targetOffset.y.coerceIn(
                                                    -maxOffset.y,
                                                    maxOffset.y
                                                )
                                            )
                                        }

                                        coroutineScope {

                                            launch {
                                                scaleAnimation.animateTo(
                                                    targetScale,
                                                    animationSpec = spring(
                                                        dampingRatio =
                                                            Spring.DampingRatioNoBouncy,
                                                        stiffness =
                                                            Spring.StiffnessMediumLow
                                                    )
                                                )
                                            }

                                            launch {
                                                offsetXAnimation.animateTo(
                                                    targetOffset.x,
                                                    animationSpec = spring(
                                                        dampingRatio =
                                                            Spring.DampingRatioNoBouncy,
                                                        stiffness =
                                                            Spring.StiffnessMediumLow
                                                    )
                                                )
                                            }

                                            launch {
                                                offsetYAnimation.animateTo(
                                                    targetOffset.y,
                                                    animationSpec = spring(
                                                        dampingRatio =
                                                            Spring.DampingRatioNoBouncy,
                                                        stiffness =
                                                            Spring.StiffnessMediumLow
                                                    )
                                                )
                                            }
                                        }

                                        scale = targetScale
                                        offset = targetOffset

                                        scaleAnimation.snapTo(targetScale)
                                        offsetXAnimation.snapTo(targetOffset.x)
                                        offsetYAnimation.snapTo(targetOffset.y)
                                    }
                                }
                            )
                        }
                        .graphicsLayer {
                            scaleX = scaleAnimation.value
                            scaleY = scaleAnimation.value

                            transformOrigin = TransformOrigin(
                                0.5f,
                                0.5f
                            )

                            translationX = offsetXAnimation.value
                            translationY = offsetYAnimation.value
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val imageLoader = ImageLoader.Builder(LocalContext.current)
                        .components {
                            add(ZipImageFetcher.Factory())
                        }
                        .build()

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(
                                savedFocusedPanel
                            )
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        imageLoader = imageLoader,
                        contentScale = ContentScale.FillWidth
                    )
                }
            }
        }
    }
}