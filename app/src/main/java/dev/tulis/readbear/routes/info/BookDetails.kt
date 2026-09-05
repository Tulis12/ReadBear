package dev.tulis.readbear.routes.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import dev.tulis.readbear.R
import dev.tulis.readbear.db.books.BookType
import dev.tulis.readbear.db.comics.Comic
import dev.tulis.readbear.db.pdfs.Pdf
import dev.tulis.readbear.utils.readingProgress
import dev.tulis.readbear.utils.readingTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetails(
    viewModel: BookDetailsViewModel = hiltViewModel(),
    bookId: Long,
    onPopBack: () -> Unit
) {
    val bookFlow by viewModel.getBookById(bookId).collectAsState(null)
    val book = bookFlow ?: return

    val context = LocalContext.current

    var cover by remember {
        mutableStateOf(
            context.filesDir
                .resolve(book.path)
                .resolve(book.cover)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.info))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onPopBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.go_back))
                    }
                }
            )
        }
    ) {
        paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(cover)
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .build(),
                contentDescription = "",
                modifier = Modifier
                    .padding(bottom = 5.dp)
                    .width(250.dp)
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(15.dp)),
                contentScale = ContentScale.Crop
            )

            Text(
                book.title,
                modifier = Modifier.padding(start = 30.dp, end = 30.dp),
                fontSize = 30.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                book.author ?: stringResource(R.string.unknown),
                modifier = Modifier.padding(bottom = 15.dp)
            )

//            HorizontalDivider(
//                modifier = Modifier.fillMaxWidth(0.75f)
//            )

            Row(
                modifier = Modifier.fillMaxWidth(0.75f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BookStat(
                    modifier = Modifier.weight(1f),
                    value = readingProgress(book),
                    label = stringResource(R.string.reading_progress)
                )

                BookStat(
                    modifier = Modifier.weight(1f),
                    value = book.totalProgress.toString(),
                    label = when(book.type) {
                        BookType.Pdf -> stringResource(R.string.pages)
                        BookType.Comic -> stringResource(R.string.panels)
                        BookType.Epub -> stringResource(R.string.chapters)
                    }
                )

                BookStat(
                    modifier = Modifier.weight(1f),
                    value = readingTime(book.readingTime),
                    label = stringResource(R.string.reading_time)
                )
            }

            if(book.summary != null && !book.summary!!.isEmpty()) {
                Text(
                    "\"${book.summary!!}\"",
                    modifier = Modifier.padding(start = 30.dp, end = 30.dp, top = 15.dp, bottom = 15.dp),
                    textAlign = TextAlign.Justify
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(15.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(0.5f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoRow(
                    stringResource(R.string.book_type),
                    book.type.toString().lowercase(LocalLocale.current.platformLocale)
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(
                            LocalLocale.current.platformLocale
                        ) else it.toString() }
                )

                when(book.type) {
                    BookType.Epub -> TODO()
                    BookType.Pdf -> {
                        var pdf: Pdf? by remember { mutableStateOf(null) }
                        LaunchedEffect(Unit) {
                            pdf = viewModel.getPdfByBookId(bookId)
                        }

                        val savedPdf = pdf

                        if(savedPdf != null) {
                            InfoRow(
                                stringResource(R.string.keywords),
                                savedPdf.keywords ?: stringResource(R.string.unknown)
                            )
                        }
                    }
                    BookType.Comic -> {
                        var comic: Comic? by remember { mutableStateOf(null) }
                        LaunchedEffect(Unit) {
                            comic = viewModel.getComicByBookId(bookId)
                        }

                        val savedComic = comic

                        if(savedComic != null) {
                            InfoRow(
                                stringResource(R.string.series),
                                savedComic.series ?: stringResource(R.string.unknown)
                            )

                            InfoRow(
                                stringResource(R.string.series_status),
                                savedComic.seriesStatus ?: stringResource(R.string.unknown)
                            )

                            InfoRow(
                                stringResource(R.string.is_manga),
                                if(savedComic.manga != null && savedComic.manga == true) {
                                    stringResource(R.string.yes)
                                } else if(savedComic.manga != null) {
                                    stringResource(R.string.no)
                                } else {
                                    stringResource(R.string.unknown)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookStat(
    modifier: Modifier = Modifier,
    value: String,
    label: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            fontWeight = FontWeight.Medium
        )
    }
}