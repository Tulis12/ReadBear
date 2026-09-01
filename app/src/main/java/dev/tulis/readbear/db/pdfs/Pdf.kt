package dev.tulis.readbear.db.pdfs

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
data class Pdf(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var keywords: String? = null,

    var bookId: Long
)
