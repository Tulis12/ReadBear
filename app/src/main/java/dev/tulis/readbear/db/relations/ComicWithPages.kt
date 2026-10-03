package dev.tulis.readbear.db.relations

import androidx.room.Embedded
import androidx.room.Relation
import dev.tulis.readbear.db.entities.comics.Comic
import dev.tulis.readbear.db.entities.comics.pages.ComicPage

data class ComicWithPages(
    @Embedded
    val comic: Comic,

    @Relation(
        parentColumn = "id",
        entityColumn = "comicId"
    )
    val panels: List<ComicPage>
)