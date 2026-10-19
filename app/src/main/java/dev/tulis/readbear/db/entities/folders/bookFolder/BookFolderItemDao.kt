package dev.tulis.readbear.db.entities.folders.bookFolder

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookFolderItemDao {
    @Insert
    suspend fun insert(bookFolderItem: BookFolderItem): Long

    @Delete
    suspend fun delete(bookFolderItem: BookFolderItem)

    @Update
    suspend fun update(bookFolderItem: BookFolderItem): Int

    @Query("SELECT * FROM BookFolderItem")
    fun getAll(): Flow<List<BookFolderItem>>

    @Query("SELECT * FROM BookFolderItem WHERE id = :id")
    suspend fun get(id: Long): BookFolderItem

    @Query("SELECT * FROM BookFolderItem WHERE id = :id")
    fun getFlow(id: Long): Flow<BookFolderItem?>

    @Query("SELECT MAX(positionIndex) FROM BookFolderItem")
    suspend fun getMaxPositionIndex(): Long?

    @Query("DELETE FROM BookFolderItem WHERE id = :id")
    suspend fun deleteById(id: Long)
}