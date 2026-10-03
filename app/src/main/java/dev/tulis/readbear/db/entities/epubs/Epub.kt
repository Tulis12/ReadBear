package dev.tulis.readbear.db.entities.epubs

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.tulis.readbear.db.entities.books.Book

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
data class Epub(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    var bookId: Long
)
