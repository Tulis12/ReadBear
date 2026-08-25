package dev.tulis.readbear.db.comics

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.tulis.readbear.db.comics.pages.ComicPage
import dev.tulis.readbear.db.relations.ComicWithBookmark
import dev.tulis.readbear.db.relations.ComicWithPages
import kotlinx.coroutines.flow.Flow

@Dao
interface ComicDao {
    @Insert
    suspend fun insert(comic: Comic): Long

    @Delete
    suspend fun delete(comic: Comic)

    @Update
    suspend fun update(comic: Comic): Int

    @Query("SELECT * FROM Comic WHERE id = :id")
    suspend fun get(id: Long): Comic

    @Query("DELETE FROM Comic WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Transaction
    @Query("SELECT * FROM Comic WHERE id = :comicId")
    suspend fun getComicWithPages(comicId: Long): ComicWithPages

    @Query("SELECT * FROM Comic WHERE bookId = :bookId")
    suspend fun getComicByBookId(bookId: Long): Comic

    @Transaction
    @Query("SELECT * FROM Comic WHERE id = :comicId")
    fun getComicWithBookmark(comicId: Long): Flow<ComicWithBookmark>
}