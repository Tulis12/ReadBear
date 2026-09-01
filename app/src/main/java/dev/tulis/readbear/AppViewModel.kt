package dev.tulis.readbear

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tulis.readbear.db.books.Book
import dev.tulis.readbear.db.books.BookRepository
import dev.tulis.readbear.db.comics.Comic
import dev.tulis.readbear.db.comics.ComicDao
import dev.tulis.readbear.db.comics.bookmarks.ComicBookmarkDao
import dev.tulis.readbear.db.comics.pages.ComicPageDao
import dev.tulis.readbear.db.pdfs.Pdf
import dev.tulis.readbear.db.pdfs.PdfDao
import javax.inject.Inject


@HiltViewModel
class AppViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val comicDao: ComicDao,
    private val comicPageDao: ComicPageDao,
    private val comicBookmarkDao: ComicBookmarkDao,
    private val pdfDao: PdfDao
): ViewModel() {
    suspend fun getBook(bookId: Long): Book {
        return bookRepository.getBook(bookId)
    }

    suspend fun getComicByBookId(bookId: Long): Comic {
        return comicDao.getComicByBookId(bookId)
    }

    suspend fun getPdfByBookId(bookId: Long): Pdf {
        return pdfDao.getPdfByBookId(bookId)
    }
}