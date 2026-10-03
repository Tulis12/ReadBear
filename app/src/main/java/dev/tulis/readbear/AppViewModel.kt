package dev.tulis.readbear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tulis.readbear.db.entities.books.Book
import dev.tulis.readbear.db.entities.books.BookRepository
import dev.tulis.readbear.db.entities.comics.Comic
import dev.tulis.readbear.db.entities.comics.ComicDao
import dev.tulis.readbear.db.entities.epubs.Epub
import dev.tulis.readbear.db.entities.epubs.EpubDao
import dev.tulis.readbear.db.entities.pdfs.Pdf
import dev.tulis.readbear.db.entities.pdfs.PdfDao
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AppViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val comicDao: ComicDao,
    private val pdfDao: PdfDao,
    private val epubDao: EpubDao
): ViewModel() {
    suspend fun getBook(bookId: Long): Book {
        return bookRepository.getBook(bookId)
    }

    fun updateBookCover(id: Long, cover: String) {
        viewModelScope.launch {
            bookRepository.updateCover(id, cover)
        }
    }

    suspend fun getComicByBookId(bookId: Long): Comic {
        return comicDao.getComicByBookId(bookId)
    }

    suspend fun getPdfByBookId(bookId: Long): Pdf {
        return pdfDao.getPdfByBookId(bookId)
    }

    suspend fun getEpubByBookId(bookId: Long): Epub {
        return epubDao.getEpubByBookId(bookId)
    }
}