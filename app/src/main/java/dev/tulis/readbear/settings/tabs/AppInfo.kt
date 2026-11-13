package dev.tulis.readbear.settings.tabs

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import dev.tulis.readbear.BuildConfig
import dev.tulis.readbear.R
import dev.tulis.readbear.utils.InfoRow
import dev.tulis.readbear.utils.LongText
import org.json.JSONArray
import java.util.Locale

@Composable
fun AppInfo() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(15.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
            modifier = Modifier.padding(5.dp)
        ) {
            AsyncImage(
                model = R.mipmap.ic_launcher,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(0.3f)
            )

            Text(stringResource(R.string.base_app_name), modifier = Modifier.padding(top = 5.dp), fontSize = 20.sp)

            Text(
                BuildConfig.VERSION_NAME,
                modifier = Modifier.padding(0.dp),
            )

            Text(
                BuildConfig.DISTRIBUTOR,
                fontSize = 13.sp,
                color = Color.Gray,
                lineHeight = 8.sp
            )
        }

        VerticalDivider(
            modifier = Modifier.fillMaxHeight(0.25f)
        )

        val locale = LocalConfiguration.current.locales[0]
        val context = LocalContext.current

        val languageName = locale.getDisplayLanguage(Locale.ENGLISH)
        val translators = remember { mutableStateListOf<String>() }

        LaunchedEffect(Unit) {
            val jsonString =
                context.resources
                    .openRawResource(R.raw.translation_credits)
                    .bufferedReader()
                    .use { it.readText() }

            val jsonArray = JSONArray(jsonString)

            val usernames = mutableListOf<String>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)

                if (obj.has(languageName)) {
                    val users = obj.getJSONArray(languageName)

                    for (j in 0 until users.length()) {
                        usernames += users.getJSONObject(j).getString("username")
                    }

                    break
                }
            }

            translators.clear()
            translators.addAll(usernames)
        }



        Column {
            InfoRow(stringResource(R.string.author), "Tulis")
            InfoRow(stringResource(R.string.current_language), locale.getDisplayLanguage(locale))

            if(!translators.isEmpty()) {
                val translatorsText = translators.joinToString(", ")

                if(translators.count() == 1) {
                    InfoRow(stringResource(R.string.translator), translators.first())
                } else {
                    InfoRow(stringResource(R.string.translators), translatorsText)
                }
            }

            HorizontalDivider()

            Text(stringResource(R.string.thanks_for_using), autoSize = TextAutoSize.StepBased(
                minFontSize = 8.sp
            ), maxLines = 1)

            AboutLinks()
        }
    }
}


@Composable
fun AboutLinks() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AboutLink(
            Icons.Default.Code,
            "GitHub",
            AboutLinkAction.Url("https://github.com/Tulis12/ReadBear")
        )

        AboutLink(
            Icons.Default.Language,
            stringResource(R.string.website),
            AboutLinkAction.Url("https://readbear.tulis.dev/")
        )

        AboutLink(
            Icons.Default.BugReport,
            stringResource(R.string.issues),
            AboutLinkAction.Url("https://readbear.tulis.dev/issues")
        )

//        AboutLink(Icons.Default.VolunteerActivism, "Credits", AboutLinkAction.Click {
//
//        })
    }
}

sealed interface AboutLinkAction {
    data class Url(val url: String) : AboutLinkAction
    data class Click(val onClick: () -> Unit) : AboutLinkAction
}

@Composable
fun AboutLink(
    icon: ImageVector,
    title: String,
    action: AboutLinkAction
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = when(action) {
                is AboutLinkAction.Click -> { {
                    action.onClick()
                } }

                is AboutLinkAction.Url -> { {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        action.url.toUri()
                    )

                    context.startActivity(intent)
                } }
            })
            .padding(horizontal = 5.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(25.dp)
                )
            }
        }

        LongText(
            text = title,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
