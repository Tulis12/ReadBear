package dev.tulis.readbear.routes.info

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tulis.readbear.db.books.Book
import dev.tulis.readbear.db.books.BookDao
import dev.tulis.readbear.db.comics.Comic
import dev.tulis.readbear.db.comics.ComicDao
import dev.tulis.readbear.db.pdfs.Pdf
import dev.tulis.readbear.db.pdfs.PdfDao
import kotlinx.coroutines.flow.Flow
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
