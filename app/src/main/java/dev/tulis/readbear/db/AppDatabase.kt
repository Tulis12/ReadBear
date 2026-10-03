package dev.tulis.readbear.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.tulis.readbear.db.converters.InstantConverter
import dev.tulis.readbear.db.entities.books.Book
import dev.tulis.readbear.db.entities.books.BookDao
import dev.tulis.readbear.db.entities.comics.Comic
import dev.tulis.readbear.db.entities.comics.ComicDao
import dev.tulis.readbear.db.entities.comics.bookmarks.ComicBookmark
import dev.tulis.readbear.db.entities.comics.bookmarks.ComicBookmarkDao
import dev.tulis.readbear.db.entities.comics.pages.ComicPage
import dev.tulis.readbear.db.entities.comics.pages.ComicPageDao
import dev.tulis.readbear.db.entities.epubs.Epub
import dev.tulis.readbear.db.entities.epubs.EpubDao
import dev.tulis.readbear.db.entities.epubs.bookmarks.EpubBookmark
import dev.tulis.readbear.db.entities.epubs.bookmarks.EpubBookmarkDao
import dev.tulis.readbear.db.entities.pdfs.Pdf
import dev.tulis.readbear.db.entities.pdfs.PdfDao
import dev.tulis.readbear.db.entities.pdfs.bookmarks.PdfBookmark
import dev.tulis.readbear.db.entities.pdfs.bookmarks.PdfBookmarkDao
import dev.tulis.readbear.db.entities.snippets.Snippet
import dev.tulis.readbear.db.entities.snippets.SnippetDao


@Database(
    entities = [
        Book::class,
        Comic::class, ComicPage::class, ComicBookmark::class,
        Pdf::class, PdfBookmark::class,
        Epub::class, EpubBookmark::class,
        Snippet::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(InstantConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao

    abstract fun comicDao(): ComicDao
    abstract fun comicPageDao(): ComicPageDao
    abstract fun comicBookmarkDao(): ComicBookmarkDao

    abstract fun pdfDao(): PdfDao
    abstract fun pdfBookmarkDao(): PdfBookmarkDao

    abstract fun epubDao(): EpubDao
    abstract fun epubBookmarkDao(): EpubBookmarkDao

    abstract fun snippetDao(): SnippetDao
}