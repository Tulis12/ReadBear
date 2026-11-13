package dev.tulis.readbear.routes.reader.epub

import android.graphics.Bitmap
import android.graphics.Canvas
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.createBitmap
import nl.siegmann.epublib.domain.Resources
import java.io.File

@Composable
fun CreateEpubCover(xhtml: String, resources: Resources, file: File, onFinish: () -> Unit) {

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = object : EpubWebViewClient(resources = resources) {
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

                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false

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