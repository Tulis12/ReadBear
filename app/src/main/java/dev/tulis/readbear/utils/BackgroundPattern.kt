package dev.tulis.readbear.utils

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.res.imageResource
import dev.tulis.readbear.R

@Composable
fun BackgroundPattern(modifier: Modifier) {
    val image = ImageBitmap.imageResource(R.drawable.readbear_bg)
    val color = MaterialTheme.colorScheme.surfaceVariant

    Canvas(
        modifier = modifier
    ) {
        drawRect(
            brush = ShaderBrush(
                ImageShader(
                    image,
                    TileMode.Repeated,
                    TileMode.Repeated
                )
            ),
            colorFilter = ColorFilter.tint(
                color,
                BlendMode.SrcIn
            )
        )
    }
}