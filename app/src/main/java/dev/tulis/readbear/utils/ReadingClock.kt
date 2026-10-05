package dev.tulis.readbear.utils

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import dev.tulis.readbear.db.entities.books.Book
import dev.tulis.readbear.routes.reader.pdf.PdfReaderViewModel

@Composable
fun readingClock(
    book: Book,
    viewModel: PdfReaderViewModel = hiltViewModel()
): () -> Unit {
    var lastReadingTime = System.currentTimeMillis()

    return {
        val currentTime = System.currentTimeMillis()

        if(currentTime - lastReadingTime > 600 * 1000) lastReadingTime = System.currentTimeMillis()
        book.readingTime += currentTime - lastReadingTime
        viewModel.updateBook(book)

        lastReadingTime = currentTime
    }
}