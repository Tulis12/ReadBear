package dev.tulis.readbear.db.relations

import androidx.room.Embedded
import androidx.room.Relation
import dev.tulis.readbear.db.epubs.Epub
import dev.tulis.readbear.db.epubs.bookmarks.EpubBookmark
import dev.tulis.readbear.db.pdfs.Pdf
import dev.tulis.readbear.db.pdfs.bookmarks.PdfBookmark

data class EpubWithBookmark(
    @Embedded
    val epub: Epub,

    @Relation(
        parentColumn = "id",
        entityColumn = "epubId"
    )
    val bookmark: EpubBookmark
)