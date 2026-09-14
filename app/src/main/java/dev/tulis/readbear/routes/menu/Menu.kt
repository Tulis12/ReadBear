package dev.tulis.readbear.routes.menu

import android.system.ErrnoException
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.tulis.readbear.R
import dev.tulis.readbear.routes.menu.library.BookLibrary
import dev.tulis.readbear.routes.menu.library.BookLibraryMenu
import dev.tulis.readbear.routes.menu.library.actions.DeleteOption
import dev.tulis.readbear.routes.menu.library.actions.ImportOption
import dev.tulis.readbear.routes.menu.quotes.QuotesLibrary
import dev.tulis.readbear.settings.BottomSettingsSheet
import dev.tulis.readbear.utils.BackgroundPattern
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Menu(
    viewModel: LibraryViewModel = hiltViewModel(),
    onOpenBook: (Long) -> Unit,
    onEditBook: (Long) -> Unit,
    onBookDetails: (Long) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var actions: (@Composable () -> Unit)? by remember { mutableStateOf(null) }
    var route by remember { mutableStateOf(MenuRoute.BOOK_LIBRARY) }

    fun changeRoute(newRoute: MenuRoute) {
        scope.launch {
            drawerState.close()
            route = newRoute
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    NavigationRoute(
                        Icons.AutoMirrored.Filled.MenuBook,
                        name = stringResource(R.string.library),
                        route = route,
                        onChangeRoute = ::changeRoute,
                        targetRoute = MenuRoute.BOOK_LIBRARY,
                    )

                    NavigationRoute(
                        Icons.Default.FormatQuote,
                        name = stringResource(R.string.quotes),
                        route = route,
                        onChangeRoute = ::changeRoute,
                        targetRoute = MenuRoute.QUOTES,
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                val topBarDefaults = TopAppBarDefaults.topAppBarColors()

                Box {
                    Box(modifier = Modifier
                        .matchParentSize()
                        .background( // TODO() doesn't this look awkward?
                            topBarDefaults.containerColor
                        )) {
//                    BackgroundPattern(Modifier.matchParentSize(), color = MaterialTheme.colorScheme.surfaceVariant, rotation = -30f)
                    }

//                HorizontalDivider(modifier = Modifier.align(Alignment.BottomEnd), thickness = 3.dp)


                    TopAppBar (
                        colors = topBarDefaults.copy(
                            containerColor = Color.Transparent
                        ),
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch { drawerState.open() }
                            }) {
                                Icon(Icons.Default.Menu, stringResource(R.string.menu))
                            }
                        },
                        title = {
                            Row {
                                Text(stringResource(R.string.base_app_name))
                            }
                        },
                        actions = {
                            Crossfade(
                                targetState = actions,
                                label = "menu"
                            ) { actions ->
                                if(actions != null) {
                                    Row {
                                        actions()
                                    }
                                }
                            }
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize(),
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            }
        ) { suggestedPadding ->
            Crossfade(
                targetState = route,
                label = "menu"
            ) { targetRoute ->
                when (targetRoute) {
                    MenuRoute.BOOK_LIBRARY -> {
                        actions = BookLibraryMenu(
                            onOpenBook = onOpenBook,
                            onEditBook = onEditBook,
                            onBookDetails = onBookDetails,
                            suggestedPadding = suggestedPadding
                        )
                    }

                    MenuRoute.QUOTES -> {
                        actions = null
                        QuotesLibrary()
                    }
                }
            }
        }
    }
}

@Composable
fun NavigationRoute(
    imageVector: ImageVector,
    name: String,
    route: MenuRoute,
    onChangeRoute: (MenuRoute) -> Unit,
    targetRoute: MenuRoute
) {
    NavigationDrawerItem(
        label = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Icon(
                    imageVector,
                    name
                )

                Text(name)
            }
        },
        selected = route == targetRoute,
        onClick = {
            onChangeRoute(targetRoute)
        }
    )
}

enum class MenuRoute {
    BOOK_LIBRARY,
    QUOTES
}