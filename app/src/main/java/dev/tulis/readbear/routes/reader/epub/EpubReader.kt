package dev.tulis.readbear.routes.reader.epub

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.BottomAppBar
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tulis.readbear.R
import dev.tulis.readbear.db.Settings
import dev.tulis.readbear.db.books.Book
import dev.tulis.readbear.utils.LongText
import java.io.FileInputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpubReader(
    viewModel: EpubReaderViewModel = hiltViewModel(),
    epubId: Long,
    returnToMenu: () -> Unit,
) {
    val context = LocalContext.current
    val settingsFlow by Settings.getSettings(context).collectAsState(null)
    val settings = settingsFlow ?: return

    val flowComicWithBookmark by viewModel
        .getEpubWithBookmark(epubId)
        .collectAsStateWithLifecycle(null)

    val view = LocalView.current
    var uiVisible by remember { mutableStateOf(false) }

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

    DisposableEffect(uiVisible) {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)

        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if(!uiVisible) {
            controller.hide(WindowInsetsCompat.Type.navigationBars())
        } else {
            controller.show(WindowInsetsCompat.Type.navigationBars())
        }

        onDispose {
            controller.show(WindowInsetsCompat.Type.navigationBars())
        }
    }

    val epubWithBookmark = flowComicWithBookmark ?: return
    var suspendBook: Book? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        suspendBook = viewModel.getBook(epubWithBookmark.epub.bookId)
        lastReadingTime = System.currentTimeMillis()
    }

    val book = suspendBook ?: return
    val filesDir = LocalContext.current.filesDir
    var finished by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize().clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) {
            uiVisible = !uiVisible
        }
    ) {
        val readerState = rememberEpubReaderState(
            page = epubWithBookmark.bookmark.page,
            pagePercentage = epubWithBookmark.bookmark.pagePercentage
        )

        val onScroll = onScroll@{
            if(finished) return@onScroll
            val index = readerState.currentPage
            val offset = readerState.currentPercentage

            bumpTime(book)

            if(readerState.currentPage == book.totalProgress - 1) {
                finished = true

                val bookmark = epubWithBookmark.bookmark
                bookmark.page = 0
                bookmark.pagePercentage = 0f

                viewModel.updateBookmark(bookmark)

                book.progress = 0
                book.readAlready++
                viewModel.updateBookProgress(book)
                return@onScroll
            }

            val bookmark = epubWithBookmark.bookmark

            if (bookmark.page >= index) {
                if(bookmark.page == index) {
                    if(bookmark.pagePercentage > offset) return@onScroll
                } else {
                    return@onScroll
                }
            }

            bookmark.page = index
            bookmark.pagePercentage = offset
            viewModel.updateBookmark(bookmark)

            book.progress = index
            viewModel.updateBookProgress(book)
        }

        LaunchedEffect(Unit) {
            readerState.load(FileInputStream(filesDir.resolve(book.path).resolve("book.epub")))
        }

        EpubPageReader(
            readerState,
            onTap = {
                uiVisible = !uiVisible
            },
            onScrollPercentageUpdate = {
                uiVisible = false
                onScroll()
            }
        )

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomCenter),
            visible = uiVisible,
            enter = slideInVertically(
                initialOffsetY = { it }
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it }
            ) + fadeOut()
        ) {
            BottomAppBar(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.chapter_x, readerState.currentPage+1),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = {
                            readerState.previousPage()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.previous_chapter))
                        }

                        IconButton(onClick = {
                            readerState.nextPage()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, stringResource(R.string.next_chapter))
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = uiVisible,
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
                }
            )
        }
    }
}