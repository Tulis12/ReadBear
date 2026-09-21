package dev.tulis.readbear.routes.reader.pdf

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.PhotoSizeSelectLarge
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tulis.readbear.R
import dev.tulis.readbear.db.Settings
import dev.tulis.readbear.db.books.Book
import dev.tulis.readbear.settings.BottomSettingsSheet
import dev.tulis.readbear.settings.PdfReadingLayout
import dev.tulis.readbear.settings.PdfSettingsContext
import dev.tulis.readbear.utils.LongText
import dev.tulis.readbear.utils.quoteScreenshooter
import io.github.yuroyami.kitepdf.PdfDocument
import io.github.yuroyami.kitepdf.compose.KiteDocLayout
import io.github.yuroyami.kitepdf.compose.KiteDocView
import io.github.yuroyami.kitepdf.compose.rememberKiteDocViewState
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

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
        val splitPages = pdfWithBookmark.pdf.splitPages
        val readingLayout = if(splitPages) {
            PdfReadingLayout.PAGED
        } else {
            settings.pdfReadingLayout
        }

        var doc: PdfDocument? by remember { mutableStateOf(null) }

        LaunchedEffect(Unit) {
            doc = PdfDocument.open(filesDir.resolve(book.path).resolve("book.pdf").readBytes())
        }

        val savedDoc = doc ?: return@Box
        val state = rememberKiteDocViewState(savedDoc, pdfWithBookmark.bookmark.page)

        val screenshot = quoteScreenshooter(book = book, progress = state.currentPage) {
            KiteDocView(
                state = state,
                layout = when(readingLayout) {
                    PdfReadingLayout.PAGED -> KiteDocLayout.Paged()
                    PdfReadingLayout.SPREAD -> KiteDocLayout.Spread()
                    PdfReadingLayout.CONTINUOUS -> KiteDocLayout.Continuous()
                },
                pageSpacing = if(readingLayout == PdfReadingLayout.CONTINUOUS) 0.dp else 8.dp,
                pagePlaceholder = {
                    Box(
                        modifier = Modifier.background(Color.White.copy(alpha = 0.5f)).fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                },
                onTap = {
                    topBarVisible = !topBarVisible
                },
                onLinkTap = { _ -> false },
            )
        }

        LaunchedEffect(state.currentLocation.page) {
            snapshotFlow {
                state.currentLocation.page
            }
                .distinctUntilChanged()
                .collect { page ->
                    topBarVisible = false
                    if(finished) return@collect

                    bumpTime(book)

                    if(page == book.totalProgress - 1) {
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

                    if (bookmark.page >= page && !settings.allowReversingProgress) {
                        return@collect
                    }

                    bookmark.page = page
                    bookmark.pageOffset = 0
                    viewModel.updateBookmark(bookmark)

                    book.progress = page
                    viewModel.updateBookProgress(book)
                }
        }

//        topBarVisible = false
//                        if(finished) return@collect
//
//                        bumpTime(book)
//
//                        if(settledPage == book.totalProgress - 1) {
//                            finished = true
//
//                            val bookmark = pdfWithBookmark.bookmark
//                            bookmark.page = 0
//                            bookmark.pageOffset = 0
//
//                            viewModel.updateBookmark(bookmark)
//
//                            book.progress = 0
//                            book.readAlready++
//                            viewModel.updateBookProgress(book)
//                            return@collect
//                        }
//
//                        val bookmark = pdfWithBookmark.bookmark
//
//                        if (bookmark.page >= settledPage && !settings.allowReversingProgress) {
//                            return@collect
//                        }
//
//                        bookmark.page = settledPage
//                        bookmark.pageOffset = 0
//                        viewModel.updateBookmark(bookmark)
//
//                        book.progress = if(readingLayout == PdfReadingLayout.SPREAD) {
//                            settledPage * 2
//                        } else settledPage
//                        viewModel.updateBookProgress(book)

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

                    IconButton(onClick = {
                        screenshot()
                    }) {
                        Icon(Icons.Default.PhotoSizeSelectLarge, stringResource(R.string.quote))
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