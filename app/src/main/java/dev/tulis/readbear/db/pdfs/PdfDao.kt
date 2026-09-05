package dev.tulis.readbear.db.pdfs

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.tulis.readbear.db.relations.PdfWithBookmark
import kotlinx.coroutines.flow.Flow

@Dao
interface PdfDao {
    @Insert
    suspend fun insert(comic: Pdf): Long

    @Delete
    suspend fun delete(comic: Pdf)

    @Update
    suspend fun update(comic: Pdf): Int

    @Query("SELECT * FROM Pdf WHERE id = :id")
    suspend fun get(id: Long): Pdf

    @Query("DELETE FROM Pdf WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM Pdf WHERE bookId = :bookId")
    suspend fun getPdfByBookId(bookId: Long): Pdf

    @Transaction
    @Query("SELECT * FROM Pdf WHERE id = :pdfId")
    fun getPdfWithBookmark(pdfId: Long): Flow<PdfWithBookmark>
}