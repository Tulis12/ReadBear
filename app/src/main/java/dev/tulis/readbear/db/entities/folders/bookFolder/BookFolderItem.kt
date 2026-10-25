package dev.tulis.readbear.db.entities.folders.bookFolder

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = BookFolder::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("parentId")]
)
data class BookFolderItem(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var parentId: Long? = null,
    var type: BookFolderItemType,
    var positionIndex: Long
)

enum class BookFolderItemType {
    Book,
    Folder
}