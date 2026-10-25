package dev.tulis.readbear.utils

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import dev.tulis.readbear.db.Settings
import dev.tulis.readbear.settings.TooLongTextOption
import java.text.Normalizer
import java.util.Locale

@Composable
fun LongText(
    text: String,
    modifier: Modifier = Modifier,
    tooLongTextOption: TooLongTextOption? = null,
    color: Color = Color.Unspecified,
    autoSize: TextAutoSize? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    softWrap: Boolean = true,
    maxLines: Int = 1,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current
) {
    var longTextOption = tooLongTextOption

    if(tooLongTextOption == null) {
        val flowLongTextOption by Settings.getTooLongTextOption(LocalContext.current).collectAsState(null)
        longTextOption = flowLongTextOption ?: return
    }

    Text(
        text = text,
        modifier = modifier
            .then(
                if(longTextOption == TooLongTextOption.BASIC_MARQUEE) {
                    Modifier.basicMarquee()
                } else {
                    Modifier
                }
            ),
        color = color,
        autoSize = autoSize,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = if (longTextOption == TooLongTextOption.ELLIPSIS) {
            TextOverflow.Ellipsis
        } else {
            TextOverflow.Visible
        },
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style
    )
}

fun shortTitle(title: String): String {
    return title.substringBefore(":").substringBefore("(").trim()
}

fun cutText(text: String, max: Int): String  {
    return if (text.length <= max) {
        text
    } else {
        text.take(max).substringBeforeLast(' ').trimEnd(',', ';', ':', ' ') + "…"
    }
}

fun normalizeName(name: String): String {
    return Normalizer
        .normalize(name, Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")
        .replace("[^a-zA-Z0-9]".toRegex(), "_")
        .lowercase(Locale.ROOT)
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    minimumGap: Dp = 20.dp
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.width(minimumGap))

        LongText(
            text = value,
            fontWeight = FontWeight.Medium
        )
    }
}