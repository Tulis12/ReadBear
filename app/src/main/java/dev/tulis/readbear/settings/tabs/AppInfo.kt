package dev.tulis.readbear.settings.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import dev.tulis.readbear.BuildConfig
import dev.tulis.readbear.R
import dev.tulis.readbear.routes.info.InfoRow
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
            Text(BuildConfig.VERSION_NAME)
        }

        VerticalDivider(
            modifier = Modifier.fillMaxHeight(0.25f)
        )

        val locale = LocalConfiguration.current.locales[0]
        val context = LocalContext.current

        val languageName = locale.getDisplayLanguage(Locale.ENGLISH)
        var translators by remember { mutableStateOf(mutableListOf<String>()) }

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

            translators = usernames
            println(translators)
        }



        Column {
            InfoRow(stringResource(R.string.author), "Tulis")
            InfoRow(stringResource(R.string.current_language), locale.getDisplayLanguage(locale))

            if(!translators.isEmpty()) {
                var translatorsText = ""
                var i = 0

                translators.forEach {
                    translatorsText += it
                    if(i != translators.count() - 1) translatorsText += ","
                    i++
                }

                if(translators.count() == 1) {
                    InfoRow(stringResource(R.string.translator), translators.first())
                } else {
                    Text(stringResource(R.string.translators))
                    Text(translatorsText, fontWeight = FontWeight.Medium)
                }
            }

            HorizontalDivider()

            Text(stringResource(R.string.thanks_for_using), autoSize = TextAutoSize.StepBased(), maxLines = 1)
            Text(text = buildAnnotatedString {
                withLink(
                    LinkAnnotation.Url("https://github.com/Tulis12/ReadBear")
                ) {
                    append("Visit the project on GitHub")
                }
            })


        }
    }
}