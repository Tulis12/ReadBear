package dev.tulis.readbear.db

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.tulis.readbear.db.books.BookDao
import dev.tulis.readbear.db.books.Book
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


@Database(
    entities = [Book::class, Comic::class, ComicPage::class, ComicBookmark::class, Pdf::class, PdfBookmark::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun comicDao(): ComicDao
    abstract fun comicPageDao(): ComicPageDao
    abstract fun comicBookmarkDao(): ComicBookmarkDao
    abstract fun pdfDao(): PdfDao
    abstract fun pdfBookmarkDao(): PdfBookmarkDao
}