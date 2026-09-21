package dev.tulis.readbear.db.quotes.snippets

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.tulis.readbear.db.books.Book

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookId")]
)
data class Snippet(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var name: String,
    var description: String? = null,
    var path: String,
    var bookId: Long,
    var progress: Long
)
