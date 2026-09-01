package dev.tulis.readbear.routes.reader.pdf

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import dev.tulis.readbear.db.books.Book
import dev.tulis.readbear.utils.LongText
import io.github.yuroyami.kitepdf.PdfDocument
import io.github.yuroyami.kitepdf.compose.KiteDocView
import io.github.yuroyami.kitepdf.compose.rememberKiteDocViewState
import io.github.yuroyami.kitepdf.core.KiteBookmark
import io.github.yuroyami.kitepdf.core.KiteLocation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReader(
    pdfId: Long,
    viewModel: PdfReaderViewModel = hiltViewModel(),
    returnToMenu: () -> Unit,
) {
    val flowComicWithBookmark by viewModel
        .getPdfWithBookmark(pdfId)
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

    val pdfWithBookmark = flowComicWithBookmark ?: return
    var suspendBook: Book? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        suspendBook = viewModel.getBook(pdfWithBookmark.pdf.bookId)
    }

    val book = suspendBook ?: return
    val filesDir = LocalContext.current.filesDir
    var finished by remember { mutableStateOf(false) }
    var lastPage = pdfWithBookmark.bookmark.page

    Box {
        val doc = PdfDocument.open(filesDir.resolve(book.path).resolve("book.pdf").readBytes())
        val bookState = rememberKiteDocViewState(doc, lastPage)

        KiteDocView(
            state = bookState,
            modifier = Modifier.fillMaxSize(),
            onTap = {
                topBarVisible = !topBarVisible
            },
            pageSpacing = 0.dp,
            pagePlaceholder = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        )

        LaunchedEffect(Unit) {
            snapshotFlow { bookState.currentPage }
                .distinctUntilChanged()
                .collect { page ->
                    if(finished) return@collect

                    if(page > lastPage) {
                        pdfWithBookmark.bookmark.page = page
                        viewModel.updateBookmark(pdfWithBookmark.bookmark)

                        book.progress = page
                        viewModel.updateBookProgress(book)

                        lastPage = page
                    }

                    if(page == book.totalProgress - 1) {
                        finished = true

                        pdfWithBookmark.bookmark.page = 0
                        viewModel.updateBookmark(pdfWithBookmark.bookmark)

                        book.progress = 0
                        book.readAlready++
                        viewModel.updateBookProgress(book)
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
                }
            )
        }
    }
}