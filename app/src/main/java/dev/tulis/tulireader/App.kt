package dev.tulis.tulireader

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import dev.tulis.tulireader.routes.menu.Menu
import dev.tulis.tulireader.routes.Route
import dev.tulis.tulireader.routes.edit.EditBookDetails
import dev.tulis.tulireader.routes.reader.WebtoonReader

@Composable
fun App(
    navController: NavHostController
) {

    NavHost(
        navController = navController,
        startDestination = Route.Menu
    ) {

        composable<Route.Menu> {
            Menu (
                onOpenBook = {
                    navController.navigate(Route.Reader(it))
                },
                onEditBook = {
                    navController.navigate(Route.EditBookDetails(it))
                }
            )
        }

        composable<Route.Reader> { entry ->
            val args = entry.toRoute<Route.Reader>()

            WebtoonReader(
                bookId = args.bookId
            ) {
                navController.popBackStack()
            }
        }

        composable<Route.EditBookDetails> { entry ->
            val args = entry.toRoute<Route.Reader>()

            EditBookDetails(bookId = args.bookId) {
                navController.popBackStack()
            }
        }
    }
}