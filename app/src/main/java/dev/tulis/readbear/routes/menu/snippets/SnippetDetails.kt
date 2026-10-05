package dev.tulis.readbear.routes.menu.snippets

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
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
import dev.tulis.readbear.routes.menu.snippets.utils.SnippetViewModel
import dev.tulis.readbear.routes.menu.snippets.utils.rememberSnippetEditState
import dev.tulis.readbear.utils.InfoRow
import dev.tulis.readbear.utils.formatDate
import dev.tulis.readbear.utils.normalizeName
import net.engawapg.lib.zoomable.ExperimentalZoomableApi
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalZoomableApi::class)
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

    BackHandler {
        onPopBack()
    }

    var zoomedPhoto by remember { mutableStateOf(false) }

    AnimatedVisibility(
        !zoomedPhoto,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.imePadding()
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .animateContentSize()
        ) {
            AsyncImage(
                model = snippetsDir.resolve(snippet.path),
                contentDescription = snippet.description,
                modifier = Modifier
                    .padding(bottom = 5.dp)
                    .fillMaxWidth(0.75f)
                    .heightIn(max = 400.dp)
                    .clickable {
                        zoomedPhoto = true
                    },
                contentScale = ContentScale.Fit
            )

            AnimatedContent(
                targetState = snippetEditState.editing,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut() using
                            SizeTransform(clip = false)
                },
                modifier = Modifier.fillMaxWidth()
            ) { editing ->

                Column(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(0.75f)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                    ) {
                        if (editing) {
                            OutlinedTextField(
                                value = snippetEditState.name,
                                onValueChange = {
                                    snippetEditState.name = it
                                },
                                isError = snippetEditState.name.isBlank(),
                                supportingText = {
                                    if(snippetEditState.name.isBlank()) {
                                        Text(stringResource(R.string.name_not_empty))
                                    }
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

                            Row {
                                TextButton(
                                    onClick = {
                                        snippetEditState.editing = false
                                    },
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    Text(stringResource(R.string.cancel))
                                }

                                Button(
                                    enabled = !snippetEditState.name.isBlank(),
                                    onClick = {
                                        if(snippetEditState.name.isBlank()) {
                                            return@Button
                                        }

                                        snippet.name = snippetEditState.name
                                        snippet.description = snippetEditState.description
                                        snippetEditState.editing = false

                                        viewModel.updateSnippet(snippet)
                                    },
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    Text(stringResource(R.string.save))
                                }
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

                        snippet.description?.let {
                            if(it.isBlank()) return@let

                            Text(
                                "„${it}‟",
                                modifier = Modifier.padding(start = 30.dp, end = 30.dp),
                                textAlign = TextAlign.Center,
                                fontStyle = FontStyle.Italic
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(5.dp)
                        )

                        Column(
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp)
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

                            InfoRow(
                                stringResource(R.string.created_on),
                                formatDate(snippet.createdAt)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(
                                10.dp,
                                Alignment.CenterHorizontally
                            ),
                            modifier = Modifier.padding(15.dp)
                        ) {
                            TextButton(onClick = {
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

            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.ime))
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    val snippetText = stringResource(R.string.snippet)

    LaunchedEffect(zoomedPhoto) {
        if(zoomedPhoto) return@LaunchedEffect

        menuState.updateTitle(snippetText)
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
    }

    AnimatedVisibility(
        visible = zoomedPhoto,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = snippetsDir.resolve(snippet.path),
                contentDescription = snippet.description,
                modifier = Modifier
                    .padding(15.dp)
                    .fillMaxWidth()
                    .zoomable(rememberZoomState()),
                contentScale = ContentScale.Fit
            )

            menuState.updateTitle("")

            menuState.updateNavigation {
                IconButton(
                    onClick = {
                        zoomedPhoto = false
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.go_back)
                    )
                }
            }

            menuState.updateActions {}
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