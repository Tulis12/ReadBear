package dev.tulis.readbear.routes.menu.library

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.tulis.readbear.R
import dev.tulis.readbear.db.Settings
import dev.tulis.readbear.db.entities.folders.bookFolder.BookFolder
import dev.tulis.readbear.db.entities.folders.bookFolder.BookFolderItem
import dev.tulis.readbear.utils.LongText

@Composable
fun FolderCover(
    item: BookFolderItem,
    folder: BookFolder,
    selectionState: SelectionState,
    settings: Settings.SettingsState,
    isHovered: Boolean,
    backgroundColor: Color,
    onOpenFolder: (BookFolder) -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (selectionState.selectedItems.contains(item)) 0.88f else 1f,
        animationSpec = tween(200),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (selectionState.selectedItems.contains(item)) 0.35f else 0f,
        animationSpec = tween(200),
        label = "glass"
    )

    Box(
        modifier = Modifier
            .background(Color.Gray.copy(alpha = alpha))
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        if(selectionState.selectionMode) {
                            if(selectionState.selectedItems.contains(item)) {
                                selectionState.removeSelectedItem(item)
                            } else {
                                selectionState.addSelectedItem(item)
                            }

                            if(selectionState.selectedItems.count() == 0) {
                                selectionState.selectionMode = false
                            }

                            return@combinedClickable
                        }

                        onOpenFolder(folder)
                    },
                    onLongClick = {
                        if(selectionState.selectionMode) return@combinedClickable

                        selectionState.selectionMode = true
                        selectionState.addSelectedItem(item)
                    },
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple()
                )
                .scale(scale)

        ) {
            Box {
//                AsyncImage( // TODO() cover (maybe book covers?)
//                    model = context.filesDir
//                        .resolve("books")
//                        .resolve(folder.path)
//                        .resolve(folder.cover),
//                    contentDescription = null,
//                    modifier = Modifier
//                        .aspectRatio(2f / 3f)
//                        .clip(RoundedCornerShape(10.dp))
//                        .fillMaxWidth(),
//                    contentScale = ContentScale.Crop
//                )

                Box(
                    modifier = Modifier
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(10.dp))
                        .fillMaxWidth()
                        .background(backgroundColor),
                )

                Crossfade(
                    targetState = isHovered,
                    label = "folderIcon",
                    modifier = Modifier.align(Alignment.Center)
                ) { hovered ->
                    Icon(
                        imageVector = if (hovered) Icons.Default.FolderOpen else Icons.Default.Folder,
                        contentDescription = stringResource(R.string.folder),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .size(50.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            Column {
                LongText(folder.name)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if(settings.progressEnabled) {
                        Text(
                            "", // TODO() maybe show progress of the whole folder? now just spacer
                            style = TextStyle(
                                fontSize = 12.sp
                            )
                        )
                    }

                    if(settings.timeClockEnabled) {
                        Text(
                            "", // TODO() reading time is much easier; now just spacer
                            style = TextStyle(
                                fontSize = 12.sp
                            )
                        )
                    }
                }

            }
        }
    }
}