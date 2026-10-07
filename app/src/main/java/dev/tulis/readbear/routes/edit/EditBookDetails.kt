package dev.tulis.readbear.routes.edit

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import dev.tulis.readbear.R
import dev.tulis.readbear.db.entities.books.BookType
import dev.tulis.readbear.db.entities.comics.Comic
import dev.tulis.readbear.db.entities.epubs.Epub
import dev.tulis.readbear.db.entities.pdfs.Pdf
import dev.tulis.readbear.routes.menu.LibraryViewModel
import dev.tulis.readbear.utils.clickableWithRipple
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBookDetails(
    viewModel: LibraryViewModel = hiltViewModel(),
    bookId: Long,
    onPopBack: () -> Unit
) {
    val flowBook by viewModel.getBookFlow(bookId).collectAsStateWithLifecycle(null)

    val book = flowBook

    if(book == null) {
        CircularProgressIndicator()
        return
    }

    val context = LocalContext.current
    var showGoBackDialog by remember { mutableStateOf(false) }

    GoBack(
        showGoBackDialog,
        onPopBack = onPopBack,
        onHideDialog = {
            showGoBackDialog = false
        }
    )

    var changed = false

    var changedCover = false
    var chosenTitle: String? by remember { mutableStateOf(null) }
    var chosenAuthor: String? by remember { mutableStateOf(null) }
    var chosenSummary: String? by remember { mutableStateOf(null) }

    var additionalData: EditBookDetails? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        when(book.type) {
            BookType.Epub -> {
                val epub = viewModel.getEpubByBookId(bookId)
                additionalData = EpubBookDetails(epub)
            }

            BookType.Pdf -> {
                val pdf = viewModel.getPdfByBookId(bookId)
                additionalData = PdfBookDetails(
                    pdf = pdf,
                    keywords = pdf.keywords
                )
            }

            BookType.Comic -> {
                val comic = viewModel.getComicByBookId(bookId)
                additionalData = ComicBookDetails(
                    comic = comic,
                    series = comic.series,
                    seriesStatus = comic.seriesStatus,
                    manga = comic.manga,
                )
            }
        }
    }

    var cover by remember {
        mutableStateOf(
        context.filesDir
                .resolve(book.path)
                .resolve(book.cover)
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            changed = true
            changedCover = true

            val mime = context.contentResolver.getType(uri)
            var extension = when (mime) {
                "image/png" -> ".png"
                "image/jpeg" -> ".jpg"
                "image/webp" -> ".webp"
                "image/gif" -> ".gif"
                else -> ""
            }

            extension = UUID.randomUUID().toString() + extension

            val tempFile = File.createTempFile("cover_", extension, context.cacheDir)

            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            cover = tempFile
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.edit))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if(changed) {
                            showGoBackDialog = true
                        } else {
                            onPopBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.go_back))
                    }
                },
                actions = {
                    val scope = rememberCoroutineScope()

                    IconButton(onClick = {
                        if(changedCover) {
                            val coverFilename = "cover_${UUID.randomUUID()}.${cover.extension}"
                            val oldCover = context.filesDir
                                .resolve(book.path)
                                .resolve(book.cover)

                            book.cover = coverFilename

                            scope.launch(Dispatchers.IO) {
                                val targetCover = context.filesDir
                                    .resolve(book.path)
                                    .resolve(coverFilename)

                                cover.copyTo(targetCover)
                                cover.delete()

                                oldCover.delete()
                            }
                        }

                        chosenTitle?.let {
                            book.title = it
                        }

                        chosenAuthor?.let {
                            book.author = it
                        }

                        chosenSummary?.let {
                            book.summary = it
                        }

                        additionalData?.let { data ->

                            when(data) {
                                is ComicBookDetails -> {
                                    val comic = data.comic
                                    comic.series = data.series
                                    comic.seriesStatus = data.seriesStatus
                                    comic.manga = data.manga
                                    viewModel.updateComic(comic)
                                }

                                is PdfBookDetails -> {
                                    val pdf = data.pdf
                                    pdf.keywords = data.keywords
                                    viewModel.updatePdf(pdf)
                                }

                                is EpubBookDetails -> {

                                }
                            }
                        }

                        viewModel.updateBook(book)
                        onPopBack()
                    }) {
                        Icon(Icons.Default.Save, contentDescription = stringResource(R.string.save))
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Box(
                modifier = Modifier.clickableWithRipple {
                    launcher.launch(arrayOf("image/*"))
                }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(cover)
                        .diskCachePolicy(CachePolicy.DISABLED)
                        .memoryCachePolicy(CachePolicy.DISABLED)
                        .build(),
                    contentDescription = "",
                    modifier = Modifier
                        .width(250.dp)
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(15.dp)),
                    contentScale = ContentScale.Crop
                )
                
                IconButton(
                    onClick = {
                        launcher.launch(arrayOf("image/*"))
                    },
                    modifier = Modifier.align(Alignment.Center),
                    colors = IconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.9f),
                        contentColor = Color.Blue,
                        disabledContainerColor = Color.Gray,
                        disabledContentColor = Color.Gray
                    )
                ) {
                    Icon(Icons.Default.Upload, contentDescription = stringResource(R.string.select_file))
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(0.75f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = chosenTitle ?: book.title,
                    onValueChange = {
                        changed = true
                        chosenTitle = it
                    },
                    label = {
                        Text(stringResource(R.string.title))
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = chosenAuthor ?: (book.author ?: ""),
                    onValueChange = {
                        changed = true
                        chosenAuthor = it
                    },
                    label = {
                        Text(stringResource(R.string.author))
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = chosenSummary ?: (book.summary ?: ""),
                    onValueChange = {
                        changed = true
                        chosenSummary = it
                    },
                    label = {
                        Text(stringResource(R.string.summary))
                    }
                )

                additionalData?.let { data ->

                    when(data) {
                        is ComicBookDetails -> {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = data.series ?: "",
                                onValueChange = {
                                    changed = true
                                    data.series = it
                                },
                                label = {
                                    Text(stringResource(R.string.series))
                                },
                                singleLine = true
                            )

                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = data.seriesStatus ?: "",
                                onValueChange = {
                                    changed = true
                                    data.seriesStatus = it
                                },
                                label = {
                                    Text(stringResource(R.string.series_status))
                                },
                                singleLine = true
                            )

                            val state = when (data.manga) {
                                true -> ToggleableState.On
                                false -> ToggleableState.Off
                                null -> ToggleableState.Indeterminate
                            }


                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TriStateCheckbox(
                                    state = state,
                                    onClick = {
                                        changed = true

                                        data.manga = when (state) {
                                            ToggleableState.Indeterminate -> true
                                            ToggleableState.On -> false
                                            ToggleableState.Off -> true
                                        }
                                    }
                                )

                                Text(stringResource(R.string.is_manga))
                            }
                        }

                        is PdfBookDetails -> {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = data.keywords ?: "",
                                onValueChange = {
                                    changed = true
                                    data.keywords
                                },
                                label = {
                                    stringResource(R.string.keywords)
                                },
                                singleLine = true
                            )
                        }

                        is EpubBookDetails -> {

                        }
                    }
                }
            }
        }
    }
}

open class EditBookDetails {}

class ComicBookDetails(
    comic: Comic,
    series: String?,
    seriesStatus: String?,
    manga: Boolean?
) : EditBookDetails() {
    var comic by mutableStateOf(comic)
    var series by mutableStateOf(series)
    var seriesStatus by mutableStateOf(seriesStatus)
    var manga by mutableStateOf(manga)
}

data class EpubBookDetails(
    var epub: Epub
) : EditBookDetails() // TODO()

data class PdfBookDetails(
    var pdf: Pdf,
    var keywords: String?
) : EditBookDetails()


@Composable
fun GoBack(
    showDialog: Boolean,
    onPopBack: () -> Unit,
    onHideDialog: () -> Unit
) {
    if(showDialog) {
        AlertDialog(
            onDismissRequest = {
                onHideDialog()
            },
            title = {
                Text(stringResource(R.string.discard_changes))
            },
            text = {
                Text(stringResource(R.string.are_you_sure_you_want_to_discard_changes))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onPopBack()
                        onHideDialog()
                    }
                ) {
                    Text(stringResource(R.string.discard_changes))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onHideDialog()
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}