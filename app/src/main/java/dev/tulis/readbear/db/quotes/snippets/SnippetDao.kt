package dev.tulis.readbear.db.quotes.snippets

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.tulis.readbear.db.relations.SnippetWithBook
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

    @Transaction
    @Query("SELECT * FROM Snippet")
    fun getAllFlow(): Flow<List<SnippetWithBook>>
}