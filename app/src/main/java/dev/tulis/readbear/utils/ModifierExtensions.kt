package dev.tulis.readbear.utils

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.text.style.TextOverflow
import dev.tulis.readbear.routes.menu.TooLongTextOption

fun Modifier.clickableWithRipple(
    onClick: () -> Unit
): Modifier {
    return this.composed {
        this.clickable(
            enabled = true,
            onClickLabel = null,
            role = null,
            onClick = onClick,
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple()
        )
    }
}