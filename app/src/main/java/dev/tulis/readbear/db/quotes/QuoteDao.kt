package dev.tulis.readbear.db.quotes

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.tulis.readbear.db.relations.PdfWithBookmark
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Insert
    suspend fun insert(comic: Quote): Long

    @Delete
    suspend fun delete(comic: Quote)

    @Update
    suspend fun update(comic: Quote): Int

    @Query("SELECT * FROM Quote WHERE id = :id")
    suspend fun get(id: Long): Quote

    @Query("SELECT * FROM Quote WHERE id = :id")
    fun getFlow(id: Long): Flow<Quote>

    @Query("DELETE FROM Quote WHERE id = :id")
    suspend fun deleteById(id: Long)

//    @Query("SELECT * FROM Pdf WHERE bookId = :bookId")
//    suspend fun getPdfByBookId(bookId: Long): Quote
//
//    @Transaction
//    @Query("SELECT * FROM Pdf WHERE id = :pdfId")
//    fun getPdfWithBookmark(pdfId: Long): Flow<PdfWithBookmark>
}