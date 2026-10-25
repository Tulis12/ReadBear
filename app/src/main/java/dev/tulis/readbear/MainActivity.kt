package dev.tulis.readbear

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import dev.tulis.readbear.db.FileMigrations
import dev.tulis.readbear.db.Settings
import dev.tulis.readbear.db.fileMigrations.FileMigration
import dev.tulis.readbear.db.fileMigrations.rememberFileMigrationState
import dev.tulis.readbear.settings.tabs.ReadBearTheme
import dev.tulis.readbear.ui.theme.ReadBearMaterialTheme
import dev.tulis.readbear.ui.theme.customThemes.catppuccin.Catppuccin
import dev.tulis.readbear.ui.theme.customThemes.forest.ForestTheme
import dev.tulis.readbear.ui.theme.customThemes.pink.PinkTheme
import dev.tulis.readbear.ui.theme.customThemes.river.RiverTheme
import dev.tulis.readbear.ui.theme.customThemes.roseWood.RoseWoodTheme
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
    private val viewModel: AppViewModel by viewModels()
    private val migrations: FileMigrations by viewModels()
    var isReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            !isReady
        }

        super.onCreate(savedInstanceState)
        handleIncomingIntent(intent)

        setContent {
            val context = LocalContext.current
            val settings = Settings.getSettings(context).collectAsState(null).value ?: return@setContent

            val readBearTheme = themes.firstOrNull {
                it.id == settings.theme
            } ?: themes[0]

            ReadBearMaterialTheme(
                readBearTheme = readBearTheme,
                themeType = settings.themeType
            ) {
                if(migrations.hasMigrations()) {
                    val migrationState = rememberFileMigrationState()
                    val unsafeMigrations = remember { mutableStateListOf<FileMigration>() }
                    val allowMigrateUnsafely = remember { mutableStateListOf<FileMigration>() }
                    var safeMigration: Boolean by remember { mutableStateOf(true) }
                    var startedMigration: Boolean by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        isReady = true

                        if(migrations.isSafeToMigrateAll()) {
                            migrations.migrate(migrationState)
                        } else {
                            val unsafeCheckedMigrations = migrations.getUnsafeMigrations()
                            migrationState.stageMax = unsafeCheckedMigrations.count()
                            safeMigration = false
                            unsafeMigrations.addAll(unsafeCheckedMigrations)
                        }
                    }

                    LaunchedEffect(allowMigrateUnsafely.count()) {
                        if(unsafeMigrations.isEmpty() && !allowMigrateUnsafely.isEmpty() && !startedMigration) {
                            startedMigration = true
                            migrations.migrateAllUnsafe(migrationState, allowMigrateUnsafely)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 400.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = stringResource(R.string.updating),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = stringResource(R.string.migration_x_from_y, migrationState.stageCurrent, migrationState.stageMax ?: migrationState.stageCurrent),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = migrationState.message
                                            ?: stringResource(R.string.hang_on),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )

                                    Text(
                                        text = "${(migrationState.progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                LinearProgressIndicator(
                                    progress = { migrationState.progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(50.dp)),
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                if(!safeMigration) {
                                    Text(
                                        stringResource(R.string.unsafe_migration_dont_close_the_app),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    if(!unsafeMigrations.isEmpty()) {
                        val unsafeMigration = unsafeMigrations.first()

                        AlertDialog(
                            onDismissRequest = {},
                            title = {
                                Text(stringResource(R.string.unsafe_migration))
                            },
                            text = {
                                Text(
                                    unsafeMigration.checkSafeMigration().unsafeMirateMessage,
                                    textAlign = TextAlign.Justify
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        unsafeMigrations.remove(unsafeMigration)
                                        allowMigrateUnsafely.add(unsafeMigration)
                                    }
                                ) {
                                    Text(stringResource(R.string.migrate_unsafely))
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        startActivity(Intent(android.provider.Settings.ACTION_INTERNAL_STORAGE_SETTINGS))
                                    }
                                ) {
                                    Text(stringResource(R.string.free_up_space))
                                }
                            }
                        )
                    }

                    return@ReadBearMaterialTheme
                }

                App(
                    viewModel = viewModel,
                    onReady = {
                        isReady = true
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_VIEW) return

        val uri = intent.data ?: return
        viewModel.requestImport(uri)
        println("jest ready?$isReady")

        println("requested import")
    }
}

