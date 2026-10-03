package dev.tulis.readbear.db.relations

import androidx.room.Embedded
import androidx.room.Relation
import dev.tulis.readbear.db.entities.books.Book
import dev.tulis.readbear.db.entities.snippets.Snippet

data class SnippetWithBook(
    @Embedded
    val snippet: Snippet,

    @Relation(
        parentColumn = "bookId",
        entityColumn = "id"
    )
    val book: Book
)