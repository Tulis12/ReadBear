package dev.tulis.readbear

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import dev.tulis.readbear.db.books.Book
import dev.tulis.readbear.db.books.BookType
import dev.tulis.readbear.routes.Route
import dev.tulis.readbear.routes.edit.EditBookDetails
import dev.tulis.readbear.routes.info.BookDetails
import dev.tulis.readbear.routes.menu.Menu
import dev.tulis.readbear.routes.reader.comic.WebtoonReader
import dev.tulis.readbear.routes.reader.epub.EpubReader
import dev.tulis.readbear.routes.reader.epub.createEpubCover
import dev.tulis.readbear.routes.reader.pdf.PdfReader
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.util.UUID

var renderEpubCover: Book? by mutableStateOf(null)

@Composable
fun App(
    viewModel: AppViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val scope = rememberCoroutineScope()

    val renderEpubCoverCopy = renderEpubCover
    if(renderEpubCoverCopy != null) {
        RenderEpubCover(renderEpubCoverCopy)
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

                            BookType.Epub -> {
                                navController.navigate(Route.EpubReader(
                                    viewModel.getEpubByBookId(it).id
                                ))
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

        composable<Route.ComicReader> { entry ->
            val args = entry.toRoute<Route.ComicReader>()

            WebtoonReader(
                comicId = args.comicId
            ) {
                navController.popBackStack()
            }
        }

        composable<Route.PdfReader> { entry ->
            val args = entry.toRoute<Route.PdfReader>()

            PdfReader(
                pdfId = args.pdfId
            ) {
                navController.popBackStack()
            }
        }

        composable<Route.EpubReader> { entry ->
            val args = entry.toRoute<Route.EpubReader>()

            EpubReader(
                epubId = args.epubId
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
}

@Composable
fun RenderEpubCover(
    book: Book,
    viewModel: AppViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val filesDir = context.filesDir

    val bookDir = filesDir.resolve(book.path)

    val cover = "cover_${UUID.randomUUID()}.jpeg"
    val coverFile = bookDir.resolve(cover)

    val reader = nl.siegmann.epublib.epub.EpubReader()

    val input = FileInputStream(bookDir.resolve("book.epub"))
    val bookEpub = reader.readEpub(input)

    val spine = bookEpub.spine

    spine.spineReferences[0].let { element ->
        val resource = element.resource

        val xhtml = resource.inputStream
            .bufferedReader()
            .readText()

        createEpubCover(xhtml, bookEpub.resources, coverFile, onFinish = {
            renderEpubCover = null
            viewModel.updateBookCover(book.id, cover)
        })
    }
}