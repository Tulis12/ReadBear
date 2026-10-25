package dev.tulis.readbear.routes.menu.library.actions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import dev.tulis.readbear.AppViewModel
import dev.tulis.readbear.R
import dev.tulis.readbear.db.entities.folders.bookFolder.BookFolderItem
import dev.tulis.readbear.db.entities.folders.bookFolder.BookFolderItemType
import dev.tulis.readbear.routes.menu.LibraryViewModel
import dev.tulis.readbear.routes.menu.library.ReorderState
import dev.tulis.readbear.routes.menu.library.SelectionState

@Composable
fun Actions(
    appViewModel: AppViewModel,
    viewModel: LibraryViewModel = hiltViewModel(),
    reorderState: ReorderState,
    selectionState: SelectionState,
    items: List<BookFolderItem>,
    currentPageId: Long?,
    callbacks: ActionsCallbacks
) {
    if(!selectionState.selectionMode) {
        IconButton(
            onClick = {
                callbacks.onShowSheet()
            }
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = stringResource(R.string.settings)
            )
        }

        IconButton(
            onClick = {
                reorderState.enabled = !reorderState.enabled
            }
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Sort,
                contentDescription = stringResource(R.string.reorder)
            )
        }

        ImportOption(
            appViewModel = appViewModel,
            viewModel = viewModel,
            onChangeImporting = {
                callbacks.onChangeImporting(it)
            },
            onThrow = {
                callbacks.onThrow(it)
            },
            onCreateFolder = callbacks.onCreateFolder,
            currentParentId = currentPageId
        )

        return
    }

    IconButton(
        onClick = {
            if(selectionState.selectedItems.containsAll(items)) {
                selectionState.selectedItems.clear()
                selectionState.selectionMode = false
                return@IconButton
            }

            selectionState.selectedItems.clear()
            selectionState.selectedItems.addAll(items)
        }
    ) {
        if(selectionState.selectedItems.containsAll(items)) {
            Icon(
                Icons.Default.Deselect,
                contentDescription = stringResource(R.string.deselect)
            )
        } else {
            Icon(
                Icons.Default.SelectAll,
                contentDescription = stringResource(R.string.select_all)
            )
        }
    }

    DeleteOption(viewModel, selectionState)

    if(selectionState.selectedItems.count() == 1) {
        val item = selectionState.selectedItems.first()

        when(item.type) {
            BookFolderItemType.Book -> {
                val bookFlow by viewModel.getBookByItemId(item.id).collectAsState(null)
                val book = bookFlow ?: return

                IconButton(
                    onClick = {
                        callbacks.onEditBook(book.id)
                    }
                ) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                }

                IconButton(
                    onClick = {
                        callbacks.onBookDetails(book.id)
                    }
                ) {
                    Icon(Icons.Default.Info, contentDescription = stringResource(R.string.info))
                }
            }

            BookFolderItemType.Folder -> {
//                val folderFlow by viewModel.getFolderByItemId(item.id).collectAsState(null) // TODO()
//                val folder = folderFlow ?: return
//
//                IconButton(
//                    onClick = {
//                        actionsCallbacks.onEditFolder(folder.id)
//                    }
//                ) {
//                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
//                }
//
//                IconButton(
//                    onClick = {
//                        actionsCallbacks.onFolderDetails(folder.id)
//                    }
//                ) {
//                    Icon(Icons.Default.Info, contentDescription = stringResource(R.string.info))
//                }
            }
        }


    }
}

data class ActionsCallbacks(
    val onThrow: (Throwable?) -> Unit,
    val onShowSheet: () -> Unit,
    val onChangeImporting: (Boolean) -> Unit,
    val onEditBook: (Long) -> Unit,
    val onBookDetails: (Long) -> Unit,

    val onCreateFolder: () -> Unit,
    val onEditFolder: (Long) -> Unit,
    val onFolderDetails: (Long) -> Unit,
)