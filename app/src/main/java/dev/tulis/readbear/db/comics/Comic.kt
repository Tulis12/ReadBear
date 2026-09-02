package dev.tulis.readbear.db.comics

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
data class Comic(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    var bookId: Long,
    var panels: Int = 0,
    var series: String? = null,
    var seriesStatus: String? = null,
    var manga: Boolean? = null
)
