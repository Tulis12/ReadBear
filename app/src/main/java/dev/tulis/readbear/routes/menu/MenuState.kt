package dev.tulis.readbear.routes.menu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class MenuState(
    private val startTitle: String
) {
    var title: String by mutableStateOf(startTitle)
    var actions: (@Composable () -> Unit)? by mutableStateOf(null)
    var navigation: (@Composable () -> Unit)? by mutableStateOf(null)
        private set

    fun updateTitle(title: String) {
        this.title = title
    }

    fun updateActions(actions: @Composable () -> Unit) {
        this.actions = actions
    }

    fun updateNavigation(navigation: @Composable () -> Unit) {
        this.navigation = navigation
    }

    fun resetTitle() {
        title = startTitle
    }

    fun resetNavigation() {
        navigation = null
    }

    fun resetActions() {
        actions = null
    }
}

@Composable
fun rememberMenuState(
    startTitle: String
): MenuState {
    return remember {
        MenuState(startTitle)
    }
}