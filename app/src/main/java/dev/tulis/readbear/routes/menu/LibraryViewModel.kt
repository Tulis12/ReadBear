package dev.tulis.readbear.routes.menu

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.icu.text.Collator
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tulis.readbear.db.books.Book
import dev.tulis.readbear.db.books.BookRepository
import dev.tulis.readbear.db.comics.Comic
import dev.tulis.readbear.db.comics.ComicDao
import dev.tulis.readbear.db.comics.bookmarks.ComicBookmark
import dev.tulis.readbear.db.comics.bookmarks.ComicBookmarkDao
import dev.tulis.readbear.db.comics.pages.ComicPage
import dev.tulis.readbear.db.comics.pages.ComicPageDao
import dev.tulis.readbear.db.pdfs.Pdf
import dev.tulis.readbear.db.pdfs.PdfDao
import dev.tulis.readbear.db.pdfs.bookmarks.PdfBookmark
import dev.tulis.readbear.db.pdfs.bookmarks.PdfBookmarkDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale
import java.util.Locale.getDefault
import java.util.UUID
import java.util.zip.ZipFile
import javax.inject.Inject
import kotlin.sequences.forEach
import androidx.core.graphics.createBitmap

@HiltViewModel
class LibraryViewModel @Inject constructor (
    private val bookRepository: BookRepository,
    private val comicDao: ComicDao,
    private val comicPageDao: ComicPageDao,
    private val comicBookmarkDao: ComicBookmarkDao,
    private val pdfDao: PdfDao,
    private val pdfBookmarkDao: PdfBookmarkDao,
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

    suspend fun createComicBookmark(comicId: Long) {
        val bookmark = ComicBookmark(comicId = comicId)
        comicBookmarkDao.insert(bookmark)
    }

    suspend fun createPdfBookmark(pdfId: Long) {
        val bookmark = PdfBookmark(pdfId = pdfId)
        pdfBookmarkDao.insert(bookmark)
    }


    suspend fun createPdfIndex(
        book: Book
    ) = withContext(Dispatchers.IO) {
        val bookDir = filesDir.resolve(book.path)

        val pdf = Pdf(bookId = book.id)
        pdf.id = pdfDao.insert(pdf)

        val cover = "cover_${UUID.randomUUID()}.png"

        val fileDescriptor = ParcelFileDescriptor.open(
            bookDir.resolve("book.pdf"),
            ParcelFileDescriptor.MODE_READ_ONLY
        )

        val renderer = PdfRenderer(fileDescriptor)

        val page = renderer.openPage(0)
        val bitmap = createBitmap(page.width * 2, page.height * 2)

        page.render(
            bitmap,
            null,
            null,
            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
        )

        FileOutputStream(bookDir.resolve(cover)).use { out ->
            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                95,
                out
            )
        }

        page.close()
        renderer.close()
        fileDescriptor.close()

        val doc = io.github.yuroyami.kitepdf.PdfDocument.open(bookDir.resolve("book.pdf").readBytes())
        val docInfo = doc.info

        docInfo.title?.let {
            if(it.isEmpty()) return@let
            book.title = it
        }

        docInfo.author?.let {
            if(it.isEmpty()) return@let
            book.author = it
        }

        docInfo.subject?.let {
            if(it.isEmpty()) return@let
            book.summary = it
        }

        docInfo.keywords?.let {
            if(it.isEmpty()) return@let
            pdf.keywords = it
        }

//        docInfo.creationDate?. TODO() date

        book.cover = cover
        book.totalProgress = doc.pages.count()
        updateBook(book)

        createPdfBookmark(pdf.id)
    }

    suspend fun createComicIndex(
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

            var i = 0
            val comic = Comic(bookId = book.id)
            val comicId = comicDao.insert(comic)

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

                comicPageDao.insert(
                    ComicPage(
                        comicId = comicId,
                        panelNumber = i,
                        path = file,
                        width = width,
                        height = height
                    )
                )
            }

            comic.id = comicId
            comic.panels = i
            comicDao.update(comic)

            val coverFileName = files.first()
            val coverExtension = coverFileName.substringAfterLast('.', "")
            val cover = "cover_${UUID.randomUUID()}.${coverExtension}"
            val coverFile = bookDir.resolve(cover)

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

            createComicBookmark(comic.id)

            book.cover = cover
            book.totalProgress = i
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
        onFinishCbz: (String) -> Unit,
        onFinishPdf: (String) -> Unit,
        onFinishEpub: (String) -> Unit,
        onThrow: (Throwable?) -> Unit
    ) {
        val supportedFormats = arrayOf("cbz", "epub", "pdf")

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val filename = getFileName(context, uri)
                val extension = filename.substringAfterLast('.', "").lowercase(getDefault())

                if(!supportedFormats.contains(extension)) {
                    onThrow(UnsupportedFormatException())
                    return@withContext
                }

                val target = File(bookDir, "book.${extension}")

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

                when(extension) {
                    "cbz" -> {
                        onFinishCbz(filename)
                    }

                    "pdf" -> {
                        onFinishPdf(filename)
                    }
                }
            }
        }
    }
}

class UnsupportedFormatException(
    message: String = "Unsupported file format"
) : Exception(message)