package dev.tulis.readbear.routes.menu

import androidx.compose.animation.Crossfade
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhotoSizeSelectLarge
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
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
import dev.tulis.readbear.routes.menu.library.bookLibraryMenu
import dev.tulis.readbear.routes.menu.quotes.QuotesLibrary
import dev.tulis.readbear.routes.menu.quotes.SnippetsLibrary
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

    @Composable
    fun NavigationRoute(
        imageVector: ImageVector,
        name: String,
        route: MenuRoute,
        onChangeRoute: (MenuRoute) -> Unit,
        targetRoute: MenuRoute,
        disabled: Boolean = false
    ) {
        val disabledMessage = stringResource(R.string.this_is_currently_disabled)

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
                if(!disabled) {
                    onChangeRoute(targetRoute)
                } else {
                    scope.launch {
                        drawerState.close()
                        snackbarHostState.showSnackbar(disabledMessage)
                    }
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    NavigationRoute(
                        Icons.AutoMirrored.Filled.MenuBook,
                        name = stringResource(R.string.library),
                        route = route,
                        onChangeRoute = ::changeRoute,
                        targetRoute = MenuRoute.BOOK_LIBRARY,
                    )

//                    NavigationRoute(
//                        Icons.Default.FormatQuote,
//                        name = stringResource(R.string.quotes),
//                        route = route,
//                        onChangeRoute = ::changeRoute,
//                        targetRoute = MenuRoute.QUOTES,
//                        disabled = true
//                    )

                    NavigationRoute(
                        Icons.Default.PhotoSizeSelectLarge,
                        name = stringResource(R.string.snippets),
                        route = route,
                        onChangeRoute = ::changeRoute,
                        targetRoute = MenuRoute.SNIPPETS,
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                Box {
                    TopAppBar (
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
                val padding = PaddingValues(
                    top = suggestedPadding.calculateTopPadding() + 3.dp,
                    start = 3.dp,
                    end= 3.dp,
                    bottom = 0.dp
                )

                when (targetRoute) {
                    MenuRoute.BOOK_LIBRARY -> {
                        actions = bookLibraryMenu(
                            onOpenBook = onOpenBook,
                            onEditBook = onEditBook,
                            onBookDetails = onBookDetails,
                            padding = padding
                        )
                    }

                    MenuRoute.QUOTES -> {
                        actions = null
                        QuotesLibrary(padding)
                    }

                    MenuRoute.SNIPPETS -> {
                        actions = null
                        SnippetsLibrary(padding = padding)
                    }
                }
            }
        }
    }
}

enum class MenuRoute {
    BOOK_LIBRARY,
    QUOTES,
    SNIPPETS
}