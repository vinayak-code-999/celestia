package ai.moon.celestia.feature.chat

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.moon.celestia.core.data.ChatSessionDto
import ai.moon.celestia.core.designsystem.component.CelestiaChip
import ai.moon.celestia.core.designsystem.component.CelestiaMascotAvatar
import ai.moon.celestia.core.designsystem.component.MascotState
import ai.moon.celestia.core.designsystem.util.rememberCelestiaHaptics
import ai.moon.celestia.feature.chat.components.ChatSessionCard

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ChatSessionsGridScreen(
    viewModel: ChatViewModel,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val sessions by viewModel.sessions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val isFilterVisible by viewModel.isFilterVisible.collectAsState()
    val matchingSessionIds by viewModel.matchingSessionIds.collectAsState()
    val selectedSessionIds by viewModel.selectedSessionIds.collectAsState()
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()

    val celestiaHaptics = rememberCelestiaHaptics()
    var sessionToDelete by remember { mutableStateOf<ChatSessionDto?>(null) }

    val filteredSessions = remember(sessions, searchQuery, selectedTag, matchingSessionIds) {
        sessions.filter { session ->
            val matchesSearch = searchQuery.isBlank() || 
                                session.title.contains(searchQuery, ignoreCase = true) || 
                                matchingSessionIds.contains(session.id)
            val matchesTag = selectedTag == null || session.tags.contains(selectedTag)
            matchesSearch && matchesTag
        }.sortedByDescending { it.pinned }
    }

    val topContentPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 62.dp
    val bottomContentPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 80.dp

    // Session Deletion Confirmation Dialog
    if (sessionToDelete != null) {
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = {
                Text(
                    text = "Delete Conversation?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${sessionToDelete?.title}'? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        sessionToDelete?.let { session ->
                            viewModel.deleteSession(session.id)
                            celestiaHaptics.performActionPop()
                        }
                        sessionToDelete = null
                    }
                ) {
                    Text(text = "Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text(text = "Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CelestiaMascotAvatar(
                        state = MascotState.Idle,
                        size = 72.dp,
                        showLabel = true
                    )

                    Text(
                        text = "No Active Conversations",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Start a new chat or tap a suggestion below:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val starters = listOf(
                        "✨ Summarize an article",
                        "💡 Explain a complex topic",
                        "🛠️ Debug code snippet",
                        "🌐 Translate text"
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(starters) { prompt ->
                            CelestiaChip(
                                label = prompt,
                                isSelected = false,
                                onSelect = {
                                    celestiaHaptics.performActionPop()
                                    viewModel.sendMessage(prompt)
                                }
                            )
                        }
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 165.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = topContentPadding, bottom = bottomContentPadding),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val allTags = sessions.flatMap { it.tags }.toSet().toList()
                if (allTags.isNotEmpty() && (isFilterVisible || selectedTag != null)) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                CelestiaChip(
                                    label = "All",
                                    isSelected = selectedTag == null,
                                    onSelect = { viewModel.selectTag(null) }
                                )
                            }
                            items(allTags) { tag ->
                                CelestiaChip(
                                    label = "#$tag",
                                    isSelected = selectedTag == tag,
                                    onSelect = { viewModel.selectTag(tag) }
                                )
                            }
                        }
                    }
                }

                items(filteredSessions, key = { it.id }) { session ->
                    val isSelected = selectedSessionIds.contains(session.id)
                    ChatSessionCard(
                        session = session,
                        onClick = {
                            if (isMultiSelectMode) {
                                viewModel.toggleSessionSelection(session.id)
                            } else {
                                celestiaHaptics.performActionPop()
                                viewModel.selectSession(session.id)
                            }
                        },
                        onLongClick = {
                            viewModel.startMultiSelect(session.id)
                        },
                        isSelectedInMultiSelect = isSelected,
                        onPin = { viewModel.togglePinSession(session.id) },
                        onDelete = { sessionToDelete = session },
                        searchQuery = searchQuery,
                        modifier = Modifier
                            .animateItem()
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "session-${session.id}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds()
                            )
                    )
                }
            }
        }
    }
}
