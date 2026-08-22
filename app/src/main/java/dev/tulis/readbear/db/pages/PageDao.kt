package dev.tulis.readbear.db.pages

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PageDao {
    @Insert
    suspend fun insert(page: Page)

    @Delete
    suspend fun delete(page: Page)

    @Query("SELECT * FROM Page WHERE id = :id")
    suspend fun get(id: Long): Page

    @Query("DELETE FROM Page WHERE id = :id")
    suspend fun deleteById(id: Long)
}