package dev.tulis.readbear.routes.reader.epub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tulis.readbear.db.books.Book
import dev.tulis.readbear.db.books.BookRepository
import dev.tulis.readbear.db.epubs.EpubDao
import dev.tulis.readbear.db.epubs.bookmarks.EpubBookmark
import dev.tulis.readbear.db.epubs.bookmarks.EpubBookmarkDao
import dev.tulis.readbear.db.relations.EpubWithBookmark
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EpubReaderViewModel @Inject constructor (
    private val epubDao: EpubDao,
    private val epubBookmarkDao: EpubBookmarkDao,
    private val bookRepository: BookRepository
) : ViewModel() {
    fun updateBookmark(bookmark: EpubBookmark) {
        viewModelScope.launch {
            epubBookmarkDao.update(bookmark)
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

    fun getEpubWithBookmark(epubId: Long): Flow<EpubWithBookmark> {
        return epubDao.getEpubWithBookmark(epubId)
    }
}
