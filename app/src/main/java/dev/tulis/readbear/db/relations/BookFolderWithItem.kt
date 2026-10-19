package dev.tulis.readbear.db.relations

import androidx.room.Embedded
import androidx.room.Relation
import dev.tulis.readbear.db.entities.comics.pages.ComicPage
import dev.tulis.readbear.db.entities.folders.bookFolder.BookFolder
import dev.tulis.readbear.db.entities.folders.bookFolder.BookFolderItem

data class BookFolderWithItem(
    @Embedded
    val folder: BookFolder,

    @Relation(
        parentColumn = "id",
        entityColumn = "parentId"
    )
    val items: List<BookFolderItem>
)