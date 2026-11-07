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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import dev.tulis.readbear.db.Settings
import dev.tulis.readbear.db.entities.books.Book
import dev.tulis.readbear.db.entities.comics.pages.ComicPage
import dev.tulis.readbear.utils.NavigationBars
import dev.tulis.readbear.utils.extensions.zoomableWithTap
import dev.tulis.readbear.utils.zip.ZipImage
import dev.tulis.readbear.utils.zip.ZipImageFetcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import kotlin.collections.emptyList
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebtoonReader(
    viewModel: WebtoonReaderViewModel = hiltViewModel(),
    comicId: Long,
    returnToMenu: () -> Unit,
) {
    val context = LocalContext.current

    val settings = Settings.getSettings(context).collectAsState(null).value ?: return
    val comicWithBookmark = viewModel.getComicWithBookmark(comicId).collectAsStateWithLifecycle(null).value ?: return
    val book = viewModel.getBook(comicWithBookmark.comic.bookId).collectAsStateWithLifecycle(null).value ?: return

    var topBarVisible by rememberSaveable { mutableStateOf(false) }
    NavigationBars(topBarVisible)

    LaunchedEffect(comicId) {
        viewModel.getPanels(comicId)
    }

    var finished by rememberSaveable { mutableStateOf(false) }

    var lastReadingTime by remember {
        mutableLongStateOf(System.currentTimeMillis())
    }

    val bumpTime: (Book) -> Unit = { book ->
        val currentTime = System.currentTimeMillis()

        if(currentTime - lastReadingTime > 600 * 1000) lastReadingTime = System.currentTimeMillis()
        book.readingTime += currentTime - lastReadingTime
        viewModel.updateBookProgress(book)

        lastReadingTime = currentTime
    }

    Box {
        val listState = rememberLazyListState()

        LaunchedEffect(Unit) {
            book.lastReadAt = Clock.System.now()

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
                    listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                )
            }
                .distinctUntilChanged()
                .collect { (index, offset, lastElement) ->
                    topBarVisible = false

                    if(finished) return@collect
                    bumpTime(book)

                    if(lastElement != null && index == comicWithBookmark.comic.panels - 1) {
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

                    if (bookmark.panelNumber >= index && !settings.allowReversingProgress) {
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
                .fillMaxSize()
                .zoomableWithTap(
                    zoomState = rememberZoomState()
                ) {
                    topBarVisible = !topBarVisible
                }
        ) {
            items(viewModel.panels) { page ->
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
                        .aspectRatio(page.width.toFloat() / page.height.toFloat()),
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
    }
}