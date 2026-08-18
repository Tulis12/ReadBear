package dev.tulis.tulireader.routes.menu

import android.app.Activity
import android.system.ErrnoException
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment

//import androidx.compose.animation.animateFloatAsState

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import dev.tulis.tulireader.db.Settings
import dev.tulis.tulireader.db.Settings.dataStore
import dev.tulis.tulireader.routes.menu.actions.DeleteOption
import dev.tulis.tulireader.routes.menu.actions.ImportOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun Menu(
    viewModel: LibraryViewModel = hiltViewModel(),
    onOpenBook: (Long) -> Unit,
    onEditBook: (Long) -> Unit
) {
    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val context = LocalContext.current;
    val scope = rememberCoroutineScope()

    val books by viewModel.books.collectAsState()

    val selectedItems = remember {
        mutableStateListOf<Long>()
    }

    var selectionMode by remember { mutableStateOf(false) }
    val columnCount by Settings.getColumns(context).collectAsState(null)
    val longTextOption by Settings.getTooLongTextOption(context).collectAsState(null)
    val alreadyReadOption by Settings.getAlreadyReadOption(context).collectAsState(null)

    var importing by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    Scaffold(
        topBar = {
            TopAppBar (
                title = {
                    Row {
                        Text("Tutaj coś musi być ale nwm co")
                    }
                },
                actions = {
                    if(!selectionMode) {

                        IconButton(
                            onClick = {
                                scope.launch {
                                    showSheet = true
                                    sheetState.show()
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                        }

                        ImportOption(
                            viewModel,
                            onChangeImporting = {
                                importing = it
                            },
                            onThrow = { throwable ->

                                val message = if (throwable is ErrnoException) {
                                    "Zabrakło miejsca na urządzeniu!"
                                } else {
                                    "Nie można było skopiować pliku!"
                                }

                                scope.launch {
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
                                contentDescription = "Deselect"
                            )
                        } else {
                            Icon(
                                Icons.Default.SelectAll,
                                contentDescription = "Select all"
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
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
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
        val columnCountSaved = columnCount ?: return@Scaffold
        var sliderColumnValue by remember { mutableIntStateOf(columnCountSaved) }

        val longTextOptionSaved = longTextOption ?: return@Scaffold
        var longTextOptionValue by remember { mutableStateOf(longTextOptionSaved) }

        val alreadyReadOptionSaved = alreadyReadOption ?: return@Scaffold
        var alreadyReadOptionValue by remember { mutableStateOf(alreadyReadOptionSaved) }

        if(showSheet) {
            BottomSettingsSheet(
                sheetState = sheetState,
                columnCount = columnCountSaved,
                onChangeColumnCount = { value ->
                    sliderColumnValue = value
                },
                tooLongTextOption = longTextOptionSaved,
                onChangeTooLongTextOption = { value ->
                    longTextOptionValue = value
                },
                alreadyReadOption = alreadyReadOptionSaved,
                onChangeAlreadyReadOption = { value ->
                    alreadyReadOptionValue = value
                },
                onSaveRequest = {
                    scope.launch {
                        context.dataStore.edit { settings ->
                            settings[Settings.SettingsKeys.COLUMNS] = sliderColumnValue
                            settings[Settings.SettingsKeys.LONG_TEXT_OPTION] = longTextOptionValue.name
                            settings[Settings.SettingsKeys.ALREADY_READ_OPTION] = alreadyReadOptionValue.name
                        }
                    }
                },
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
            BookLibrary(
                sliderColumnValue = sliderColumnValue,
                tooLongTextOption = longTextOptionValue,
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

            if(importing) {
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
                    Text("Importing...")
                }
            }
        }
    }
}