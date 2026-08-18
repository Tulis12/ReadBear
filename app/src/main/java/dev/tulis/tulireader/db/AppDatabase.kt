package dev.tulis.tulireader.db

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.tulis.tulireader.db.bookmarks.Bookmark
import dev.tulis.tulireader.db.bookmarks.BookmarkDao
import dev.tulis.tulireader.db.books.BookDao
import dev.tulis.tulireader.db.books.Book
import dev.tulis.tulireader.db.pages.Page
import dev.tulis.tulireader.db.pages.PageDao


@Database(
    entities = [Book::class, Page::class, Bookmark::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun pageDao(): PageDao
    abstract fun bookmarkDao(): BookmarkDao
}