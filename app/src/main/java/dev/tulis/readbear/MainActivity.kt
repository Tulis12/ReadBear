package dev.tulis.readbear

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.compose.rememberNavController
import dev.tulis.readbear.ui.theme.ReadBearTheme
import dev.tulis.readbear.ui.theme.ThemeType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            enableEdgeToEdge()

            ReadBearTheme(
                themeType = ThemeType.SYSTEM
            ) {
                App(navController = navController)
            }
        }
    }
}