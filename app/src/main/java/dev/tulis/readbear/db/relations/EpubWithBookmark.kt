package dev.tulis.readbear.db.relations

import androidx.room.Embedded
import androidx.room.Relation
import dev.tulis.readbear.db.entities.epubs.Epub
import dev.tulis.readbear.db.entities.epubs.bookmarks.EpubBookmark

data class EpubWithBookmark(
    @Embedded
    val epub: Epub,

    @Relation(
        parentColumn = "id",
        entityColumn = "epubId"
    )
    val bookmark: EpubBookmark
)