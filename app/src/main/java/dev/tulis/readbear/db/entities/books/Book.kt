package dev.tulis.readbear.db.entities.books

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.time.Instant

@Entity
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
    var lastReadAt: Instant? = null,
    val createdAt: Instant = Clock.System.now()
)

enum class BookType {
    Comic,
    Pdf,
    Epub
}