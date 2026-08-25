package dev.tulis.readbear.db.comics.bookmarks

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.tulis.readbear.db.books.Book
import dev.tulis.readbear.db.comics.Comic

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Comic::class,
            parentColumns = ["id"],
            childColumns = ["comicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("comicId")]
)
data class ComicBookmark(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val comicId: Long,
    var panelNumber: Int = 0,
    var panelOffset: Int = 0,
)