package dev.tulis.readbear.db.epubs.bookmarks

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface EpubBookmarkDao {
    @Insert
    suspend fun insert(bookmark: EpubBookmark)

    @Delete
    suspend fun delete(bookmark: EpubBookmark)

    @Update
    suspend fun update(bookmark: EpubBookmark)

    @Query("SELECT * FROM EpubBookmark WHERE epubId = :epubId")
    suspend fun getByBookId(epubId: Long): EpubBookmark?

    @Query("DELETE FROM EpubBookmark WHERE id = :id")
    suspend fun deleteById(id: Long)
}