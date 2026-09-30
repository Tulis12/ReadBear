package dev.tulis.readbear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
    navController: NavHostController,
    onReady: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val renderEpubCoverCopy = renderEpubCover
    if(renderEpubCoverCopy != null) {
        RenderEpubCover(renderEpubCoverCopy)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            buildAnnotatedString {
                append("You 100% should not see this. But if you do, please for the love of god create an issue: ")

                withLink(
                    LinkAnnotation.Url("https://github.com/Tulis12/ReadBear")
                ) {
                    append("GitHub")
                }

                append(" and attach a screenshot. Or not. Just please tell me. I love you. You are the best human being. You will succeed in life.")

                append("\n\n")
                append(currentRoute)
            },
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.7f),
            textAlign = TextAlign.Justify,
            color = Color.White
        )
    }

    NavHost(
        navController = navController,
        startDestination = Route.Menu,
        modifier = Modifier.fillMaxSize().background(Color.Red)
    ) {

        composable<Route.Menu> {
            DebugCard("Menu")

            Menu(
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
                },
                onReady = onReady
            )
        }

        composable<Route.ComicReader> { entry ->
            DebugCard("Comic Reader")

            val args = entry.toRoute<Route.ComicReader>()

            WebtoonReader(
                comicId = args.comicId
            ) {
                navController.popBackStack()
            }
        }

        composable<Route.PdfReader> { entry ->
            DebugCard("PdfReader")

            val args = entry.toRoute<Route.PdfReader>()

            PdfReader(
                pdfId = args.pdfId
            ) {
                navController.popBackStack()
            }
        }

        composable<Route.EpubReader> { entry ->
            DebugCard("Epub Reader")

            val args = entry.toRoute<Route.EpubReader>()

            EpubReader(
                epubId = args.epubId
            ) {
                navController.popBackStack()
            }
        }

        composable<Route.BookDetails> { entry ->
            DebugCard("Book Details")

            val args = entry.toRoute<Route.BookDetails>()

            BookDetails(bookId = args.bookId) {
                navController.popBackStack()
            }
        }

        composable<Route.EditBookDetails> { entry ->
            DebugCard("Edit Book Details")

            val args = entry.toRoute<Route.EditBookDetails>()

            EditBookDetails(bookId = args.bookId) {
                navController.popBackStack()
            }
        }
    }
}

@Composable
fun DebugCard(text: String) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(text, fontSize = 20.sp, color = Color.White, modifier = Modifier.align(Alignment.Center))
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