package dev.tulis.readbear.routes.menu

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.tulis.readbear.R
import dev.tulis.readbear.routes.menu.library.BookLibraryMenu
import dev.tulis.readbear.routes.menu.quotes.SnippetsLibrary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Menu(
    onOpenBook: (Long) -> Unit,
    onEditBook: (Long) -> Unit,
    onBookDetails: (Long) -> Unit,
    onReady: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val menuState = rememberMenuState(stringResource(R.string.base_app_name))
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
                    changeRoute(targetRoute)
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
                        targetRoute = MenuRoute.BOOK_LIBRARY,
                    )

                    NavigationRoute(
                        Icons.Default.PhotoSizeSelectLarge,
                        name = stringResource(R.string.snippets),
                        targetRoute = MenuRoute.SNIPPETS,
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar (
                    navigationIcon = {
                        Crossfade(
                            targetState = menuState.navigation,
                            label = "navigation"
                        ) { navigation ->
                            navigation?.invoke() ?: run {
                                IconButton(onClick = {
                                    scope.launch { drawerState.open() }
                                }) {
                                    Icon(Icons.Default.Menu, stringResource(R.string.menu))
                                }
                            }
                        }
                    },
                    title = {
                        Crossfade(
                            targetState = menuState.title,
                            label = "title"
                        ) { title ->
                            Text(title)
                        }
                    },
                    actions = {
                        Crossfade(
                            targetState = menuState.actions,
                            label = "menu"
                        ) { actions ->
                            actions?.let {
                                Row {
                                    it()
                                }
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
                        menuState.resetNavigation()

                        BookLibraryMenu(
                            menuState = menuState,
                            onOpenBook = onOpenBook,
                            onEditBook = onEditBook,
                            onBookDetails = onBookDetails,
                            padding = padding,
                            onReady = {
                                onReady()
                            }
                        )
                    }

                    MenuRoute.SNIPPETS -> {
                        SnippetsLibrary(
                            menuState = menuState,
                            padding = padding
                        )
                    }
                }
            }
        }
    }
}

enum class MenuRoute {
    BOOK_LIBRARY,
    SNIPPETS
}