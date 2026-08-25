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


@Database(
    entities = [Book::class, Comic::class, ComicPage::class, ComicBookmark::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun comicDao(): ComicDao
    abstract fun comicPageDao(): ComicPageDao
    abstract fun comicBookmarkDao(): ComicBookmarkDao
}