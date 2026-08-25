package dev.tulis.readbear

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
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
import dev.tulis.readbear.routes.reader.WebtoonReader
import kotlinx.coroutines.launch

@Composable
fun App(
    viewModel: AppViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val scope = rememberCoroutineScope()

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

                            else -> TODO()
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

            BookDetails(args.bookId) {
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
}