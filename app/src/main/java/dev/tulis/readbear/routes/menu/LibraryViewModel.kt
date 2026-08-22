package dev.tulis.readbear.routes.menu

import android.content.Context
import android.graphics.BitmapFactory
import android.icu.text.Collator
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tulis.readbear.db.bookmarks.Bookmark
import dev.tulis.readbear.db.bookmarks.BookmarkRepository
import dev.tulis.readbear.db.books.Book
import dev.tulis.readbear.db.books.BookRepository
import dev.tulis.readbear.db.pages.Page
import dev.tulis.readbear.db.pages.PageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipFile
import javax.inject.Inject
import kotlin.sequences.forEach

@HiltViewModel
class LibraryViewModel @Inject constructor (
    private val bookRepository: BookRepository,
    private val pageRepository: PageRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val filesDir: File
) : ViewModel() {

    val imageExtensions = arrayOf(
        "jpg", "jpeg", "png",
        "webp", "avif", "heic",
        "heif", "jxl", "gif",
        "bmp", "tiff", "tif"
    )

    val books = bookRepository.books
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            emptyList()
        )


    suspend fun getBook(bookId: Long): Book {
        return bookRepository.getBook(bookId)
    }

    suspend fun getBookmark(bookId: Long): Bookmark? {
        return bookmarkRepository.get(bookId)
    }

    fun getBookFlow(bookId: Long): Flow<Book?> {
        return bookRepository.getBookFlow(bookId)
    }

    suspend fun addBook(book: Book): Long {
        return bookRepository.addBook(book)
    }

    fun updateBook(book: Book) {
        viewModelScope.launch {
            bookRepository.updateBook(book)
        }
    }

    suspend fun updateBookCover(id: Long, cover: String): Int {
        return bookRepository.updateCover(id, cover)
    }

    fun removeBook(book: Book) {
        viewModelScope.launch {
            bookRepository.deleteBook(book)
        }
    }

    suspend fun createBookmark(bookId: Long) {
        val bookmark = Bookmark(bookId = bookId, pageNumber = 0, pageOffset = 0, readAlready = 0)
        bookmarkRepository.add(bookmark)
    }

    suspend fun createIndex(
        book: Book
    ) = withContext(Dispatchers.IO) {
        val files = mutableListOf<String>()
        val bookDir = filesDir.resolve(book.path)

        ZipFile(
            bookDir.resolve("book.cbz")
        ).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                if (!entry.isDirectory) {
                    files.add(entry.name)
                }
            }

            val collator = Collator.getInstance()

            files.sortWith { a, b ->
                collator.compare(a, b)
            }

            var i = 0L

            for(file in files) {
                val extension = file.substringAfterLast('.', "")
                val isImage = imageExtensions.any {
                    extension.equals(it, ignoreCase = true)
                }

                if(!isImage) continue
                i++

                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }

                zip.getInputStream(zip.getEntry(file)).use { input ->
                    BitmapFactory.decodeStream(input, null, options)
                }

                val width = options.outWidth
                val height = options.outHeight

                pageRepository.addPage(
                    Page(
                        bookId = book.id,
                        pageNumber = i,
                        path = file,
                        width = width,
                        height = height
                    )
                )
            }

            val coverFileName = files.first()
            val coverExtension = coverFileName.substringAfterLast('.', "")
            val coverFile = bookDir.resolve("cover.${coverExtension}")

            val isImage = imageExtensions.any {
                coverExtension.equals(it, ignoreCase = true)
            }

            if(!isImage) return@use

            val entry = zip.getEntry(coverFileName) ?: return@use

            zip.getInputStream(entry).use { input ->
                coverFile.outputStream().use { outputStream ->
                    input.copyTo(outputStream)
                }
            }

            createBookmark(book.id)

            book.cover = "cover.${coverExtension}"
            book.pages = i.toInt()
            updateBook(book)
        }
    }

    fun getFileName(context: Context, uri: Uri): String {
        var name = "unknown"

        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                name = cursor.getString(0)
            }
        }

        return name
    }

    fun copyFileToAppStorage(
        context: Context,
        uri: Uri,
        bookDir: File,
        onFinish: (String) -> Unit,
        onThrow: (Throwable?) -> Unit
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val target = File(bookDir, "book.cbz")

                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(target).use { output ->
                        try {
                            input.copyTo(output)
                        } catch (e: IOException) {
                            target.delete()
                            bookDir.deleteRecursively()

                            onThrow(e.cause)
                            return@withContext
                        }
                    }
                }

                onFinish(
                    getFileName(context, uri)
                )
            }
        }
    }
}