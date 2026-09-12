package dev.tulis.readbear.settings.tabs

import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import dev.tulis.readbear.ui.theme.darkScheme
import dev.tulis.readbear.ui.theme.AppTypography
import dev.tulis.readbear.R
import dev.tulis.readbear.ui.theme.ThemeType
import dev.tulis.readbear.ui.theme.catppuccin.FrappeColorScheme
import dev.tulis.readbear.ui.theme.catppuccin.LightColorScheme
import dev.tulis.readbear.ui.theme.catppuccin.MacchiatoColorScheme
import dev.tulis.readbear.ui.theme.catppuccin.MochaColorScheme
import dev.tulis.readbear.ui.theme.lightScheme
import dev.tulis.readbear.utils.BackgroundPattern
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Appearance(
    themeMode: ThemeType,
    onChangeThemeMode: (ThemeType) -> Unit,
    theme: String,
    onChangeTheme: (String) -> Unit
) {
    val languages = mapOf(
        Pair("en", "English"),
        Pair("pl", "polski"),
        Pair("ru", "Русский")
    )

    val locale = LocalConfiguration.current.locales[0]

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var languageSelected by remember { mutableStateOf(
                locale.language
            ) }

            var expanded by remember { mutableStateOf(false) }

            Text(stringResource(R.string.language))

            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                ) {
                    Text(languages[languageSelected] ?: stringResource(R.string.unknown))
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
                                languageSelected = locale.key
                                expanded = false

                                val appLocale = LocaleListCompat.forLanguageTags(locale.key)
                                AppCompatDelegate.setApplicationLocales(appLocale)
                            }
                        )
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var expanded by remember { mutableStateOf(false) }

            Text(stringResource(R.string.theme))

            val themeModes = arrayOf(
                ThemeMode(stringResource(R.string.dark), ThemeType.DARK),
                ThemeMode(stringResource(R.string.light), ThemeType.LIGHT),
                ThemeMode(stringResource(R.string.system), ThemeType.SYSTEM)
            )

            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                ) {
                    Text(
                        themeModes.first {
                            it.themeType == themeMode
                        }.modeName
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    themeModes.forEach { themeMode ->
                        DropdownMenuItem(
                            text = {
                                Text(themeMode.modeName)
                            },
                            onClick = {
                                onChangeThemeMode(themeMode.themeType)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        val items = arrayOf(
            ReadBearTheme("Bear", ReadBearTheme.ThemeSchemes(darkScheme, lightScheme)),
            ReadBearTheme("Catppuccin", ReadBearTheme.ThemeSchemes(
                MacchiatoColorScheme,
                LightColorScheme
            ))
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                val themeMode = when (themeMode) {
                    ThemeType.DARK -> item.themeSchemes.darkScheme
                    ThemeType.LIGHT -> item.themeSchemes.lightScheme
                    ThemeType.SYSTEM -> if(isSystemInDarkTheme()) {
                        item.themeSchemes.darkScheme
                    } else item.themeSchemes.lightScheme
                }

                MaterialTheme(
                    colorScheme = themeMode,
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

                            RadioButton(
                                true,
                                {},
                                modifier = Modifier.align(Alignment.TopEnd)
                            )

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

data class ThemeMode(
    val modeName: String,
    val themeType: ThemeType
)

data class ReadBearTheme(
    var themeName: String,
    var themeSchemes: ThemeSchemes
) {
    data class ThemeSchemes(
        val darkScheme: ColorScheme,
        val lightScheme: ColorScheme
    )
}

data class AppLanguage(
    val locale: Locale,
    val name: String
)