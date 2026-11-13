package dev.tulis.readbear.routes.reader.epub

import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import me.rmyhal.contentment.rememberIndicatorVisible
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.epub.EpubReader
import java.io.InputStream

@Stable
class EpubReaderState(
    page: Int,
    pagePercentage: Float
) {
    var bookEpub: Book? by mutableStateOf(null)
    var currentPage by mutableIntStateOf(page)
    var currentId: String? by mutableStateOf(null)
    var currentPercentage by mutableFloatStateOf(pagePercentage)
        private set

    var isReady by mutableStateOf(false)

    private val reader = EpubReader()

    fun load(inputStream: InputStream) {
        bookEpub = reader.readEpub(inputStream)
    }

    fun nextPage() {
        currentPage++
    }

    fun updatePercentage(percentage: Float) {
        currentPercentage = percentage
    }

    fun switchToPage(page: Int, id: String?) {
        currentPage = page
        currentId = id
    }

    fun previousPage() {
        if (currentPage > 0) {
            currentPage--
        }
    }
}

@Composable
fun rememberEpubReaderState(
    page: Int,
    pagePercentage: Float
): EpubReaderState {
    return remember {
        EpubReaderState(page, pagePercentage)
    }
}

@Composable
fun EpubPageReader(state: EpubReaderState, onTap: () -> Unit = {}, onScrollPercentageUpdate: (Float) -> Unit = {}) {
    var webView: WebView? by remember { mutableStateOf(null) }
    var scale by remember { mutableFloatStateOf(1f) }

    val book = state.bookEpub
    if(book == null) {
        CircularProgressIndicator()
        return
    }

    val spine = book.spine
    val resources = book.resources

    LaunchedEffect(state.currentPage) {
        val savedWebView = webView ?: return@LaunchedEffect

        val xhtml =
            spine.spineReferences[state.currentPage]
                .resource
                .inputStream
                .bufferedReader()
                .readText()

        savedWebView.loadDataWithBaseURL(
            "https://epub.local/",
            xhtml,
            "application/xhtml+xml",
            "UTF-8",
            null
        )
    }

    LaunchedEffect(state.currentId) {
        val id = state.currentId ?: return@LaunchedEffect

        val savedWebView = webView ?: return@LaunchedEffect

        savedWebView.postDelayed({
            savedWebView.evaluateJavascript(
                """
            (function waitForElement() {
                const element = document.getElementById("$id");
        
                if (element) {
                    element.scrollIntoView({
                        behavior: "instant",
                        block: "start"
                    });
                    return;
                }
        
                requestAnimationFrame(waitForElement);
            })();
            """.trimIndent(),
                null
            )
        }, 50)

        state.currentId = null
    }

    Box(
        modifier = Modifier.background(Color.White).fillMaxSize()
    ) {
        AnimatedVisibility(
            rememberIndicatorVisible(
                !state.isReady,
                minShowTimeMillis = 600L,
                delayMillis = 0L
            ),
            enter = EnterTransition.None,
            exit = fadeOut(),
            modifier = Modifier
                .zIndex(1f)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .zIndex(1f)
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        AndroidView(
            modifier = Modifier.padding(15.dp),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true

                    webView = this
                    webViewClient = EpubWebViewClient(
                        state = state,
                        resources = resources,
                        initialScroll = state.currentPercentage,
                        onChangePage = { url ->
                            val resource = resources.getByHref(url)

                            if(resource != null) {
                                state.switchToPage(
                                    spine.spineReferences.indexOfFirst { reference ->
                                        reference.resource.id == resource.id
                                    },
                                    id = url.split("#").getOrNull(1)
                                )
                            }
                        },
                        onChangeScale = {
                            scale = it
                        }
                    )

                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun onUnconsumedClick() {
                            onTap()
                        }
                    }, "Android")

                    setOnScrollChangeListener { view, _, _, _, _ ->
                        val webView = view as WebView

                        val contentHeight = webView.contentHeight * scale
                        val maxScroll = contentHeight - webView.height

                        val progress = if (maxScroll > 0) {
                            scrollY.toFloat() / maxScroll
                        } else {
                            0f
                        }

                        val percent = progress.coerceIn(0f, 1f)
                        println(percent)
                        state.updatePercentage(percent)
                        onScrollPercentageUpdate(percent)
                    }
                }
            }
        )
    }
}