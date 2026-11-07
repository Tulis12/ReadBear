package dev.tulis.readbear.utils.extensions

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import net.engawapg.lib.zoomable.ZoomState
import net.engawapg.lib.zoomable.zoomable

fun Modifier.zoomableWithTap(
    zoomState: ZoomState,
    enableOneFingerZoom: Boolean = false,
    onTap: () -> Unit
): Modifier {
    return this
        .zoomable(
            zoomState = zoomState,
            enableOneFingerZoom = enableOneFingerZoom
        )
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    onTap()
                }
            )
        }
}