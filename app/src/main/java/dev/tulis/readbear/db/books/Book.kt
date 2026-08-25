package dev.tulis.readbear.db.books

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    var readAlready: Int = 0
)

enum class BookType {
    Comic,
    Pdf,
    Epub
}