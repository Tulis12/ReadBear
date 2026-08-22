package dev.tulis.readbear.routes.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tulis.readbear.db.bookmarks.Bookmark
import dev.tulis.readbear.db.bookmarks.BookmarkRepository
import dev.tulis.readbear.db.books.BookRepository
import dev.tulis.readbear.db.pages.Page
import dev.tulis.readbear.db.relations.BookWithBookmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject

@HiltViewModel
class WebtoonReaderViewModel @Inject constructor (
    private val bookRepository: BookRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val filesDir: File
) : ViewModel() {

    var zipFile: ZipFile? = null

    override fun onCleared() {
        super.onCleared()
        zipFile?.close()
    }

    fun updateBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            bookmarkRepository.update(bookmark)
        }
    }

    fun getBookWithBookmark(bookId: Long): Flow<BookWithBookmark> {
        return bookRepository.getBookWithBookmark(bookId)
    }

    suspend fun getPages(bookId: Long): List<Page> {
        val bookWithPages = bookRepository.getBookWithPages(bookId)

        zipFile = withContext(Dispatchers.IO) {
            ZipFile(
                filesDir
                    .resolve(bookWithPages.book.path)
                    .resolve("book.cbz")
            )
        }

        return bookWithPages.pages
    }
}
