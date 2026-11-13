package ai.moon.celestia.feature.chat

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.moon.celestia.core.data.AiFunctionScope
import ai.moon.celestia.core.designsystem.component.CelestiaChip
import ai.moon.celestia.core.designsystem.component.CelestiaTextField
import ai.moon.celestia.core.designsystem.component.CelestiaTransparentGlassPill
import ai.moon.celestia.core.designsystem.component.MascotState
import ai.moon.celestia.core.designsystem.component.ModelSelectorChip
import ai.moon.celestia.core.designsystem.component.applePressScale
import ai.moon.celestia.core.designsystem.util.rememberCelestiaHaptics
import ai.moon.celestia.core.model.OllamaErrorType
import ai.moon.celestia.feature.chat.components.ChatMessageItem
import ai.moon.celestia.ui.CelestiaTab
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ActiveChatView(
    activeSessionId: String,
    viewModel: ChatViewModel,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    userAvatar: String = "Person",
    onNavigateToTab: (CelestiaTab) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val mascotState = when (uiState) {
        is ChatUiState.WarmingUp -> MascotState.Thinking
        is ChatUiState.Streaming -> MascotState.Streaming
        is ChatUiState.Error -> MascotState.Error
        else -> MascotState.Idle
    }

    val messages by viewModel.messages.collectAsState()
    val activeModel by viewModel.activeModel.collectAsState()
    val availableModels by viewModel.availableModels.collectAsState()
    val maxContextTokens by viewModel.maxContextTokensState.collectAsState()
    val isWebSearchEnabled by viewModel.isWebSearchEnabled.collectAsState()

    val estimatedTokens = remember(messages) {
        messages.sumOf { it.content.length } / 4
    }
    val contextRatio = (estimatedTokens.toFloat() / maxContextTokens.coerceAtLeast(1)).coerceIn(0f, 1f)

    var inputText by remember { mutableStateOf("") }
    var initialInputText by remember { mutableStateOf("") }

    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val recognizedText by viewModel.voiceManager.recognizedText.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.voiceManager.startListening()
        }
    }

    LaunchedEffect(isListening) {
        if (isListening) {
            initialInputText = inputText
        }
    }

    LaunchedEffect(recognizedText) {
        if (recognizedText.isNotBlank()) {
            inputText = if (initialInputText.isBlank()) recognizedText else "$initialInputText $recognizedText"
        }
    }

    val celestiaHaptics = rememberCelestiaHaptics()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // --- RECREATED FLUID SCROLL ENGINE ---
    val hasError = uiState is ChatUiState.Error
    val messageStartIndex = 1 + (if (hasError) 1 else 0)
    val lastMessageIndex = (messageStartIndex + messages.size - 1).coerceAtLeast(0)
    val bottomSpacerIndex = messageStartIndex + messages.size

    var userScrolledUp by remember { mutableStateOf(false) }

    val isAtBottom by remember {
        derivedStateOf {
            if (messages.isEmpty()) return@derivedStateOf true
            if (!listState.canScrollForward) return@derivedStateOf true
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf true

            val lastVisibleIndex = visibleItems.last().index
            lastVisibleIndex >= lastMessageIndex
        }
    }

    // Reset userScrolledUp when user reaches bottom naturally
    LaunchedEffect(isAtBottom) {
        if (isAtBottom) {
            userScrolledUp = false
        }
    }

    // Detect when user actively scrolls up away from bottom
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress && !isAtBottom) {
            userScrolledUp = true
        }
    }

    // --- HIGH-PERFORMANCE ZERO-JITTER SCROLL ENGINE ---
    val lastMessageContent = remember(messages) { messages.lastOrNull()?.content.orEmpty() }
    val isStreaming = uiState is ChatUiState.Streaming || uiState is ChatUiState.WarmingUp

    // Smooth animated scroll when a new message is added or state stops streaming
    LaunchedEffect(messages.size, isStreaming) {
        if (messages.isNotEmpty() && !userScrolledUp) {
            if (!isStreaming) {
                listState.animateScrollToItem(bottomSpacerIndex)
            }
        }
    }

    // Throttled high-performance scroll during token streaming (bypasses layout thrashing when already at bottom)
    LaunchedEffect(isStreaming, lastMessageContent.length) {
        if (isStreaming && messages.isNotEmpty() && !userScrolledUp) {
            if (listState.canScrollForward) {
                listState.scrollToItem(bottomSpacerIndex)
            }
        }
    }

    val topContentPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 62.dp
    val rawImeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val animatedImeBottom by animateDpAsState(
        targetValue = rawImeBottom,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioNoBouncy
        ),
        label = "animatedImeBottom"
    )
    val navBarsBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val activeChatBottomPadding = navBarsBottom + animatedImeBottom + 92.dp
    val scrollButtonBottomPadding = animatedImeBottom + navBarsBottom + 88.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .sharedBounds(
                sharedContentState = rememberSharedContentState(key = "session-$activeSessionId"),
                animatedVisibilityScope = animatedVisibilityScope,
                resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds()
            )
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = topContentPadding, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Item: Model Selector & Context Memory Bar
            item(key = "header_toolbar") {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ModelSelectorChip(
                                scope = AiFunctionScope.Chat,
                                selectedModel = activeModel,
                                availableModels = availableModels,
                                onModelSelected = { viewModel.selectModel(it) },
                                onRefreshModels = { viewModel.loadAvailableModels() }
                            )

                            CelestiaChip(
                                label = if (isWebSearchEnabled) "🌐 Web Search On" else "🌐 Search Off",
                                isSelected = isWebSearchEnabled,
                                onSelect = {
                                    celestiaHaptics.performActionPop()
                                    viewModel.toggleWebSearch()
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Context Memory",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "($estimatedTokens / $maxContextTokens tokens)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            LinearProgressIndicator(
                                progress = { contextRatio },
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = when {
                                    contextRatio > 0.85f -> MaterialTheme.colorScheme.error
                                    contextRatio > 0.6f -> Color(0xFFFF9800)
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        }
                    }
                }
            }

            // Optional Error Item
            if (hasError) {
                item(key = "error_banner") {
                    when (val state = uiState) {
                        is ChatUiState.Error -> {
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = when (val err = state.errorType) {
                                            is OllamaErrorType.OutOfMemory -> err.rawMessage
                                            is OllamaErrorType.ModelNotFound -> "Model '${err.modelName}' not found."
                                            is OllamaErrorType.ServerUnreachable -> "Server unreachable at ${err.hostUrl}."
                                            is OllamaErrorType.Timeout -> err.message
                                            is OllamaErrorType.Generic -> err.message
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Button(
                                            onClick = { viewModel.retryLastMessage() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error,
                                                contentColor = MaterialTheme.colorScheme.onError
                                            )
                                        ) {
                                            Text("Retry")
                                        }
                                    }
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }

            // Message Items
            items(messages, key = { it.id }) { msg ->
                val isLastStreamingItem = isStreaming && msg.id == messages.lastOrNull()?.id
                ChatMessageItem(
                    message = msg,
                    mascotState = mascotState,
                    userAvatar = userAvatar,
                    onSpeak = if (msg.role != "user") { { viewModel.voiceManager.speak(msg.content) } } else null,
                    onNavigateToTab = onNavigateToTab,
                    modifier = if (isLastStreamingItem) Modifier else Modifier.animateItem()
                )
            }

            // Bottom Spacer Item ensuring last message sits clear of the floating input bar
            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(activeChatBottomPadding))
            }
        }

        // Floating Scroll-to-Bottom Button
        AnimatedVisibility(
            visible = messages.isNotEmpty() && (!isAtBottom || userScrolledUp),
            enter = fadeIn(animationSpec = tween(180)) + scaleIn(initialScale = 0.7f),
            exit = fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.7f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = scrollButtonBottomPadding)
        ) {
            CelestiaTransparentGlassPill(
                modifier = Modifier
                    .size(42.dp)
                    .applePressScale {
                        celestiaHaptics.performActionPop()
                        userScrolledUp = false
                        coroutineScope.launch {
                            listState.animateScrollToItem(bottomSpacerIndex)
                        }
                    },
                shape = CircleShape,
                alpha = 0.25f
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Scroll to bottom",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Sticky Floating Input Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = animatedImeBottom)
                .background(Color.Transparent)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            CelestiaTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = "Message $activeModel...",
                onSend = {
                    if (inputText.isNotBlank()) {
                        celestiaHaptics.performActionPop()
                        val textToSend = inputText
                        inputText = ""
                        userScrolledUp = false
                        viewModel.sendMessage(textToSend)
                        coroutineScope.launch {
                            listState.scrollToItem(bottomSpacerIndex)
                        }
                    }
                },
                onStop = {
                    celestiaHaptics.performActionPop()
                    viewModel.stopGeneration()
                },
                onVoiceInput = {
                    celestiaHaptics.performActionPop()
                    if (isListening) {
                        viewModel.voiceManager.stopListening()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                isListening = isListening,
                isStreaming = isStreaming
            )
        }
    }
}
