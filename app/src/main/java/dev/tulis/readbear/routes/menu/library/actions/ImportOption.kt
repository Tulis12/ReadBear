package dev.tulis.readbear.routes.menu.library.actions

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.tulis.readbear.AppViewModel
import dev.tulis.readbear.R
import dev.tulis.readbear.db.entities.books.Book
import dev.tulis.readbear.db.entities.books.BookType
import dev.tulis.readbear.db.entities.folders.bookFolder.BookFolderItem
import dev.tulis.readbear.db.entities.folders.bookFolder.BookFolderItemType
import dev.tulis.readbear.routes.menu.LibraryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

fun importBook(
    uri: Uri,
    context: Context,
    viewModel: LibraryViewModel,
    scope: CoroutineScope,
    onChangeImporting: (Boolean) -> Unit,
    onThrow: (Throwable?) -> Unit
) {
    onChangeImporting(true)

    val booksDir = context.filesDir.resolve("books")

    val bookUuid = UUID.randomUUID()
    val bookDir = booksDir.resolve(bookUuid.toString())

    bookDir.mkdirs()

    val createBookFolderItem: suspend () -> Long = {
        val index = viewModel.getMaxPositionIndex() ?: -1

        viewModel.addBookFolderItem(
            BookFolderItem(parentId = null, positionIndex = index + 1, type = BookFolderItemType.Book)
        )
    }

    viewModel.copyFileToAppStorage(
        context = context,
        bookDir = bookDir,
        uri = uri,
        onFinishCbz = {
                originalName ->

            scope.launch {
                val bookId = viewModel.addBook(
                    Book(
                        title = originalName.substringBeforeLast("."),
                        path = bookUuid.toString(),
                        type = BookType.Comic,
                        folderItemId = createBookFolderItem()
                    )
                )

                viewModel.createComicIndex(viewModel.getBook(bookId))
                onChangeImporting(false)
            }
        },
        onFinishPdf = {
                originalName ->

            scope.launch {
                val bookId = viewModel.addBook(
                    Book(
                        title = originalName.substringBeforeLast("."),
                        path = bookUuid.toString(),
                        type = BookType.Pdf,
                        folderItemId = createBookFolderItem()
                    )
                )

                viewModel.createPdfIndex(viewModel.getBook(bookId))
                onChangeImporting(false)
            }
        },
        onFinishEpub = {
                originalName ->

            scope.launch {
                val bookId = viewModel.addBook(
                    Book(
                        title = originalName.substringBeforeLast("."),
                        path = bookUuid.toString(),
                        type = BookType.Epub,
                        folderItemId = createBookFolderItem()
                    )
                )

                viewModel.createEpubIndex(viewModel.getBook(bookId))
                onChangeImporting(false)
            }
        },
        onThrow = {
                throwable ->

            onChangeImporting(false)
            onThrow(throwable)
        }
    )
}

@Composable
fun ImportOption(
    appViewModel: AppViewModel,
    viewModel: LibraryViewModel,
    onChangeImporting: (Boolean) -> Unit,
    onThrow: (Throwable?) -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(appViewModel) {
        appViewModel.importRequest.collect { uri ->
            importBook(
                uri = uri,
                context = context,
                viewModel = viewModel,
                scope = scope,
                onChangeImporting = onChangeImporting,
                onThrow = onThrow
            )
        }
    }

    IconButton(
        onClick = {
            expanded = true
        }
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = stringResource(R.string.import_book)
        )
    }


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            importBook(    uri,
                context,
                viewModel,
                scope,
                onChangeImporting,
            onThrow
            )
        }
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = {
                Text(stringResource(R.string.add_local_resource))
           },
            onClick = {
                expanded = false
                launcher.launch(
                    arrayOf(
                        "application/pdf",
                        "application/epub+zip",
                        "application/zip",
                        "application/vnd.comicbook+zip",
                        "*/*"
                    )
                )
            }
        )
    }
}