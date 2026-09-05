package dev.tulis.readbear.routes.reader.pdf

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nucleusframework.pdfium.PdfPage
import dev.nucleusframework.pdfium.rememberPdfReaderState
import dev.tulis.readbear.R
import dev.tulis.readbear.db.Settings
import dev.tulis.readbear.db.books.Book
import dev.tulis.readbear.settings.BottomSettingsSheet
import dev.tulis.readbear.settings.PdfReadingLayout
import dev.tulis.readbear.settings.PdfSettingsContext
import dev.tulis.readbear.utils.LongText
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReader(
    viewModel: PdfReaderViewModel = hiltViewModel(),
    pdfId: Long,
    returnToMenu: () -> Unit,
) {
    val context = LocalContext.current
    val settingsFlow by Settings.getSettings(context).collectAsState(null)
    val settings = settingsFlow ?: return

    val flowComicWithBookmark by viewModel
        .getPdfWithBookmark(pdfId)
        .collectAsStateWithLifecycle(null)

    val view = LocalView.current
    var topBarVisible by remember { mutableStateOf(false) }

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

    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

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

    val pdfWithBookmark = flowComicWithBookmark ?: return
    var suspendBook: Book? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        suspendBook = viewModel.getBook(pdfWithBookmark.pdf.bookId)
        lastReadingTime = System.currentTimeMillis()
    }

    val book = suspendBook ?: return
    val filesDir = LocalContext.current.filesDir
    var finished by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) {
            topBarVisible = !topBarVisible
        }
    ) {
        val reader = rememberPdfReaderState()

        LaunchedEffect(Unit) {
            reader.open(filesDir.resolve(book.path).resolve("book.pdf").readBytes())
        }

        var count = reader.pageCount
        val splitPages = pdfWithBookmark.pdf.splitPages
        val readingLayout = if(splitPages) {
            PdfReadingLayout.PAGED
        } else {
            settings.pdfReadingLayout
        }

        if (readingLayout == PdfReadingLayout.PAGED || readingLayout == PdfReadingLayout.SPREAD) {
            if(readingLayout == PdfReadingLayout.SPREAD) count = ceil(count / 2f).toInt()
            if(splitPages) count *= 2
            val pagerState = rememberPagerState(
                initialPage = pdfWithBookmark.bookmark.page,
                pageCount = { count }
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when(readingLayout) {
                    PdfReadingLayout.PAGED -> {

                        if(splitPages) {
                            if(page % 2 == 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clipToBounds()
                                ) {
                                    PdfPage(
                                        state = reader,
                                        pageIndex = page / 2,
                                        onLinkClick = {
                                            if(it.destPageIndex != -1) {
                                                scope.launch {
                                                    pagerState.animateScrollToPage(it.destPageIndex * 2)
                                                }

                                                true
                                            } else {
                                                false
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight()
                                            .graphicsLayer {
                                                scaleX = 2f
                                                transformOrigin = TransformOrigin(0f, 0.5f)
                                            }
                                            .graphicsLayer {
                                                scaleY = 2f
                                                transformOrigin = TransformOrigin(0f, 0.5f)
                                            }
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clipToBounds()
                                ) {
                                    PdfPage(
                                        state = reader,
                                        pageIndex = page / 2,
                                        onLinkClick = {
                                            if(it.destPageIndex != -1) {
                                                scope.launch {
                                                    pagerState.animateScrollToPage(it.destPageIndex * 2)
                                                }

                                                true
                                            } else {
                                                false
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight()
                                            .graphicsLayer {
                                                scaleX = 2f
                                                scaleY = 2f
                                                transformOrigin = TransformOrigin(0.5f, 0.5f)
                                                translationX = -size.width / 2f
                                            }
                                    )
                                }
                            }
                        } else {
                            PdfPage(
                                state = reader,
                                pageIndex = page,
                                onLinkClick = {
                                    if(it.destPageIndex != -1) {
                                        scope.launch {
                                            pagerState.animateScrollToPage(it.destPageIndex)
                                        }

                                        true
                                    } else {
                                        false
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }
                    }

                    PdfReadingLayout.SPREAD -> {
                        val goTo: (Int) -> Unit = {
                            scope.launch {
                                pagerState.animateScrollToPage(it / 2)
                            }
                        }

                        Row {
                            PdfPage(
                                state = reader,
                                pageIndex = page * 2,
                                modifier = Modifier.weight(1f)
                            )

                            PdfPage(
                                state = reader,
                                pageIndex = page*2 +1,
                                modifier = Modifier.weight(1f),
                                onLinkClick = {
                                    if(it.destPageIndex != -1) {
                                        goTo(it.destPageIndex)

                                        true
                                    } else {
                                        false
                                    }
                                }
                            )
                        }
                    }
                }
            }

            LaunchedEffect(pagerState.settledPage) {
                snapshotFlow {
                    pagerState.settledPage
                }
                    .distinctUntilChanged()
                    .collect { settledPage ->
                        topBarVisible = false
                        if(finished) return@collect

                        bumpTime(book)

                        if(settledPage == book.totalProgress - 1) {
                            finished = true

                            val bookmark = pdfWithBookmark.bookmark
                            bookmark.page = 0
                            bookmark.pageOffset = 0

                            viewModel.updateBookmark(bookmark)

                            book.progress = 0
                            book.readAlready++
                            viewModel.updateBookProgress(book)
                            return@collect
                        }

                        val bookmark = pdfWithBookmark.bookmark

                        if (bookmark.page >= settledPage) {
                            return@collect
                        }

                        bookmark.page = settledPage
                        bookmark.pageOffset = 0
                        viewModel.updateBookmark(bookmark)

                        book.progress = if(readingLayout == PdfReadingLayout.SPREAD) {
                            settledPage * 2
                        } else settledPage
                        viewModel.updateBookProgress(book)
                    }
            }
        } else {
            // This check is relevant when switching from spread to continuous
            if(pdfWithBookmark.bookmark.page < book.progress) pdfWithBookmark.bookmark.page = book.progress
            viewModel.updateBookmark(pdfWithBookmark.bookmark)

            val listState = rememberLazyListState(pdfWithBookmark.bookmark.page, pdfWithBookmark.bookmark.pageOffset)

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

                        if(lastElement != null && lastElement == book.totalProgress - 1) {
                            finished = true

                            val bookmark = pdfWithBookmark.bookmark
                            bookmark.page = 0
                            bookmark.pageOffset = 0

                            viewModel.updateBookmark(bookmark)

                            book.progress = 0
                            book.readAlready++
                            viewModel.updateBookProgress(book)
                            return@collect
                        }

                        val bookmark = pdfWithBookmark.bookmark

                        if (bookmark.page >= index) {
                            if(bookmark.page == index) {
                                if(bookmark.pageOffset > offset) return@collect
                            } else {
                                return@collect
                            }
                        }

                        bookmark.page = index
                        bookmark.pageOffset = offset
                        viewModel.updateBookmark(bookmark)

                        book.progress = index
                        viewModel.updateBookProgress(book)
                    }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                items(count) { page ->
                    PdfPage(
                        state = reader,
                        pageIndex = page,
                        onLinkClick = {
                            if(it.destPageIndex != -1) {
                                scope.launch {
                                    listState.animateScrollToItem(it.destPageIndex)
                                }

                                true
                            } else {
                                false
                            }
                        }
                    )
                }
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
                        Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.go_back))
                    }
                },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LongText(book.title, modifier = Modifier.padding(4.dp))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showSheet = true

                        scope.launch {
                            awaitFrame()
                            awaitFrame()
                            awaitFrame()
                            awaitFrame()
                            awaitFrame()
                            sheetState.show()
                        }
                    }) {
                        Icon(Icons.Default.Settings, stringResource(R.string.settings))
                    }
                }
            )
        }

        if(showSheet) {
            BottomSettingsSheet(
                defaultTabOpen = 1,
                sheetState = sheetState,
                additionalContext = PdfSettingsContext(pdfId = pdfId),
                onHide = {
                    scope.launch {
                        sheetState.hide()
                        showSheet = false
                    }
                }
            )
        }
    }
}