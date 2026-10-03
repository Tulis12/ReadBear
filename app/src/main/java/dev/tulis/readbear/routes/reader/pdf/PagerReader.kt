package dev.tulis.readbear.routes.reader.pdf

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.hilt.navigation.compose.hiltViewModel
import dev.nucleusframework.pdfium.PdfPage
import dev.nucleusframework.pdfium.PdfReaderState
import dev.tulis.readbear.db.Settings
import dev.tulis.readbear.settings.PdfReadingLayout
import dev.tulis.readbear.utils.readingClock
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.ceil

@Composable
fun PagerReader(
    settings: Settings.SettingsState,
    reader: PdfReaderState,
    pdfState: PdfState,
    viewModel: PdfReaderViewModel = hiltViewModel()
) {
    var count = pdfState.count
    val scope = rememberCoroutineScope()

    if (pdfState.readingLayout == PdfReadingLayout.SPREAD) count = ceil(count / 2f).toInt()
    if (pdfState.splitPages) count *= 2
    val pagerState = rememberPagerState(
        initialPage = pdfState.pdfWithBookmark.bookmark.page,
        pageCount = { count }
    )

    val clock = readingClock(pdfState.book)

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
    ) { page ->
        when (pdfState.readingLayout) {
            PdfReadingLayout.PAGED -> {

                if (pdfState.splitPages) {
                    if (page % 2 == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clipToBounds()
                        ) {
                            PdfPage(
                                state = reader,
                                selectableText = true,
                                pageIndex = page / 2,
                                onLinkClick = {
                                    if (it.destPageIndex != -1) {
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
                                selectableText = true,
                                pageIndex = page / 2,
                                onLinkClick = {
                                    if (it.destPageIndex != -1) {
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
                                        transformOrigin =
                                            TransformOrigin(0.5f, 0.5f)
                                        translationX = -size.width / 2f
                                    }
                            )
                        }
                    }
                } else {
                    PdfPage(
                        state = reader,
                        pageIndex = page,
                        selectableText = true,
                        onLinkClick = {
                            if (it.destPageIndex != -1) {
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
                        modifier = Modifier.weight(1f),
                        selectableText = true
                    )

                    PdfPage(
                        state = reader,
                        pageIndex = page * 2 + 1,
                        modifier = Modifier.weight(1f),
                        onLinkClick = {
                            if (it.destPageIndex != -1) {
                                goTo(it.destPageIndex)

                                true
                            } else {
                                false
                            }
                        },
                        selectableText = true
                    )
                }
            }

            PdfReadingLayout.CONTINUOUS -> error("Impossible.")
        }
    }

    LaunchedEffect(pagerState.settledPage) {
        snapshotFlow {
            pagerState.settledPage
        }
            .distinctUntilChanged()
            .collect { settledPage ->
                val book = pdfState.book

                pdfState.onChangeTopBarVisible(false)
                if (pdfState.finished) return@collect

                clock()

                if (settledPage == book.totalProgress - 1) {
                    pdfState.onChangeFinished(true)

                    val bookmark = pdfState.pdfWithBookmark.bookmark
                    bookmark.page = 0
                    bookmark.pageOffset = 0

                    viewModel.updateBookmark(bookmark)

                    book.progress = 0
                    book.readAlready++
                    viewModel.updateBookProgress(book)
                    return@collect
                }

                val bookmark = pdfState.pdfWithBookmark.bookmark

                if (bookmark.page >= settledPage && !settings.allowReversingProgress) {
                    return@collect
                }

                bookmark.page = settledPage
                bookmark.pageOffset = 0
                viewModel.updateBookmark(bookmark)

                book.progress = if (pdfState.readingLayout == PdfReadingLayout.SPREAD) {
                    settledPage * 2
                } else settledPage

                viewModel.updateBookProgress(book)
            }
    }
}