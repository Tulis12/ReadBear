package dev.tulis.tulireader.db.books

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    var title: String,
    var path: String,
    var cover: String = "",
    var pages: Int = 0
)