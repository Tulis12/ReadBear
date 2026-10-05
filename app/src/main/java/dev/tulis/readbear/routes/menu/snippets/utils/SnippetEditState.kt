package dev.tulis.readbear.routes.menu.snippets.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class SnippetEditState(
    name: String,
    description: String?
) {
    var name: String by mutableStateOf(name)
    var description: String? by mutableStateOf(description)
    var editing: Boolean by mutableStateOf(false)
}

@Composable
fun rememberSnippetEditState(
    name: String,
    description: String?
): SnippetEditState {
    return remember {
        SnippetEditState(name, description)
    }
}