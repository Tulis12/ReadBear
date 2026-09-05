package dev.tulis.readbear

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import dev.tulis.readbear.db.books.BookType
import dev.tulis.readbear.routes.menu.Menu
import dev.tulis.readbear.routes.Route
import dev.tulis.readbear.routes.edit.EditBookDetails
import dev.tulis.readbear.routes.info.BookDetails
import dev.tulis.readbear.routes.reader.comic.WebtoonReader
import dev.tulis.readbear.routes.reader.pdf.PdfReader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

var changingLanguage by mutableStateOf(false)

fun changeLanguage() {
    changingLanguage = true
}

@Composable
fun App(
    viewModel: AppViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(changingLanguage) {
        delay(1.seconds)
        changingLanguage = false
    }

    NavHost(
        navController = navController,
        startDestination = Route.Menu
    ) {

        composable<Route.Menu> {
            Menu (
                onOpenBook = {
                    scope.launch {
                        val book = viewModel.getBook(it)

                        when(book.type) {
                            BookType.Comic -> {
                                navController.navigate(Route.ComicReader(
                                    viewModel.getComicByBookId(it).id
                                ))
                            }

                            BookType.Pdf -> {
                                navController.navigate(Route.PdfReader(
                                    viewModel.getPdfByBookId(it).id
                                ))
                            }

                            else -> {
                                println(book.type)
                                TODO()
                            }
                        }
                    }
                },
                onEditBook = {
                    navController.navigate(Route.EditBookDetails(it))
                },
                onBookDetails = {
                    navController.navigate(Route.BookDetails(it))
                }
            )
        }

        composable<Route.PdfReader> { entry ->
            val args = entry.toRoute<Route.PdfReader>()

            PdfReader(
                pdfId = args.pdfId
            ) {
                navController.popBackStack()
            }
        }

        composable<Route.ComicReader> { entry ->
            val args = entry.toRoute<Route.ComicReader>()

            WebtoonReader(
                comicId = args.comicId
            ) {
                navController.popBackStack()
            }
        }

        composable<Route.BookDetails> { entry ->
            val args = entry.toRoute<Route.BookDetails>()

            BookDetails(bookId = args.bookId) {
                navController.popBackStack()
            }
        }

        composable<Route.EditBookDetails> { entry ->
            val args = entry.toRoute<Route.EditBookDetails>()

            EditBookDetails(bookId = args.bookId) {
                navController.popBackStack()
            }
        }
    }

    AnimatedVisibility(changingLanguage,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xff19120C)))
    }
}