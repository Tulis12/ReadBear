package dev.tulis.readbear.utils

import android.graphics.Matrix
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.res.imageResource
import dev.tulis.readbear.R

@Composable
fun BackgroundPattern(modifier: Modifier, color: Color = MaterialTheme.colorScheme.surfaceVariant, rotation: Float = 0f) {
    val image = ImageBitmap.imageResource(R.drawable.readbear_bg)

    Canvas(
        modifier = modifier
    ) {
        val shader = ImageShader(
            image,
            TileMode.Repeated,
            TileMode.Repeated
        )

        val matrix = Matrix().apply {
            setRotate(rotation)
        }

        shader.setLocalMatrix(matrix)

        drawRect(
            brush = ShaderBrush(shader),
            colorFilter = ColorFilter.tint(
                color,
                BlendMode.SrcIn
            )
        )
    }
}