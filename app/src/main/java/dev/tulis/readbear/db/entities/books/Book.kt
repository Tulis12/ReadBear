package dev.tulis.readbear.db.entities.books

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.tulis.readbear.db.entities.folders.bookFolder.BookFolderItem
import kotlin.time.Clock
import kotlin.time.Instant

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = BookFolderItem::class,
            parentColumns = ["id"],
            childColumns = ["folderItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("folderItemId")]
)
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    var title: String,
    var summary: String? = null,
    var author: String? = null,
    var published: String? = null,
    var web: String? = null,
    var path: String,
    var cover: String = "",
    var type: BookType,
    var readingTime: Long = 0,
    var progress: Int = 0,
    var totalProgress: Int = 0,
    var readAlready: Int = 0,
    var folderItemId: Long,
    var lastReadAt: Instant? = null,
    val createdAt: Instant = Clock.System.now()
)

enum class BookType {
    Comic,
    Pdf,
    Epub
}