package dev.tulis.readbear.db.relations

import androidx.room.Embedded
import androidx.room.Relation
import dev.tulis.readbear.db.bookmarks.Bookmark
import dev.tulis.readbear.db.books.Book

data class BookWithBookmark(
    @Embedded
    val book: Book,

    @Relation(
        parentColumn = "id",
        entityColumn = "bookId"
    )
    val bookmark: Bookmark
)