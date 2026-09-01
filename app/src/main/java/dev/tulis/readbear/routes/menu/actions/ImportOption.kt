package dev.tulis.readbear.routes.menu.actions

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.tulis.readbear.R
import dev.tulis.readbear.db.books.Book
import dev.tulis.readbear.db.books.BookType
import dev.tulis.readbear.routes.menu.LibraryViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun ImportOption(
    viewModel: LibraryViewModel,
    onChangeImporting: (Boolean) -> Unit,
    onThrow: (Throwable?) -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    IconButton(
        onClick = {
            expanded = true;
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
            onChangeImporting(true)

            val bookUuid = UUID.randomUUID()
            val bookDir = context.filesDir.resolve(bookUuid.toString())

            bookDir.mkdirs()

            viewModel.copyFileToAppStorage(
                context = context,
                bookDir = bookDir,
                uri = it,
                onFinishCbz = {
                    originalName ->

                    scope.launch {
                        val bookId = viewModel.addBook(
                            Book(
                                title = originalName.substringBeforeLast("."),
                                path = bookUuid.toString(),
                                type = BookType.Comic
                            )
                        )

                        viewModel.createComicIndex(viewModel.getBook(bookId))
                        onChangeImporting(false)
                    }
                },
                onFinishPdf = {
                    originalName ->
                    onChangeImporting(false)

                    scope.launch {
                        val bookId = viewModel.addBook(
                            Book(
                                title = originalName.substringBeforeLast("."),
                                path = bookUuid.toString(),
                                type = BookType.Pdf
                            )
                        )

                        viewModel.createPdfIndex(viewModel.getBook(bookId))
                    }
                },
                onFinishEpub = {

                },
                onThrow = {
                    throwable ->

                    onChangeImporting(false)
                    onThrow(throwable)
                }
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