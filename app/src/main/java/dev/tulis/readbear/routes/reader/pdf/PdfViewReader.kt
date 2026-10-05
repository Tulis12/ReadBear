package dev.tulis.readbear.routes.reader.pdf

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoSizeSelectLarge
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nucleusframework.pdfium.rememberPdfReaderState
import dev.tulis.readbear.R
import dev.tulis.readbear.db.Settings
import dev.tulis.readbear.db.entities.books.Book
import dev.tulis.readbear.settings.BottomSettingsSheet
import dev.tulis.readbear.settings.PdfReadingLayout
import dev.tulis.readbear.settings.PdfSettingsContext
import dev.tulis.readbear.utils.LongText
import dev.tulis.readbear.routes.menu.snippets.utils.snippetScreenshooter
import dev.tulis.readbear.utils.NavigationBars
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReader(
    viewModel: PdfReaderViewModel = hiltViewModel(),
    pdfId: Long,
    returnToMenu: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settingsFlow by Settings.getSettings(context).collectAsState(null)
    val settings = settingsFlow ?: return

    val flowComicWithBookmark by viewModel
        .getPdfWithBookmark(pdfId)
        .collectAsStateWithLifecycle(null)

    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var topBarVisible by remember { mutableStateOf(true) }
    NavigationBars(topBarVisible)

    val pdfWithBookmark = flowComicWithBookmark ?: return
    val flowBook: Book? by viewModel.getBookFlow(pdfWithBookmark.pdf.bookId).collectAsStateWithLifecycle(null)

    val book = flowBook ?: return
    val filesDir = LocalContext.current.filesDir
    var finished by remember { mutableStateOf(false) }


    val reader = rememberPdfReaderState()

    LaunchedEffect(Unit) {
        reader.open(filesDir.resolve(book.path).resolve("book.pdf").readBytes())
    }

    val splitPages = pdfWithBookmark.pdf.splitPages
    val readingLayout = if(splitPages) {
        PdfReadingLayout.PAGED
    } else {
        settings.pdfReadingLayout
    }

    Box {
        val pdfState = PdfState(
            book = book,
            pdfWithBookmark = pdfWithBookmark,
            readingLayout = readingLayout,
            count = reader.pageCount,
            topBarVisible = topBarVisible,
            splitPages = splitPages,
            finished = finished,
            onChangeFinished = {
                finished = it
            },
            onChangeTopBarVisible = {
                topBarVisible = it
            }
        )

        var currentPage by remember { mutableIntStateOf(0) }

        val screenshot = snippetScreenshooter(
            book = book,
            progress = currentPage
        ) {
            Box(
                modifier = Modifier.zoomable(zoomState = rememberZoomState(), onTap = {
                    topBarVisible = !topBarVisible
                })
            ) {
                if (readingLayout == PdfReadingLayout.PAGED || readingLayout == PdfReadingLayout.SPREAD) {
                    PagerReader(settings, reader, pdfState)
                    return@Box
                }

                LazyReader(settings, reader, pdfState)
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
                            repeat(5) { awaitFrame() }
                            sheetState.show()
                        }
                    }) {
                        Icon(
                            Icons.Default.Settings,
                            stringResource(R.string.settings)
                        )
                    }

                    IconButton(onClick = {
                        screenshot()
                    }) {
                        Icon(
                            Icons.Default.PhotoSizeSelectLarge,
                            stringResource(R.string.quote)
                        )
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