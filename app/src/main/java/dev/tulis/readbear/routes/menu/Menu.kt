package dev.tulis.readbear.routes.menu

import android.system.ErrnoException
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.BlendMode

//import androidx.compose.animation.animateFloatAsState

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import dev.tulis.readbear.R
import dev.tulis.readbear.db.Settings
import dev.tulis.readbear.db.Settings.dataStore
import dev.tulis.readbear.routes.menu.actions.DeleteOption
import dev.tulis.readbear.routes.menu.actions.ImportOption
import kotlinx.coroutines.android.awaitFrame

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun Menu(
    viewModel: LibraryViewModel = hiltViewModel(),
    onOpenBook: (Long) -> Unit,
    onEditBook: (Long) -> Unit,
    onBookDetails: (Long) -> Unit
) {
    val context = LocalContext.current;
    val scope = rememberCoroutineScope()

    val books by viewModel.books.collectAsState()

    val selectedItems = remember {
        mutableStateListOf<Long>()
    }

    var selectionMode by remember { mutableStateOf(false) }
    val settingsFlow by Settings.getSettings(context).collectAsState(null)
    val settings = settingsFlow ?: return

    var importing by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar (
                title = {
                    Row {
                        Text(stringResource(R.string.base_app_name))
                    }
                },
                actions = {
                    if(!selectionMode) {

                        IconButton(
                            onClick = {
                                showSheet = true

                                scope.launch {
                                    awaitFrame()
                                    awaitFrame()
                                    awaitFrame()
                                    awaitFrame()
                                    awaitFrame()
                                    sheetState.show()
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings)
                            )
                        }

                        val noSpaceMessage = stringResource(R.string.error_no_space_left)
                        val copyFailedMessage = stringResource(R.string.error_copy_failed)
                        val unsupportedFormatMessage = stringResource(R.string.error_unsupported_format)

                        ImportOption(
                            viewModel,
                            onChangeImporting = {
                                importing = it
                            },
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
                            }
                        )

                        return@TopAppBar
                    }

                    IconButton(
                        onClick = {
                            val allBooksIds = books.map { it.id }

                            if(selectedItems.containsAll(allBooksIds)) {
                                selectedItems.clear()
                                selectionMode = false
                                return@IconButton
                            }

                            selectedItems.clear()
                            selectedItems.addAll(allBooksIds)
                        }
                    ) {
                        if(selectedItems.containsAll(books.map{
                                it.id
                            })) {
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

                    DeleteOption(viewModel, selectedItems) {
                        selectedItems.clear()
                        selectionMode = false
                    }

                    if(selectedItems.count() == 1) {
                        IconButton(
                            onClick = {
                                onEditBook(selectedItems[0])
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                        }

                        IconButton(
                            onClick = {
                                onBookDetails(selectedItems[0])
                            }
                        ) {
                            Icon(Icons.Default.Info, contentDescription = stringResource(R.string.info))
                        }
                    }
                }
            )
        },
        modifier = Modifier
            .fillMaxSize(),
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { suggestedPadding ->
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

        val padding = PaddingValues(
            top = suggestedPadding.calculateTopPadding() + 3.dp,
            start = 3.dp,
            end= 3.dp,
            bottom = 0.dp
        )

        Box {
            val image = ImageBitmap.imageResource(R.mipmap.readbear_bg)
            val color = MaterialTheme.colorScheme.surfaceVariant

            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawRect(
                    brush = ShaderBrush(
                        ImageShader(
                            image,
                            TileMode.Repeated,
                            TileMode.Repeated
                        )
                    ),
                    colorFilter = ColorFilter.tint(
                        color,
                        BlendMode.SrcIn
                    )
                )
            }

            BookLibrary(
                settings = settings,
                padding = padding,
                selectedItems = selectedItems,
                onAddSelectedItem = {
                    selectedItems.add(it)
                },
                onRemoveSelectedItem = {
                    selectedItems.remove(it)
                },
                selectionMode = selectionMode,
                onChangeSelectionMode = {
                    selectionMode = it
                },
                onOpenBook = {
                    onOpenBook(it)
                }
            )
        }
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
}