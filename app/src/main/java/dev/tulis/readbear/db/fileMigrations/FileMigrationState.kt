package dev.tulis.readbear.db.fileMigrations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class FileMigrationState {
    var stageCurrent: Int by mutableIntStateOf(0)
    var stageMax: Int? by mutableStateOf(null)

    var message: String? by mutableStateOf(null)
    var progress: Float by mutableFloatStateOf(0f)
}

@Composable
fun rememberFileMigrationState(): FileMigrationState {
    return remember {
        FileMigrationState()
    }
}