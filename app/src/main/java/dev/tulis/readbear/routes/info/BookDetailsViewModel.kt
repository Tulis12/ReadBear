package dev.tulis.readbear.routes.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tulis.readbear.db.books.Book
import dev.tulis.readbear.db.books.BookDao
import dev.tulis.readbear.db.books.BookRepository
import dev.tulis.readbear.db.comics.Comic
import dev.tulis.readbear.db.comics.ComicDao
import dev.tulis.readbear.db.comics.bookmarks.ComicBookmark
import dev.tulis.readbear.db.comics.bookmarks.ComicBookmarkDao
import dev.tulis.readbear.db.comics.pages.ComicPage
import dev.tulis.readbear.db.pdfs.Pdf
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
class BookDetailsViewModel @Inject constructor (
    private val bookDao: BookDao,
    private val pdfDao: PdfDao,
    private val comicDao: ComicDao
) : ViewModel() {
    fun getBookById(bookId: Long): Flow<Book?> {
        return bookDao.getBookFlow(bookId)
    }

    suspend fun getPdfByBookId(bookId: Long): Pdf {
        return pdfDao.getPdfByBookId(bookId)
    }

    suspend fun getComicByBookId(bookId: Long): Comic {
        return comicDao.getComicByBookId(bookId)
    }
}
