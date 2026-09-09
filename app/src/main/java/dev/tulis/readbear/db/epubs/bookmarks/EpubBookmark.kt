package dev.tulis.readbear.db.epubs.bookmarks

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.tulis.readbear.db.epubs.Epub

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Epub::class,
            parentColumns = ["id"],
            childColumns = ["epubId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("epubId")]
)
data class EpubBookmark(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val epubId: Long,
    var page: Int = 0,
    var pagePercentage: Float = 0f
)