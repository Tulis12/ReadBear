package dev.tulis.readbear.routes.menu.actions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.tulis.readbear.routes.menu.LibraryViewModel
import kotlinx.coroutines.launch

@Composable
fun DeleteOption(
    viewModel: LibraryViewModel,
    selectedItems: List<Long>,
    onClearSelectedItemsAndDisableSelectionMode: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    IconButton(
        onClick = {
            showDialog = true;
        }
    ) {
        Icon(
            Icons.Default.Delete,
            contentDescription = "Delete"
        )
    }

    if(showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            title = {
                Text("Potwierdzenie")
            },
            text = {
                if(selectedItems.count() == 1)
                    Text("Czy na pewno chcesz usunąć ten element?")
                else Text("Czy na pewno chcesz usunąć ${selectedItems.count()} elementów?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedItems.forEach {
                            scope.launch {
                                val book = viewModel.getBook(it)
                                viewModel.removeBook(book)
                            }
                        }

                        onClearSelectedItemsAndDisableSelectionMode()
                        showDialog = false
                    }
                ) {
                    Text("Usuń")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text("Anuluj")
                }
            }
        )
    }
}