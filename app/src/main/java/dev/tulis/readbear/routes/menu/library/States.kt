package dev.tulis.readbear.routes.menu.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class ReorderState {
    var enabled: Boolean by mutableStateOf(false)
}

@Composable
fun rememberReorderState(): ReorderState {
    return remember {
        ReorderState()
    }
}