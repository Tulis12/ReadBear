package dev.tulis.readbear.settings.tabs

import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import coil3.compose.AsyncImage
import dev.tulis.readbear.ui.theme.darkScheme
import dev.tulis.readbear.ui.theme.AppTypography
import dev.tulis.readbear.R
import dev.tulis.readbear.ui.theme.lightScheme
import dev.tulis.readbear.utils.BackgroundPattern
import dev.tulis.readbear.utils.sampleImages
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Appearance() {
    val languages = mapOf(
        Pair("en", "English"),
        Pair("pl", "polski"),
        Pair("ru", "Русский")
    )

    val locale = LocalConfiguration.current.locales[0]
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(
        locale.language
    ) }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
//        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.language))

            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                ) {
                    Text(languages[selected] ?: stringResource(R.string.unknown))
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    languages.forEach { locale ->
                        DropdownMenuItem(
                            text = {
                                Text(locale.value)
                            },
                            onClick = {
                                selected = locale.key
                                expanded = false

                                val appLocale = LocaleListCompat.forLanguageTags(locale.key)
                                AppCompatDelegate.setApplicationLocales(appLocale)
                            }
                        )
                    }
                }
            }
        }

        val items = arrayOf(
            ReadBearTheme("Default Dark", darkScheme),
            ReadBearTheme("Default Light", lightScheme)
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->

                MaterialTheme(
                    colorScheme = item.colorScheme,
                    typography = AppTypography,
                    content = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .fillMaxSize()
                                .border(
                                    width = 5.dp,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(10.dp)
                                )
                        ) {
                            BackgroundPattern(modifier = Modifier.matchParentSize())

                            Column(
                                modifier = Modifier.padding(15.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                val scope = rememberCoroutineScope()
                                val context = LocalContext.current
                                val toastMessage = stringResource(R.string.sample_button_pressed)

                                Text(item.themeName, color = MaterialTheme.colorScheme.onBackground)
                                val tooltipState = rememberTooltipState(isPersistent = true)

                                TooltipBox(
                                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                        TooltipAnchorPosition.Above,
                                        10.dp
                                    ),
                                    tooltip = {
                                        RichTooltip(
                                            title = {
                                                Text(stringResource(R.string.sample_tooltip))
                                            },
                                            action = {
                                                Button(onClick = {
                                                    Toast.makeText(
                                                        context,
                                                        toastMessage,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }) {
                                                    Text(stringResource(R.string.sample_button))
                                                }
                                            }
                                        ) {
                                            var checked by remember { mutableStateOf(false) }

                                            Column {
                                                Text(stringResource(R.string.example_tooltip), textAlign = TextAlign.Justify)

                                                Row(
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(checked, onCheckedChange = {
                                                        checked = it

                                                        Toast.makeText(
                                                            context,
                                                            toastMessage,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    })

                                                    Text(stringResource(R.string.sample_checkbox))
                                                }

                                                Row(
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    IconButton(onClick = {
                                                        Toast.makeText(
                                                            context,
                                                            toastMessage,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }) {
                                                        Icon(Icons.Default.Settings, stringResource(R.string.sample_settings))
                                                    }

                                                    Text(stringResource(R.string.sample_settings))
                                                }
                                            }
                                        }
                                    },
                                    state = tooltipState
                                ) {
                                    Button(onClick = {
                                        scope.launch {
                                            tooltipState.show()
                                        }
                                    }) {
                                        Text(stringResource(R.string.show_more))
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }

}

data class ReadBearTheme(
    var themeName: String,
    var colorScheme: ColorScheme
)

data class AppLanguage(
    val locale: Locale,
    val name: String
)