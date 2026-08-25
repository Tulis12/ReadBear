package dev.tulis.readbear.routes

import kotlinx.serialization.Serializable
@Serializable
sealed interface Route {

    @Serializable
    data object Menu : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data class ComicReader(
        val comicId: Long
    ) : Route

    @Serializable
    data class BookDetails(
        val bookId: Long
    ) : Route

    @Serializable
    data class EditBookDetails(
        val bookId: Long
    ) : Route
}