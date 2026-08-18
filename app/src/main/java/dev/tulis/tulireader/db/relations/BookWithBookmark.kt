package dev.tulis.tulireader.db.relations

import androidx.room.Embedded
import androidx.room.Relation
import dev.tulis.tulireader.db.bookmarks.Bookmark
import dev.tulis.tulireader.db.books.Book

data class BookWithBookmark(
    @Embedded
    val book: Book,

    @Relation(
        parentColumn = "id",
        entityColumn = "bookId"
    )
    val bookmark: Bookmark
)