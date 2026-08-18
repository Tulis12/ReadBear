package dev.tulis.tulireader.db.bookmarks

import jakarta.inject.Inject

class BookmarkRepository @Inject constructor(
    private val dao: BookmarkDao
) {
    suspend fun add(bookmark: Bookmark) {
        dao.insert(bookmark)
    }

    suspend fun update(bookmark: Bookmark) {
        dao.update(bookmark)
    }

    suspend fun get(bookId: Long): Bookmark? {
        return dao.getByBookId(bookId)
    }
}