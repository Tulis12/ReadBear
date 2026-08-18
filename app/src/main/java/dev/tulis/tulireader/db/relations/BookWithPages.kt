package dev.tulis.tulireader.db.relations

import androidx.room.Embedded
import androidx.room.Relation
import dev.tulis.tulireader.db.books.Book
import dev.tulis.tulireader.db.pages.Page

data class BookWithPages(
    @Embedded
    val book: Book,

    @Relation(
        parentColumn = "id",
        entityColumn = "bookId"
    )
    val pages: List<Page>
)