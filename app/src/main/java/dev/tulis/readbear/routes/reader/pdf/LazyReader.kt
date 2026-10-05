package dev.tulis.readbear.routes.reader.pdf

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import dev.nucleusframework.pdfium.PdfPage
import dev.nucleusframework.pdfium.PdfReaderState
import dev.tulis.readbear.db.Settings
import dev.tulis.readbear.utils.readingClock
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun LazyReader(
    settings: Settings.SettingsState,
    reader: PdfReaderState,
    pdfState: PdfState,
    viewModel: PdfReaderViewModel = hiltViewModel()
) {
    val book = pdfState.book
    val bookmark = pdfState.pdfWithBookmark.bookmark

    // This check is relevant when switching from spread to continuous TODO()
    if (bookmark.page < book.progress) bookmark.page = book.progress
    viewModel.updateBookmark(bookmark)

    val listState = rememberLazyListState(
        bookmark.page,
        bookmark.pageOffset
    )

    val scope = rememberCoroutineScope()
    val clock = readingClock(book)

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
                pdfState.onChangeTopBarVisible(false)

                if (pdfState.finished) return@collect

                clock()

                val bookmark = pdfState.pdfWithBookmark.bookmark

                if (lastElement != null && lastElement == book.totalProgress - 1) {
                    pdfState.onChangeFinished(true)


                    bookmark.page = 0
                    bookmark.pageOffset = 0

                    viewModel.updateBookmark(bookmark)

                    book.progress = 0
                    book.readAlready++
                    viewModel.updateBook(book)
                    return@collect
                }

                if (bookmark.page >= index && !settings.allowReversingProgress) {
                    if (bookmark.page == index) {
                        if (bookmark.pageOffset > offset) return@collect
                    } else {
                        return@collect
                    }
                }

                bookmark.page = index
                bookmark.pageOffset = offset
                viewModel.updateBookmark(bookmark)

                book.progress = index
                viewModel.updateBook(book)
            }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(pdfState.count) { page ->
            PdfPage(
                state = reader,
                pageIndex = page,
                onLinkClick = {
                    if (it.destPageIndex != -1) {
                        scope.launch {
                            listState.animateScrollToItem(it.destPageIndex)
                        }

                        true
                    } else {
                        false
                    }
                },
                selectableText = true
            )
        }
    }
}