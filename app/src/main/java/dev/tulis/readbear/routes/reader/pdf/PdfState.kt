package dev.tulis.readbear.routes.reader.pdf

import androidx.compose.runtime.Stable
import dev.tulis.readbear.db.books.Book
import dev.tulis.readbear.db.relations.PdfWithBookmark
import dev.tulis.readbear.settings.PdfReadingLayout

@Stable
data class PdfState(
    val book: Book,
    val pdfWithBookmark: PdfWithBookmark,
    val readingLayout: PdfReadingLayout,
    val count: Int,
    val topBarVisible: Boolean,
    val splitPages: Boolean,
    val finished: Boolean,
    val onChangeFinished: (Boolean) -> Unit,
    val onChangeTopBarVisible: (Boolean) -> Unit
)