package dev.tulis.readbear.db.quotes.snippets

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SnippetDao {
    @Insert
    suspend fun insert(snippet: Snippet): Long

    @Delete
    suspend fun delete(snippet: Snippet)

    @Update
    suspend fun update(snippet: Snippet): Int

    @Query("SELECT * FROM Snippet WHERE id = :id")
    suspend fun get(id: Long): Snippet

    @Query("SELECT * FROM Snippet WHERE id = :id")
    fun getFlow(id: Long): Flow<Snippet>

    @Query("DELETE FROM Snippet WHERE id = :id")
    suspend fun deleteById(id: Long)

//    @Query("SELECT * FROM Pdf WHERE bookId = :bookId")
//    suspend fun getPdfByBookId(bookId: Long): Quote
//
//    @Transaction
//    @Query("SELECT * FROM Pdf WHERE id = :pdfId")
//    fun getPdfWithBookmark(pdfId: Long): Flow<PdfWithBookmark>
}