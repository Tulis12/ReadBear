package dev.tulis.readbear.db.entities.folders.bookFolder

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = BookFolderItem::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("itemId")]
)
data class BookFolder(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var name: String,
    var description: String? = null,
    var cover: String? = null,
    var itemId: Long
)
