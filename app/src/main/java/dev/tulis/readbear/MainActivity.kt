package dev.tulis.readbear

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import dev.tulis.readbear.ui.theme.ReadBearTheme
import dev.tulis.readbear.ui.theme.ThemeType
import dagger.hilt.android.AndroidEntryPoint
import dev.tulis.readbear.db.Settings

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val themeMode = Settings.getThemeMode(LocalContext.current).collectAsState(null).value

            val navController = rememberNavController()
            enableEdgeToEdge()

            if(themeMode == null) {
                CircularProgressIndicator()
                return@setContent
            }

            ReadBearTheme(
                themeType = themeMode
            ) {
                App(navController = navController)
            }
        }
    }
}