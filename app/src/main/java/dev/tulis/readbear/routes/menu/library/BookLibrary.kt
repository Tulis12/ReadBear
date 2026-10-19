package dev.tulis.readbear.routes.menu.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tulis.readbear.R
import dev.tulis.readbear.db.Settings
import dev.tulis.readbear.db.entities.folders.bookFolder.BookFolderItem
import dev.tulis.readbear.db.entities.folders.bookFolder.BookFolderItemType
import dev.tulis.readbear.routes.menu.LibraryViewModel

@Composable
fun BookLibrary(
    viewModel: LibraryViewModel = hiltViewModel(),
    settings: Settings.SettingsState,
    padding: PaddingValues,
    selectionState: SelectionState,
    onOpenBook: (Long) -> Unit,
    onReady: () -> Unit
) {
    Box(
        modifier = Modifier.padding(padding).fillMaxSize()
    ) {
//        val booksFlow: List<Book>? by viewModel.books.collectAsState(null)
        val itemsFlow: List<BookFolderItem>? by viewModel.getItems().collectAsStateWithLifecycle(null)
        val context = LocalContext.current

        val items = itemsFlow ?: return@Box

//        val books = booksFlow ?: return@Box

        LaunchedEffect(Unit) {
            onReady()
        }

        if(items.count() == 0) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(15.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.there_is_nothing_here),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = stringResource(R.string.add_a_book),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            return@Box
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(settings.columnCount),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(items) { item ->

                when(item.type) {
                    BookFolderItemType.Book -> {
                        val bookFlow by viewModel.getBookByItemId(item.id).collectAsState(null)
                        val book = bookFlow ?: return@items

                        BookCover(
                            item = item,
                            book = book,
                            selectionState = selectionState,
                            settings = settings,
                            onOpenBook = onOpenBook
                        )
                    }

                    BookFolderItemType.Folder -> {

                    }
                }
            }
        }
    }
}