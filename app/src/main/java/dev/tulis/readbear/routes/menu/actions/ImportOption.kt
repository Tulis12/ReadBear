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
import dev.tulis.readbear.db.books.Book
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
            contentDescription = "Add"
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
                onFinish = {
                    originalName ->
                    onChangeImporting(false)

                    scope.launch {
                        val bookId = viewModel.addBook(
                            Book(
                                title = originalName.substringBeforeLast("."),
                                path = bookUuid.toString()
                            )
                        )

                        viewModel.createIndex(viewModel.getBook(bookId))
                    }
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
            text = { Text("Dodaj z pliku lokalnego...") },
            onClick = {
                expanded = false
                launcher.launch(arrayOf("*/*"))
            }
        )

        DropdownMenuItem(
            text = { Text("Edytuj") },
            onClick = { expanded = false }
        )
    }
}