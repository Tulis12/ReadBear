package dev.tulis.readbear.db.quotes

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
data class Quote(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var quote: String,
    var description: String? = null,
    var bookId: Long,
    var progress: Long
)
