package dev.tulis.readbear

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import dev.tulis.readbear.routes.menu.Menu
import dev.tulis.readbear.routes.Route
import dev.tulis.readbear.routes.edit.EditBookDetails
import dev.tulis.readbear.routes.reader.WebtoonReader

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