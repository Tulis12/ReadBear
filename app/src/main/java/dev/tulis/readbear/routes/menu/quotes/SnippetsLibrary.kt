package dev.tulis.readbear.routes.menu.quotes

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import dev.tulis.readbear.db.quotes.snippets.Snippet
import dev.tulis.readbear.routes.menu.quotes.utils.QuoteViewModel
import dev.tulis.readbear.utils.cutText

@Composable
fun SnippetsLibrary(
    viewModel: QuoteViewModel = hiltViewModel(),
    padding: PaddingValues
) {
    val snippets = viewModel.getSnippets().collectAsState(ArrayList()).value

    val snippetsDir = LocalContext.current.filesDir.resolve("snippets")
    snippetsDir.mkdirs()

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.padding(padding)
    ) {
        items(snippets.count()) { snippetIndex ->
            val snippet = snippets[snippetIndex].snippet
            val book = snippets[snippetIndex].book

            val title = cutText(book.title)
            val author = cutText(book.author?.let { ";$it" } ?: "")

            val attrib = title + author + ":${snippet.progress + 1}"

            Card(
                modifier = Modifier.padding(5.dp).fillMaxWidth().heightIn(min = 50.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(15.dp)
                ) {
                    AsyncImage(
                        model = snippetsDir
                            .resolve(snippet.path),
                        contentDescription = null
                    )

                    Text(snippet.name, fontSize = 20.sp, modifier = Modifier.padding(top = 5.dp))
                    snippet.description?.let { Text(it, fontStyle = FontStyle.Italic) }
                    Text("— $attrib", modifier = Modifier.align(Alignment.End))
                }
            }
        }
    }
}