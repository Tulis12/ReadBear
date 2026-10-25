package dev.tulis.readbear.db.entities.books

import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import java.io.File

class BookRepository @Inject constructor(
    private val dao: BookDao
) {

    val books = dao.getAllFlow()

    suspend fun addBook(book: Book): Long {
        return dao.insert(book)
    }

    suspend fun updateBook(book: Book) {
        return dao.update(book)
    }

    suspend fun updateCover(id: Long, cover: String): Int {
        return dao.updateCover(id, cover)
    }

//    suspend fun getBookWithPages(bookId: Long): BookWithPages {
//        return dao.getBookWithPages(bookId)
//    }

//    fun getBookWithBookmark(bookId: Long): Flow<BookWithBookmark> {
//        return dao.getBookWithBookmark(bookId)
//    }

    suspend fun getBook(bookId: Long): Book {
        return dao.get(bookId)
    }

    fun getBookFlow(bookId: Long): Flow<Book> {
        return dao.getBookFlow(bookId)
    }

    fun synchronizeDeletedPaths() {
//        filesDir TODO()
    }

    fun getBookByItemId(itemId: Long): Flow<Book?> {
        return dao.getByItemId(itemId)
    }

    suspend fun getBookByItemIdSuspend(itemId: Long): Book {
        return dao.getByItemIdSuspend(itemId)
    }
}