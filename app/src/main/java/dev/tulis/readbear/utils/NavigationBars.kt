package dev.tulis.readbear.utils

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
fun NavigationBars(
    topBarVisible: Boolean
) {
    val view = LocalView.current

    DisposableEffect(topBarVisible) {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)

        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if(!topBarVisible) {
            controller.hide(WindowInsetsCompat.Type.navigationBars())
        } else {
            controller.show(WindowInsetsCompat.Type.navigationBars())
        }

        onDispose {
            controller.show(WindowInsetsCompat.Type.navigationBars())
        }
    }
}