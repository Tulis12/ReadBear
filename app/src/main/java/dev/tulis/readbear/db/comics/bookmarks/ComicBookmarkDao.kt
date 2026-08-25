package dev.tulis.readbear.db.comics.bookmarks

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ComicBookmarkDao {
    @Insert
    suspend fun insert(bookmark: ComicBookmark)

    @Delete
    suspend fun delete(bookmark: ComicBookmark)

    @Update
    suspend fun update(bookmark: ComicBookmark)

    @Query("SELECT * FROM ComicBookmark WHERE comicId = :bookId")
    suspend fun getByBookId(bookId: Long): ComicBookmark?

    @Query("DELETE FROM ComicBookmark WHERE id = :id")
    suspend fun deleteById(id: Long)
}