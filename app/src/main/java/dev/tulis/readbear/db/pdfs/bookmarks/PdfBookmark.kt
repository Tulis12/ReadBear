package dev.tulis.readbear.db.pdfs.bookmarks

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.tulis.readbear.db.comics.Comic
import dev.tulis.readbear.db.pdfs.Pdf

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Pdf::class,
            parentColumns = ["id"],
            childColumns = ["pdfId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("pdfId")]
)
data class PdfBookmark(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pdfId: Long,
    var page: Int = 0
)