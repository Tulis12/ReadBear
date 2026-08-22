package dev.tulis.readbear.db.pages

import jakarta.inject.Inject

class PageRepository @Inject constructor(
    private val dao: PageDao
) {
    suspend fun addPage(page: Page) {
        dao.insert(page)
    }
}