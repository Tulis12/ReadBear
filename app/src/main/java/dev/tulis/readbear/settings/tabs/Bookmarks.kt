package dev.tulis.readbear.settings.tabs

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dev.tulis.readbear.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkSettings(
    allowReversingProgress: Boolean,
    onChangeAllowReversingProgress: (Boolean) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = allowReversingProgress,
                onCheckedChange = {
                    onChangeAllowReversingProgress(it)
                }
            )

            Text(stringResource(R.string.allow_reversing_progress), modifier = Modifier.padding(end = 5.dp).clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onChangeAllowReversingProgress(!allowReversingProgress)
            })

            val context = LocalContext.current
            val info = stringResource(R.string.info)
            val tooltipState = rememberTooltipState(isPersistent = true)
            val scope = rememberCoroutineScope()

            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    TooltipAnchorPosition.Above,
                    10.dp
                ),
                tooltip = {
                    RichTooltip(
                        title = {
                            Text(stringResource(R.string.allow_reversing_progress))
                        },
                        action = {
                            Button(onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    "https://github.com/Tulis12/ReadBear/issues/21".toUri()
                                )

                                context.startActivity(intent)
                            }) {
                                Text(stringResource(R.string.read_more))
                            }
                        }
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(stringResource(R.string.allow_reversing_progress_tooltip), textAlign = TextAlign.Justify)
                        }

                    }
                },
                state = tooltipState
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            tooltipState.show()
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = info
                    )
                }
            }
        }
    }
}