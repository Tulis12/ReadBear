package dev.tulis.readbear.db.pdfs.bookmarks

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PdfBookmarkDao {
    @Insert
    suspend fun insert(bookmark: PdfBookmark)

    @Delete
    suspend fun delete(bookmark: PdfBookmark)

    @Update
    suspend fun update(bookmark: PdfBookmark)

    @Query("SELECT * FROM PdfBookmark WHERE pdfId = :pdfId")
    suspend fun getByBookId(pdfId: Long): PdfBookmark?

    @Query("DELETE FROM PdfBookmark WHERE id = :id")
    suspend fun deleteById(id: Long)
}