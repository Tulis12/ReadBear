package dev.tulis.readbear.routes.reader.epub

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.createBitmap
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Resources
import nl.siegmann.epublib.epub.EpubReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Composable
fun createEpubCover(xhtml: String, resources: Resources, file: File, onFinish: () -> Unit) {

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = object : EpubWebViewClient(resources) {
                    override fun onPageFinished(view: WebView, url: String?) {
                        view.postDelayed({
                            val bitmap = createBitmap(view.width, view.height)
                            Canvas(bitmap).apply { view.draw(this) }

                            file.outputStream().use {
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, it)
                            }

                            onFinish()
                            bitmap.recycle()
                            view.destroy()
                        }, 500)

                        super.onPageFinished(view, url)
                    }
                }

                isVerticalScrollBarEnabled = false;
                isHorizontalScrollBarEnabled = false;

                loadDataWithBaseURL(
                    "https://epub.local/",
                    xhtml,
                    "application/xhtml+xml",
                    "UTF-8",
                    null
                )
            }
        }
    )
}