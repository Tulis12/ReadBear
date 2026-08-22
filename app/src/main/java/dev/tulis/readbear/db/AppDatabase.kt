package dev.tulis.readbear.db

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.tulis.readbear.db.bookmarks.Bookmark
import dev.tulis.readbear.db.bookmarks.BookmarkDao
import dev.tulis.readbear.db.books.BookDao
import dev.tulis.readbear.db.books.Book
import dev.tulis.readbear.db.pages.Page
import dev.tulis.readbear.db.pages.PageDao


@Database(
    entities = [Book::class, Page::class, Bookmark::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun pageDao(): PageDao
    abstract fun bookmarkDao(): BookmarkDao
}