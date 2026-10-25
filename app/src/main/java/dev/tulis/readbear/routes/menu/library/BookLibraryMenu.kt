package dev.tulis.readbear.routes.menu.library

import android.system.ErrnoException
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import dev.tulis.readbear.AppViewModel
import dev.tulis.readbear.R
import dev.tulis.readbear.db.Settings
import dev.tulis.readbear.db.entities.folders.bookFolder.BookFolder
import dev.tulis.readbear.db.entities.folders.bookFolder.BookFolderItemType
import dev.tulis.readbear.routes.menu.LibraryViewModel
import dev.tulis.readbear.routes.menu.MenuState
import dev.tulis.readbear.routes.menu.UnsupportedFormatException
import dev.tulis.readbear.routes.menu.library.actions.Actions
import dev.tulis.readbear.routes.menu.library.actions.ActionsCallbacks
import dev.tulis.readbear.settings.BottomSettingsSheet
import dev.tulis.readbear.utils.BackgroundPattern
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookLibraryMenu(
    viewModel: LibraryViewModel = hiltViewModel(),
    appViewModel: AppViewModel,
    menuState: MenuState,
    reorderState: ReorderState,
    callbacks: BookLibraryMenuCallbacks,
    padding: PaddingValues
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val itemsFlow by viewModel.getItemsWithParentId(null).collectAsState(null)
    val items = itemsFlow ?: run {
        menuState.resetActions()
        return
    }

    val settingsFlow by Settings.getSettings(context).collectAsState(null)
    val settings = settingsFlow ?: run {
        menuState.resetActions()
        return
    }

    var currentParentId: Long? by remember { mutableStateOf(null) }

    val selectionState = rememberSelectionState()
    var importing by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var creatingFolder by remember { mutableStateOf(false) }

    if(showSheet) {
        BottomSettingsSheet(
            sheetState = sheetState,
            onHide = {
                scope.launch {
                    sheetState.hide()
                    showSheet = false
                }
            }
        )
    }



    Box {
        BackgroundPattern(modifier = Modifier.matchParentSize())

        BookLibrary(
            settings = settings,
            padding = padding,
            selectionState = selectionState,
            menuState = menuState,
            currentParentId = currentParentId,
            onChangeCurrentParentId = {
                currentParentId = it
            },
            reorderState = reorderState,
            onOpenBook = {
                callbacks.onOpenBook(it)
            },
            onReady = callbacks.onReady
        )
    }

    if(importing) {
        Box {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )

            Column(
                modifier = Modifier.align(Alignment.Center),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text(stringResource(R.string.importing), color = Color.White)
            }
        }
    }

    if(creatingFolder) {
        var tried by remember { mutableStateOf(false) }
        var folderName: String? by remember { mutableStateOf(null) }

        AlertDialog(
            onDismissRequest = {
                creatingFolder = false
            },
            title = {
                Text(stringResource(R.string.create_folder))
            },
            text = {
                // cover TODO()

                OutlinedTextField(
                    value = folderName ?: "",
                    onValueChange = {
                        if(it.isBlank()) folderName = null
                        folderName = it
                    },
                    label = {
                        Text(stringResource(R.string.name))
                    },
                    isError = tried && folderName?.isBlank() == true,
                    supportingText = {
                        if(tried && folderName?.isBlank() == true) {
                            Text(stringResource(R.string.name_not_empty))
                        }
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        tried = true

                        val folder = folderName
                        if(folder.isNullOrBlank()) return@TextButton

                        scope.launch {
                            viewModel.createFolder(
                                BookFolder(
                                    name = folder,
                                    itemId = viewModel.addBookFolderItem(BookFolderItemType.Folder, parentId = currentParentId),
                                )
                            )
                        }

                        creatingFolder = false
                    }
                ) {
                    Text(stringResource(R.string.create))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        creatingFolder = false
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    val noSpaceMessage = stringResource(R.string.error_no_space_left)
    val copyFailedMessage = stringResource(R.string.error_copy_failed)
    val unsupportedFormatMessage = stringResource(R.string.error_unsupported_format)

    menuState.updateActions actions@{
        Actions(
            appViewModel = appViewModel,
            selectionState = selectionState,
            items = items,
            currentPageId = currentParentId,
            reorderState = reorderState,
            callbacks = ActionsCallbacks(
                onThrow = { throwable ->
                    scope.launch {
                        val message = when (throwable) {
                            is ErrnoException -> {
                                noSpaceMessage
                            }

                            is UnsupportedFormatException -> {
                                unsupportedFormatMessage
                            }

                            else -> {
                                copyFailedMessage
                            }
                        }

                        snackbarHostState.showSnackbar(
                            message = message
                        )
                    }
                },
                onShowSheet = {
                    showSheet = true

                    scope.launch {
                        awaitFrame()
                        awaitFrame()
                        awaitFrame()
                        awaitFrame()
                        awaitFrame()
                        sheetState.show()
                    }
                },
                onChangeImporting = {
                    importing = it
                },
                onCreateFolder = {
                    creatingFolder = true
                },
                onEditBook = callbacks.onEditBook,
                onBookDetails = callbacks.onBookDetails,
                onEditFolder = callbacks.onEditFolder,
                onFolderDetails = callbacks.onFolderDetails
            )
        )
    }
}

data class BookLibraryMenuCallbacks(
    val onOpenBook: (Long) -> Unit,
    val onEditBook: (Long) -> Unit,
    val onBookDetails: (Long) -> Unit,

    val onEditFolder: (Long) -> Unit,
    val onFolderDetails: (Long) -> Unit,

    val onReady: () -> Unit,
)