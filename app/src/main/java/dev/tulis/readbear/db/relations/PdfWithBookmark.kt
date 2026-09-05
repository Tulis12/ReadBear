package dev.tulis.readbear.db.relations

import androidx.room.Embedded
import androidx.room.Relation
import dev.tulis.readbear.db.pdfs.Pdf
import dev.tulis.readbear.db.pdfs.bookmarks.PdfBookmark

data class PdfWithBookmark(
    @Embedded
    val pdf: Pdf,

    @Relation(
        parentColumn = "id",
        entityColumn = "pdfId"
    )
    val bookmark: PdfBookmark
)