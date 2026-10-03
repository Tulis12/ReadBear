package dev.tulis.readbear.routes.menu.quotes

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import dev.tulis.readbear.R
import dev.tulis.readbear.routes.menu.MenuState
import dev.tulis.readbear.routes.menu.quotes.utils.SnippetViewModel
import dev.tulis.readbear.routes.menu.quotes.utils.rememberSnippetEditState
import dev.tulis.readbear.utils.InfoRow
import dev.tulis.readbear.utils.clickableWithoutRipple
import dev.tulis.readbear.utils.normalizeName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnippetDetails(
    viewModel: SnippetViewModel = hiltViewModel(),
    menuState: MenuState,
    snippetId: Long,
    onPopBack: () -> Unit
) {
    val snippetWithBookFlow by viewModel.getSnippetWithBookById(snippetId).collectAsState(null)
    val snippetWithBook = snippetWithBookFlow ?: return

    val book = snippetWithBook.book
    val snippet = snippetWithBook.snippet

    val context = LocalContext.current
    val snippetsDir = context.filesDir.resolve("snippets")

    val snippetEditState = rememberSnippetEditState(snippet.name, snippet.description)

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

            Crossfade(
                snippetEditState.editing
            ) { editing ->
                Column(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxHeight(0.4f).fillMaxWidth(0.75f)
                ) {
                    if (editing) {
                        OutlinedTextField(
                            value = snippetEditState.name,
                            onValueChange = {
                                snippetEditState.name = it
                            },
                            label = {
                                Text(stringResource(R.string.name))
                            },
                            maxLines = 1
                        )

                        OutlinedTextField(
                            value = snippetEditState.description ?: "",
                            onValueChange = {
                                snippetEditState.description = it
                            },
                            label = {
                                Text(stringResource(R.string.description))
                            }
                        )

                        Button(
                            onClick = {
                                snippet.name = snippetEditState.name
                                snippet.description = snippetEditState.description
                                snippetEditState.editing = false

                                viewModel.updateSnippet(snippet)
                            },
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Text(stringResource(R.string.save))
                        }

                        return@Column
                    }

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
                        modifier = Modifier.padding(start = 30.dp, end = 30.dp),
                        textAlign = TextAlign.Justify,
                        fontStyle = FontStyle.Italic
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(15.dp)
                    )

                    Column(
                        modifier = Modifier.padding(start = 30.dp, end = 30.dp)
                    ) {
                        InfoRow(
                            stringResource(R.string.book),
                            book.title
                        )

                        book.author?.let {
                            InfoRow(
                                stringResource(R.string.author),
                                it
                            )
                        }

                        InfoRow(
                            stringResource(R.string.source),
                            snippet.progress.toString()
                        )
                    }



                    Row(
                        horizontalArrangement = Arrangement.spacedBy(
                            10.dp,
                            Alignment.CenterHorizontally
                        ),
                        modifier = Modifier.padding(15.dp)
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
        }
    }


    menuState.updateTitle(stringResource(R.string.snippet))
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


    var showDeleteDialog by remember { mutableStateOf(false) }

    menuState.updateActions {
        IconButton(onClick = {
            snippetEditState.editing = true
        }) {
            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
        }

        IconButton(onClick = {
            showDeleteDialog = true
        }) {
            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
        }
    }

    if(showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text(stringResource(R.string.confirm))
            },
            text = {
                Text(
                    stringResource(R.string.confirm_snippet_delete)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onPopBack()
                        showDeleteDialog = false
                        viewModel.removeSnippet(snippet)
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}