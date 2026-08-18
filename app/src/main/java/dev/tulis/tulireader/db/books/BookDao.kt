package dev.tulis.tulireader.db.books

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.tulis.tulireader.db.relations.BookWithBookmark
import dev.tulis.tulireader.db.relations.BookWithPages
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query("SELECT * FROM Book")
    fun getAll(): Flow<List<Book>>

    @Insert
    suspend fun insert(book: Book): Long

    @Query("UPDATE Book SET cover = :cover WHERE id = :id")
    suspend fun updateCover(id: Long, cover: String)

    @Update
    suspend fun update(book: Book)

    @Delete
    suspend fun delete(book: Book)

    @Query("SELECT * FROM Book WHERE id = :id")
    suspend fun get(id: Long): Book

    @Query("SELECT * FROM Book WHERE id = :id")
    fun getBookFlow(id: Long): Flow<Book?>

    @Query("DELETE FROM Book WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Transaction
    @Query("SELECT * FROM Book WHERE id = :bookId")
    suspend fun getBookWithPages(bookId: Long): BookWithPages

    @Transaction
    @Query("SELECT * FROM Book WHERE id = :bookId")
    fun getBookWithBookmark(bookId: Long): Flow<BookWithBookmark>
}