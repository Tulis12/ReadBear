package dev.tulis.readbear.routes.menu.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DriveFolderUpload
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.tulis.readbear.R
import dev.tulis.readbear.db.Settings
import dev.tulis.readbear.db.entities.folders.bookFolder.BookFolderItemType
import dev.tulis.readbear.routes.menu.LibraryViewModel
import dev.tulis.readbear.routes.menu.MenuState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun BookLibrary(
    viewModel: LibraryViewModel = hiltViewModel(),
    settings: Settings.SettingsState,
    padding: PaddingValues,
    reorderState: ReorderState,
    selectionState: SelectionState,
    menuState: MenuState,
    currentParentId: Long?,
    onChangeCurrentParentId: (Long?) -> Unit,
    onOpenBook: (Long) -> Unit,
    onReady: () -> Unit
) {
    val historyTree = remember { mutableListOf<Long>() }
    val currentFolder by viewModel.getFolderById(currentParentId ?: -1).collectAsState(null)

    LaunchedEffect(Unit) {
        onReady()
    }

    val goBackFolder = {
        onChangeCurrentParentId(if (historyTree.isNotEmpty()) {
            historyTree.removeAt(historyTree.lastIndex)
            historyTree.lastOrNull()
        } else {
            null
        })
    }

    Crossfade(
        targetState = currentParentId,
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
    ) { parentId ->
        val unknown = stringResource(R.string.unknown)

        if(parentId == null) {
            menuState.resetNavigation()
            menuState.resetTitle()
        } else {
            menuState.updateTitle(currentFolder?.name ?: unknown)

            menuState.updateNavigation {
                IconButton(
                    onClick = {
                        goBackFolder()
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.go_back))
                }
            }
        }

        var hoveredFolderItemId by remember { mutableStateOf<Long?>(null) }
        var lastHoveredFolderItemId by remember { mutableStateOf<Long?>(null) }
        var hoverEndedAt by remember { mutableLongStateOf(0L) }

        val itemsFlow by viewModel
            .getItemsWithParentId(parentId)
            .collectAsState(initial = null)

        val dbItems = itemsFlow ?: return@Crossfade

        Crossfade(
            dbItems.isEmpty()
        ) { isEmpty ->
            if(isEmpty) {
                EmptyScreen()
                return@Crossfade
            }

            val items = remember { dbItems.toMutableStateList() }
            var animate by remember { mutableStateOf(false) }

            LaunchedEffect(currentParentId) {
                animate = false
                delay(50.milliseconds)
                animate = true
            }

            LaunchedEffect(dbItems) {
                items.clear()
                items.addAll(dbItems)
            }

            val lazyGridState = rememberLazyGridState()

            val reorderableLazyGridState =
                rememberReorderableLazyGridState(
                    lazyGridState,
                    onHoverStart = {
                        hoveredFolderItemId = it as Long
                    },
                    onHoverEnd = {
                        lastHoveredFolderItemId = hoveredFolderItemId
                        hoverEndedAt = System.currentTimeMillis()
                        hoveredFolderItemId = null
                    }
                ) { from, to ->
                    val fromIndex = items.indexOfFirst { it.id == from.key }
                    val toIndex = items.indexOfFirst { it.id == to.key }

                    if (fromIndex != -1 && toIndex != -1) {
                        val item = items.removeAt(fromIndex)
                        items.add(toIndex, item)
                    }
                }

            var extra by remember { mutableStateOf(false) }
            var targetExtra by remember { mutableStateOf(false) }

            LaunchedEffect(targetExtra) {
                if (!targetExtra) delay(100.milliseconds)
                extra = targetExtra
            }

            LazyVerticalGrid(
                state = lazyGridState,
                columns = GridCells.Fixed(settings.columnCount),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if(extra) {
                    item(span = { GridItemSpan(maxLineSpan) }, key = -1L) {
                        val isHovered = hoveredFolderItemId == -1L

                        val backgroundColor by animateColorAsState(
                            targetValue = if (isHovered) {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLow
                            },
                            animationSpec = tween(200),
                        )

                        Card(
                            colors = CardDefaults.cardColors(
                                backgroundColor
                            )
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(10.dp).fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DriveFolderUpload,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )

                                Spacer(Modifier.width(10.dp))

                                Text(
                                    text = stringResource(R.string.hover_here_to_go_up),
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                items(items, key = { it.id }) { item ->
                    ReorderableItem(
                        reorderableLazyGridState,
                        key = item.id,
                        enabled = item.type != BookFolderItemType.Folder,
                        animateItemModifier = Modifier
                            .then(
                                if(animate) Modifier.animateItem() else Modifier
                            )
                    ) { _ ->
                        Box {
                            when(item.type) {
                                BookFolderItemType.Book -> {
                                    val bookFlow by viewModel.getBookByItemId(item.id).collectAsState(null)
                                    val book = bookFlow ?: return@ReorderableItem

                                    BookCover(
                                        item = item,
                                        book = book,
                                        selectionState = selectionState,
                                        settings = settings,
                                        onOpenBook = onOpenBook
                                    )
                                }

                                BookFolderItemType.Folder -> {
                                    val folderFlow by viewModel.getFolderByItemId(item.id).collectAsState(null)
                                    val folder = folderFlow ?: return@ReorderableItem

                                    val isHovered = hoveredFolderItemId == item.id

                                    val backgroundColor by animateColorAsState(
                                        targetValue = if (isHovered) {
                                            lerp(
                                                MaterialTheme.colorScheme.secondaryContainer,
                                                MaterialTheme.colorScheme.onSecondary,
                                                0.6f
                                            )
                                        } else {
                                            MaterialTheme.colorScheme.secondaryContainer
                                        },
                                        animationSpec = tween(300),
                                    )

                                    FolderCover(
                                        item = item,
                                        folder = folder,
                                        selectionState = selectionState,
                                        settings = settings,
                                        isHovered = isHovered,
                                        backgroundColor = backgroundColor,
                                        onOpenFolder = { folder ->
                                            onChangeCurrentParentId(folder.id)
                                            historyTree.add(folder.id)
                                        }
                                    )
                                }
                            }

                            val scope = rememberCoroutineScope()

                            AnimatedVisibility(
                                reorderState.enabled,
                                enter = fadeIn(),
                                exit = fadeOut(),
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                IconButton(
                                    modifier = Modifier.draggableHandle(
                                        onDragStarted = {
                                            selectionState.clearSelectedItems()
                                            selectionState.selectionMode = false
                                            if(currentParentId != null) targetExtra = true
                                        },
                                        onDragStopped = {
                                            targetExtra = false
                                            val now = System.currentTimeMillis()

                                            val targetFolderId = hoveredFolderItemId
                                                ?: if (now - hoverEndedAt < 100L) lastHoveredFolderItemId else null

                                            hoveredFolderItemId = null
                                            lastHoveredFolderItemId = null
                                            hoverEndedAt = 0L

                                            targetFolderId?.let { id ->
                                                if(id == -1L) {
                                                    items.remove(item)

                                                    item.parentId = if (historyTree.isNotEmpty()) {
                                                        historyTree.getOrNull(historyTree.lastIndex - 1)
                                                    } else {
                                                        null
                                                    }

                                                    viewModel.updateItem(item)
                                                }

                                                scope.launch {
                                                    val folder = viewModel.getFolderByItemIdSuspend(id)

                                                    folder?.let {
                                                        items.remove(item)
                                                        item.parentId = it.id
                                                        viewModel.updateItem(item)
                                                    }
                                                }
                                            }

                                            val reorderedItems = items.mapIndexedNotNull { index, item ->
                                                val newPosition = index.toLong()

                                                if (item.positionIndex != newPosition) {
                                                    item.copy(positionIndex = newPosition)
                                                } else {
                                                    null
                                                }
                                            }

                                            viewModel.savePositions(reorderedItems)
                                        },
                                    ),
                                    colors = IconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.surfaceContainer,
                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                        disabledContentColor = MaterialTheme.colorScheme.surfaceContainer
                                    ),
                                    onClick = {},
                                ) {
                                    Icon(Icons.Rounded.DragHandle, contentDescription = "Reorder")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyScreen() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
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
}