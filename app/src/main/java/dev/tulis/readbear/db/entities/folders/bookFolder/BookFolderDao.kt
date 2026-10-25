package dev.tulis.readbear.db.entities.folders.bookFolder

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookFolderDao {
    @Insert
    suspend fun insert(bookFolder: BookFolder): Long

    @Delete
    suspend fun delete(bookFolder: BookFolder)

    @Update
    suspend fun update(bookFolder: BookFolder): Int

    @Query("SELECT * FROM BookFolder WHERE id = :id")
    fun get(id: Long): Flow<BookFolder?>

    @Query("SELECT * FROM BookFolder WHERE id = :id")
    fun getFlow(id: Long): Flow<BookFolder?>

    @Query("DELETE FROM BookFolder WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM BookFolder WHERE itemId IS :itemId")
    fun getFolderByItemId(itemId: Long): Flow<BookFolder?>

    @Query("SELECT * FROM BookFolder WHERE itemId IS :itemId")
    suspend fun getFolderByItemIdSuspend(itemId: Long): BookFolder?

//    @Transaction
//    @Query("SELECT * FROM bookFolder")
//    fun getAllFlow(): Flow<List<bookFolderWithBook>>
}