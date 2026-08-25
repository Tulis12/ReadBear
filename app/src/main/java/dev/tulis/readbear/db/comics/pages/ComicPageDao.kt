package dev.tulis.readbear.db.comics.pages

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ComicPageDao {
    @Insert
    suspend fun insert(page: ComicPage)

    @Delete
    suspend fun delete(page: ComicPage)

    @Query("SELECT * FROM ComicPage WHERE id = :id")
    suspend fun get(id: Long): ComicPage

    @Query("DELETE FROM ComicPage WHERE id = :id")
    suspend fun deleteById(id: Long)
}