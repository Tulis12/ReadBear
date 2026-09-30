package dev.tulis.readbear.routes.menu.quotes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoSizeSelectLarge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import dev.tulis.readbear.R
import dev.tulis.readbear.routes.menu.MenuState
import dev.tulis.readbear.routes.menu.quotes.utils.QuoteViewModel
import dev.tulis.readbear.utils.BackgroundPattern
import dev.tulis.readbear.utils.clickableWithoutRipple
import dev.tulis.readbear.utils.cutText
import dev.tulis.readbear.utils.shortTitle

@Composable
fun SnippetsLibrary(
    viewModel: QuoteViewModel = hiltViewModel(),
    menuState: MenuState,
    padding: PaddingValues
) {
    val snippets = viewModel.getSnippets().collectAsState(ArrayList()).value

    val snippetsDir = LocalContext.current.filesDir.resolve("snippets")
    snippetsDir.mkdirs()

    var clickedSnipped: Long? by remember { mutableStateOf(null) }

    Box(
        modifier = Modifier.padding(padding).fillMaxSize()
    ) {
        BackgroundPattern(modifier = Modifier.matchParentSize())

        if(snippets.count() == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(15.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoSizeSelectLarge,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.there_is_nothing_here),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = stringResource(R.string.add_snippet_during_reading),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2)
        ) {
            items(snippets.count()) { snippetIndex ->
                val snippet = snippets[snippetIndex].snippet
                val book = snippets[snippetIndex].book

                val title = cutText(shortTitle(book.title), 40)

                val attrib = buildString {
                    append("„").append(title).append("” ")
                    append(stringResource(R.string.page_attrib, snippet.progress + 1))
                }

                Card(
                    modifier = Modifier
                        .padding(5.dp).fillMaxWidth().heightIn(min = 50.dp)
                        .clickable {
                            clickedSnipped = snippet.id
                        }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(15.dp)
                    ) {
                        AsyncImage(
                            model = snippetsDir
                                .resolve(snippet.path),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 5.dp)
                                .clip(RoundedCornerShape(15.dp))
                        )

                        Text(snippet.name, fontSize = 20.sp, modifier = Modifier.padding(top = 5.dp))
                        snippet.description?.let { Text(it, fontStyle = FontStyle.Italic) }
                        Text("— $attrib", modifier = Modifier.align(Alignment.End))
                    }
                }
            }
        }

        AnimatedVisibility(
            clickedSnipped != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val clickedSnippetSaved = remember { clickedSnipped }

            clickedSnippetSaved?.let {
                SnippetDetails(menuState = menuState, snippetId = clickedSnippetSaved) {
                    clickedSnipped = null
                }
            }
        }
    }

    menuState.resetNavigation()
    menuState.resetTitle()
}