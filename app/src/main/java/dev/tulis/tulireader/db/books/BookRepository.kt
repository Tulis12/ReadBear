package dev.tulis.tulireader.db.books

import androidx.room.Query
import androidx.room.Transaction
import dev.tulis.tulireader.db.relations.BookWithBookmark
import dev.tulis.tulireader.db.relations.BookWithPages
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import java.io.File

class BookRepository @Inject constructor(
    private val dao: BookDao,
    private val filesDir: File
) {

    val books = dao.getAll()

    suspend fun addBook(book: Book): Long {
        return dao.insert(book)
    }

    suspend fun updateBook(book: Book) {
        return dao.update(book)
    }

    suspend fun updateCover(id: Long, cover: String) {
        dao.updateCover(id, cover)
    }

    suspend fun getBookWithPages(bookId: Long): BookWithPages {
        return dao.getBookWithPages(bookId)
    }

    fun getBookWithBookmark(bookId: Long): Flow<BookWithBookmark> {
        return dao.getBookWithBookmark(bookId)
    }

    suspend fun getBook(bookId: Long): Book {
        return dao.get(bookId)
    }

    fun getBookFlow(bookId: Long): Flow<Book?> {
        return dao.getBookFlow(bookId)
    }

    suspend fun deleteBook(book: Book) {
        dao.delete(book)

        val target = File(filesDir, book.path)
        target.deleteRecursively()
    }
}