package dev.tulis.readbear.db.relations

import androidx.room.Embedded
import androidx.room.Relation
import dev.tulis.readbear.db.entities.comics.Comic
import dev.tulis.readbear.db.entities.comics.bookmarks.ComicBookmark

data class ComicWithBookmark(
    @Embedded
    val comic: Comic,

    @Relation(
        parentColumn = "id",
        entityColumn = "comicId"
    )
    val bookmark: ComicBookmark
)