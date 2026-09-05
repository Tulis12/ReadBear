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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import dev.tulis.readbear.R
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
            contentDescription = stringResource(R.string.delete)
        )
    }

    if(showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            title = {
                Text(stringResource(R.string.confirm))
            },
            text = {
                Text(
                    pluralStringResource(
                    R.plurals.are_you_sure_you_want_to_delete_x_elements,
                    selectedItems.count()
                    )
                )
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
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}