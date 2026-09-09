package dev.tulis.readbear.db.epubs

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.tulis.readbear.db.relations.EpubWithBookmark
import dev.tulis.readbear.db.relations.PdfWithBookmark
import kotlinx.coroutines.flow.Flow

@Dao
interface EpubDao {
    @Insert
    suspend fun insert(comic: Epub): Long

    @Delete
    suspend fun delete(comic: Epub)

    @Update
    suspend fun update(comic: Epub): Int

    @Query("SELECT * FROM Epub WHERE id = :id")
    suspend fun get(id: Long): Epub

    @Query("SELECT * FROM Epub WHERE id = :id")
    fun getFlow(id: Long): Flow<Epub>

    @Query("DELETE FROM Epub WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM Epub WHERE bookId = :bookId")
    suspend fun getEpubByBookId(bookId: Long): Epub

    @Transaction
    @Query("SELECT * FROM Epub WHERE id = :epubId")
    fun getEpubWithBookmark(epubId: Long): Flow<EpubWithBookmark>
}