package dev.tulis.readbear.db.comics.pages

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
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
data class ComicPage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val comicId: Long,
    val panelNumber: Int,
    val path: String,
    val height: Int,
    val width: Int
)