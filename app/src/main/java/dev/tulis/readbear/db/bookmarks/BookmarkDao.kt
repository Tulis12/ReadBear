package dev.tulis.readbear.db.bookmarks

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface BookmarkDao {
    @Insert
    suspend fun insert(bookmark: Bookmark)

    @Delete
    suspend fun delete(bookmark: Bookmark)

    @Update
    suspend fun update(bookmark: Bookmark)

    @Query("SELECT * FROM Bookmark WHERE bookId = :bookId")
    suspend fun getByBookId(bookId: Long): Bookmark?

    @Query("DELETE FROM Bookmark WHERE id = :id")
    suspend fun deleteById(id: Long)
}