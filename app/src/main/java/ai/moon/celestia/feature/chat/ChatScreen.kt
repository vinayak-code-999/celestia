package ai.moon.celestia.feature.chat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import ai.moon.celestia.core.data.SharedContentBus
import ai.moon.celestia.ui.CelestiaTab

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    userAvatar: String = "Person",
    onNavigateToTab: (CelestiaTab) -> Unit = {}
) {
    LaunchedEffect(Unit) {
        val payload = SharedContentBus.consumePayload()
        if (payload != null && payload.targetTab == CelestiaTab.Chat) {
            val fullPrompt = if (!payload.actionPrompt.isNullOrBlank()) "${payload.actionPrompt}\n\n${payload.content}" else payload.content
            viewModel.sendMessage(fullPrompt)
        }
    }

    val currentSessionId by viewModel.currentSessionId.collectAsState()

    SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentSessionId,
            transitionSpec = {
                val spatialSpring = spring<IntOffset>(
                    stiffness = 250f,
                    dampingRatio = 0.85f
                )
                val fadeSpring = spring<Float>(
                    stiffness = 250f,
                    dampingRatio = 0.85f
                )

                if (targetState == null) {
                    // Exiting chat to grid (pop back)
                    (slideInHorizontally(animationSpec = spatialSpring) { -(it / 3) } +
                     fadeIn(animationSpec = fadeSpring))
                    .togetherWith(
                        slideOutHorizontally(animationSpec = spatialSpring) { it } +
                        fadeOut(animationSpec = fadeSpring)
                    ).apply { targetContentZIndex = -1f }
                } else {
                    // Entering chat from grid (push forward)
                    (slideInHorizontally(animationSpec = spatialSpring) { it } +
                     fadeIn(animationSpec = fadeSpring))
                    .togetherWith(
                        slideOutHorizontally(animationSpec = spatialSpring) { -(it / 3) } +
                        fadeOut(animationSpec = fadeSpring)
                    ).apply { targetContentZIndex = 1f }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            label = "chatGridToActiveChat"
        ) { activeSessionId ->
            val animatedVisibilityScope = this
            if (activeSessionId == null) {
                // View 1: Chats History Grid View
                ChatSessionsGridScreen(
                    viewModel = viewModel,
                    animatedVisibilityScope = animatedVisibilityScope
                )
            } else {
                // View 2: Active Chat Conversation View
                ActiveChatView(
                    activeSessionId = activeSessionId,
                    viewModel = viewModel,
                    animatedVisibilityScope = animatedVisibilityScope,
                    userAvatar = userAvatar,
                    onNavigateToTab = onNavigateToTab
                )
            }
        }
    }
}
