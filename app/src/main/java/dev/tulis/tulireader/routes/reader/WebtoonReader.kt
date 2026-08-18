package dev.tulis.tulireader.routes.reader

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import dev.tulis.tulireader.db.pages.Page
import dev.tulis.tulireader.utils.zip.ZipImage
import dev.tulis.tulireader.utils.zip.ZipImageFetcher
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebtoonReader(
    bookId: Long,
    viewModel: WebtoonReaderViewModel = hiltViewModel(),
    returnToMenu: () -> Unit,
) {
    var pages by remember { mutableStateOf<List<Page>>(emptyList()) }
    val flowBookWithBookmark by viewModel
        .getBookWithBookmark(bookId)
        .collectAsStateWithLifecycle(null)

    val view = LocalView.current

    LaunchedEffect(Unit) {
        pages = viewModel.getPages(bookId)
    }

    DisposableEffect(Unit) {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)

        controller.hide(WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        onDispose {
            controller.show(WindowInsetsCompat.Type.navigationBars())
        }
    }

    val bookWithBookmark = flowBookWithBookmark ?: return
    var finished = false

    var visible by remember { mutableStateOf(false) }

    Box {
        val listState = rememberLazyListState()

        LaunchedEffect(Unit) {
            finished = false

            listState.scrollToItem(
                index = bookWithBookmark.bookmark.pageNumber,
                scrollOffset = bookWithBookmark.bookmark.pageOffset
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
                    visible = false

                    if(finished) return@collect

                    if(lastElement != null && lastElement.index == bookWithBookmark.book.pages - 1) {
                        finished = true

                        val bookmark = bookWithBookmark.bookmark
                        bookmark.pageNumber = 0
                        bookmark.pageOffset = 0
                        bookmark.readAlready++
                        viewModel.updateBookmark(bookmark)

                        return@collect
                    }

                    val bookmark = bookWithBookmark.bookmark

                    if (bookmark.pageNumber >= index) {
                        if(bookmark.pageNumber == index) {
                            if(bookmark.pageOffset > offset) return@collect
                        } else {
                            return@collect
                        }
                    }

                    bookmark.pageNumber = index
                    bookmark.pageOffset = offset

                    viewModel.updateBookmark(bookmark)
                }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(pages) { page ->

                val imageLoader = ImageLoader.Builder(LocalContext.current)
                    .components {
                        add(ZipImageFetcher.Factory())
                    }
                    .build()

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(
                            ZipImage(
                                zipFile = viewModel.zipFile!!,
                                path = page.path
                            )
                        )
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(page.width.toFloat() / page.height.toFloat())
                        .combinedClickable(
                            onClick = {
                                visible = !visible
                            },
                            onLongClick = {

                            },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ),
                    imageLoader = imageLoader,
                    contentScale = ContentScale.FillWidth
                )
            }
        }

        AnimatedVisibility(visible) {
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
                        Text(bookWithBookmark.book.title)
                    }
                }
            )
        }
    }
}