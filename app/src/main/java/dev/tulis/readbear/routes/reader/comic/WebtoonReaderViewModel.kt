package dev.tulis.readbear.routes.reader.comic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tulis.readbear.db.books.Book
import dev.tulis.readbear.db.books.BookRepository
import dev.tulis.readbear.db.comics.ComicDao
import dev.tulis.readbear.db.comics.bookmarks.ComicBookmark
import dev.tulis.readbear.db.comics.bookmarks.ComicBookmarkDao
import dev.tulis.readbear.db.comics.pages.ComicPage
import dev.tulis.readbear.db.relations.ComicWithBookmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject

@HiltViewModel
class WebtoonReaderViewModel @Inject constructor (
    private val comicDao: ComicDao,
    private val comicBookmarkDao: ComicBookmarkDao,
    private val bookRepository: BookRepository,
    private val filesDir: File
) : ViewModel() {
    var zipFile: ZipFile? = null

    override fun onCleared() {
        super.onCleared()
        zipFile?.close()
    }

    fun updateBookmark(bookmark: ComicBookmark) {
        viewModelScope.launch {
            comicBookmarkDao.update(bookmark)
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

    fun getComicWithBookmark(comicId: Long): Flow<ComicWithBookmark> {
        return comicDao.getComicWithBookmark(comicId)
    }

    suspend fun getPanels(comicId: Long): List<ComicPage> {
        val comicWithPages = comicDao.getComicWithPages(comicId)
        val book = bookRepository.getBook(comicWithPages.comic.bookId)

        // This is important, don't remove it future me!
        zipFile = withContext(Dispatchers.IO) {
            ZipFile(
                filesDir
                    .resolve(book.path)
                    .resolve("book.cbz")
            )
        }

        return comicWithPages.panels
    }
}
