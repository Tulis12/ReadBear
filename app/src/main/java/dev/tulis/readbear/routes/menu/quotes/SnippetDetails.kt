package dev.tulis.readbear.routes.menu.quotes

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import dev.tulis.readbear.R
import dev.tulis.readbear.routes.menu.MenuState
import dev.tulis.readbear.routes.menu.quotes.utils.QuoteViewModel
import dev.tulis.readbear.utils.clickableWithoutRipple
import dev.tulis.readbear.utils.normalizeName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnippetDetails(
    viewModel: QuoteViewModel = hiltViewModel(),
    menuState: MenuState,
    snippetId: Long,
    onPopBack: () -> Unit
) {
    val snippetFlow by viewModel.getSnippetById(snippetId).collectAsState(null)
    val snippet = snippetFlow ?: return

    val context = LocalContext.current
    val snippetsDir = context.filesDir.resolve("snippets")

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/png")
    ) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }

        val snippetImage = snippetsDir.resolve(snippet.path)
        val outputStream = context.contentResolver.openOutputStream(uri)

        outputStream?.use { out ->
            out.write(snippetImage.readBytes())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickableWithoutRipple {},
        contentAlignment = Alignment.Center
    ) {
        Column(
//            modifier = Modifier.padding(50.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AsyncImage(
                model = snippetsDir.resolve(snippet.path),
                contentDescription = snippet.description,
                modifier = Modifier
                    .padding(bottom = 5.dp)
                    .width(250.dp)
                    .clip(RoundedCornerShape(15.dp))
            )

            Text(
                snippet.name,
                modifier = Modifier.padding(top = 5.dp),
                fontSize = 30.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                "„${snippet.description}‟",
                modifier = Modifier.padding(start = 30.dp, end = 30.dp, bottom = 15.dp),
                textAlign = TextAlign.Justify,
                fontStyle = FontStyle.Italic
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
            ) {
                Button(onClick = {
                    launcher.launch(normalizeName(snippet.name))
                }) {
                    Text(stringResource(R.string.save_as_file))
                }

                Button(onClick = {
                    TODO()
                }) {
                    Text(stringResource(R.string.show_source))
                }
            }
        }
    }

    menuState.updateNavigation {
        IconButton(
            onClick = {
                onPopBack()
            }
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.go_back)
            )
        }
    }

    menuState.updateTitle(stringResource(R.string.snippet))
}