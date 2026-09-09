package dev.tulis.readbear.routes.reader.epub

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import nl.siegmann.epublib.domain.Resources
import java.io.ByteArrayInputStream

open class EpubWebViewClient(
    private val resources: Resources,
    private val initialScroll: Float = 0f,
    private val onChangePage: (String) -> Unit = {},
    private val onChangeScale: (Float) -> Unit = {}
) : WebViewClient() {
    var initialized = false
    var scale = 1f

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        if(url.contains("https://epub.local/")) onChangePage(url.replace("https://epub.local/", ""))
    }

//    override fun onPageFinished(view: WebView, url: String?) { // TODO()
//        view.evaluateJavascript(
//            """
//            (() => {
//                let style = document.getElementById('dark-theme');
//
//                if (!style) {
//                    style = document.createElement('style');
//                    style.id = 'dark-theme';
//                    document.head.appendChild(style);
//                }
//
//                style.textContent = `
//                    html, body {
//                        background-color: #121212 !important;
//                        color: #e0e0e0 !important;
//                    }
//
//                    body * {
//                        color: #e0e0e0 !important;
//                    }
//
//                    a {
//                        color: #8ab4f8 !important;
//                    }
//                `;
//            })();
//            """.trimIndent(),
//            null
//        )
//    }

    override fun onScaleChanged(view: WebView?, oldScale: Float, newScale: Float) {
        onChangeScale(newScale)
        scale = newScale

        super.onScaleChanged(view, oldScale, newScale)
    }

    override fun onPageFinished(view: WebView, url: String?) {
        if(!initialized) {
            view.scrollTo(
                0, (initialScroll * ((view.contentHeight * scale) - view.height)).toInt()
            )

            initialized = true
        }

        view.evaluateJavascript(
            """
            document.addEventListener("click", function(event) {
                const target = event.target;
        
                if (target.closest("a, button, input, textarea, select, [onclick]")) {
                    return;
                }
        
                Android.onUnconsumedClick();
            });
            """.trimIndent(),
            null
        )
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {


        var url = request?.url?.toString()

        if(url?.contains("https://epub.local/") == true) {
            url = url.replace("https://epub.local/", "")

            if(resources.getByHref(url) == null) {
                return WebResourceResponse(
                    "text/plain",
                    "UTF-8",
                    404,
                    "Not Found",
                    emptyMap(),
                    null
                )
            }

            return WebResourceResponse(
                resources.getByHref(url).mediaType.name,
                resources.getByHref(url).inputEncoding,
                ByteArrayInputStream(resources.getByHref(url).inputStream.readBytes())
            )
        }

        return super.shouldInterceptRequest(view, request)
    }
}