package dev.tulis.readbear.db.relations

import androidx.room.Embedded
import androidx.room.Relation
import dev.tulis.readbear.db.books.Book
import dev.tulis.readbear.db.comics.Comic
import dev.tulis.readbear.db.comics.bookmarks.ComicBookmark
import dev.tulis.readbear.db.quotes.snippets.Snippet

data class SnippetWithBook(
    @Embedded
    val snippet: Snippet,

    @Relation(
        parentColumn = "bookId",
        entityColumn = "id"
    )
    val book: Book
)