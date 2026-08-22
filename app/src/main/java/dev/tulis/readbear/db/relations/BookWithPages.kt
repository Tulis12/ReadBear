package dev.tulis.readbear.db.relations

import androidx.room.Embedded
import androidx.room.Relation
import dev.tulis.readbear.db.books.Book
import dev.tulis.readbear.db.pages.Page

data class BookWithPages(
    @Embedded
    val book: Book,

    @Relation(
        parentColumn = "id",
        entityColumn = "bookId"
    )
    val pages: List<Page>
)