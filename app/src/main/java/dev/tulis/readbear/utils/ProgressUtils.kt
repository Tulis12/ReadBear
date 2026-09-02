package dev.tulis.readbear.utils

import dev.tulis.readbear.db.books.Book
import kotlin.math.ceil
import kotlin.math.roundToInt

fun readingTime(readingTime: Long): String {
    val readingTimeS = readingTime / 1000f

    val h = (readingTimeS / 3600).toInt()
    val m = ceil((readingTimeS - h * 3600) / 60).toInt()

    return "${h}h ${m}m"
}

fun readingProgress(book: Book): String {
    var text = "${((book.progress / book.totalProgress.toFloat()) * 100).roundToInt()}%"
    if(book.progress == 0 && book.readAlready > 0) {
        text = "100%"
    }

    return text
}