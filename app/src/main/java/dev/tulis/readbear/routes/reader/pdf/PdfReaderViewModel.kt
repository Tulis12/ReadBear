package dev.tulis.readbear.routes.reader.pdf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tulis.readbear.db.books.Book
import dev.tulis.readbear.db.books.BookRepository
import dev.tulis.readbear.db.comics.ComicDao
import dev.tulis.readbear.db.comics.bookmarks.ComicBookmark
import dev.tulis.readbear.db.comics.bookmarks.ComicBookmarkDao
import dev.tulis.readbear.db.comics.pages.ComicPage
import dev.tulis.readbear.db.pdfs.PdfDao
import dev.tulis.readbear.db.pdfs.bookmarks.PdfBookmark
import dev.tulis.readbear.db.pdfs.bookmarks.PdfBookmarkDao
import dev.tulis.readbear.db.relations.ComicWithBookmark
import dev.tulis.readbear.db.relations.PdfWithBookmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipFile
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
