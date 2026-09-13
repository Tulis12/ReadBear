package dev.tulis.readbear

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.tulis.readbear.db.Settings
import dev.tulis.readbear.settings.tabs.ReadBearTheme
import dev.tulis.readbear.ui.theme.ReadBearMaterialTheme
import dev.tulis.readbear.ui.theme.customThemes.roseWood.RoseWoodTheme
import dev.tulis.readbear.ui.theme.customThemes.catppuccin.Catppuccin
import dev.tulis.readbear.ui.theme.customThemes.forest.ForestTheme
import dev.tulis.readbear.ui.theme.customThemes.river.RiverTheme
import dev.tulis.readbear.ui.theme.customThemes.pink.PinkTheme
import dev.tulis.readbear.ui.theme.customThemes.sunflower.SunflowerTheme
import dev.tulis.readbear.ui.theme.customThemes.teal.TealTheme
import dev.tulis.readbear.ui.theme.darkScheme
import dev.tulis.readbear.ui.theme.lightScheme


val themes = arrayOf(
    ReadBearTheme(
        "Bear", ReadBearTheme.ThemeSchemes(
            darkScheme,
            lightScheme
        )
    ),
    ReadBearTheme("Sunflower", ReadBearTheme.ThemeSchemes(
        SunflowerTheme.darkScheme,
        SunflowerTheme.lightScheme
    )),
    ReadBearTheme("Rosewood", ReadBearTheme.ThemeSchemes(
        RoseWoodTheme.darkScheme,
        RoseWoodTheme.lightScheme
    )),
    ReadBearTheme("Pink", ReadBearTheme.ThemeSchemes(
        PinkTheme.darkScheme,
        PinkTheme.lightScheme
    )),
    ReadBearTheme("Forest", ReadBearTheme.ThemeSchemes(
        ForestTheme.darkScheme,
        ForestTheme.lightScheme
    )),
    ReadBearTheme("Teal", ReadBearTheme.ThemeSchemes(
        TealTheme.darkScheme,
        TealTheme.lightScheme
    )),
    ReadBearTheme("River", ReadBearTheme.ThemeSchemes(
        RiverTheme.darkScheme,
        RiverTheme.lightScheme
    )),
    ReadBearTheme("Catppuccin", ReadBearTheme.ThemeSchemes(
        Catppuccin.macchiatoColorScheme,
        Catppuccin.lightColorScheme
    ))
)

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val settings = Settings.getSettings(context).collectAsState(null).value

            val navController = rememberNavController()
            enableEdgeToEdge()

            if(settings == null) {
                CircularProgressIndicator()
                return@setContent
            }

            var readBearTheme = themes.firstOrNull {
                it.id == settings.theme
            }

            if(readBearTheme == null) {
                readBearTheme = themes[0]
            }

            ReadBearMaterialTheme(
                readBearTheme = readBearTheme,
                themeType = settings.themeType
            ) {
                App(navController = navController)
            }
        }
    }
}