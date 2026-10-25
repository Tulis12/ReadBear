package dev.tulis.readbear.routes.menu.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tulis.readbear.db.entities.folders.bookFolder.BookFolderItem

@Stable
class SelectionState {
    val selectedItems = mutableStateListOf<BookFolderItem>()
    var selectionMode by mutableStateOf(false)


    fun addSelectedItem(item: BookFolderItem) {
        selectedItems.add(item)
    }

    fun removeSelectedItem(item: BookFolderItem) {
        selectedItems.remove(item)
    }

    fun clearSelectedItems() {
        selectedItems.clear()
    }
}

@Composable
fun rememberSelectionState(): SelectionState {
    return remember {
        SelectionState()
    }
}