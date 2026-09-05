package dev.tulis.readbear.routes.reader.pdf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tulis.readbear.db.books.Book
import dev.tulis.readbear.db.books.BookRepository
import dev.tulis.readbear.db.pdfs.PdfDao
import dev.tulis.readbear.db.pdfs.bookmarks.PdfBookmark
import dev.tulis.readbear.db.pdfs.bookmarks.PdfBookmarkDao
import dev.tulis.readbear.db.relations.PdfWithBookmark
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PdfReaderViewModel @Inject constructor (
    private val pdfDao: PdfDao,
    private val pdfBookmarkDao: PdfBookmarkDao,
    private val bookRepository: BookRepository,
    private val filesDir: File
) : ViewModel() {
    fun updateBookmark(bookmark: PdfBookmark) {
        viewModelScope.launch {
            pdfBookmarkDao.update(bookmark)
        }
    }

    fun updateBookProgress(book: Book) {
        viewModelScope.launch {
            bookRepository.updateBook(book)
        }
    }

    suspend fun getBook(bookId: Long): Book {
        return bookRepository.getBook(bookId)
    }

    fun getPdfWithBookmark(pdfId: Long): Flow<PdfWithBookmark> {
        return pdfDao.getPdfWithBookmark(pdfId)
    }
}
