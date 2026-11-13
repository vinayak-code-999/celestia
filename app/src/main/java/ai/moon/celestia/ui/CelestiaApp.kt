package ai.moon.celestia.ui

import ai.moon.celestia.core.designsystem.component.navigation.CelestiaTopBar
import ai.moon.celestia.core.designsystem.component.navigation.CelestiaGlassBottomBar
import ai.moon.celestia.core.designsystem.component.MascotState

import ai.moon.celestia.core.designsystem.theme.CelestiaTheme
import ai.moon.celestia.core.designsystem.theme.CelestiaThemeStyle
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import ai.moon.celestia.core.model.ServerHealthStatus
import ai.moon.celestia.feature.chat.ChatScreen
import ai.moon.celestia.feature.chat.ChatUiState
import ai.moon.celestia.feature.chat.ChatViewModel
import ai.moon.celestia.feature.designguide.GuidingDesignGuideScreen
import ai.moon.celestia.feature.settings.SettingsScreen
import ai.moon.celestia.feature.settings.SettingsViewModel
import ai.moon.celestia.feature.summarize.SummarizeScreen
import ai.moon.celestia.feature.summarize.SummarizeViewModel
import ai.moon.celestia.feature.translate.TranslateScreen
import ai.moon.celestia.feature.translate.TranslateViewModel
import ai.moon.celestia.feature.workbench.WorkbenchScreen
import ai.moon.celestia.feature.workbench.WorkbenchViewModel
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import android.os.Build
import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.DocumentScanner

val LocalLayerBackdrop = androidx.compose.runtime.compositionLocalOf<LayerBackdrop?> { null }

data class GlassPreferences(
    val blurRadius: Float = 16f,
    val refractionPower: Float = 12f,
    val alpha: Float = 0.20f,
    val shineIntensity: Float = 0.45f,
    val chromaticAberration: Boolean = true,
    val warmRadiance: Float = 0.25f,
    val presetName: String = "PeachWarm"
)

val LocalGlassPreferences = androidx.compose.runtime.compositionLocalOf { GlassPreferences() }

enum class CelestiaTab(
    val title: String,
    val icon: ImageVector
) {
    Hub("Hub", Icons.Default.AutoAwesome),
    Chat("Chat", Icons.Default.ChatBubbleOutline),
    Scanner("Scanner", Icons.Default.DocumentScanner),
    Summarize("Summarize", Icons.Default.Summarize),
    Translate("Translate", Icons.Default.Translate),
    Workbench("Workbench", Icons.Default.AutoAwesome)
}

sealed interface AppScreen {
    data class Main(val tab: CelestiaTab) : AppScreen
    data object Settings : AppScreen
    data object DesignGuide : AppScreen
}

val AppScreen.depth: Int
    get() = when (this) {
        is AppScreen.Main -> 0
        AppScreen.Settings -> 1
        AppScreen.DesignGuide -> 2
    }

@Composable
fun CelestiaApp(
    chatViewModel: ChatViewModel,
    scannerViewModel: ai.moon.celestia.feature.scanner.ScannerViewModel,
    summarizeViewModel: SummarizeViewModel,
    translateViewModel: TranslateViewModel,
    workbenchViewModel: WorkbenchViewModel,
    settingsViewModel: SettingsViewModel
) {
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Main(CelestiaTab.Chat)) }
    val healthStatus by settingsViewModel.healthStatus.collectAsState()
    val isConnected = healthStatus is ServerHealthStatus.Connected
    val currentSessionId by chatViewModel.currentSessionId.collectAsState()
    val sessions by chatViewModel.sessions.collectAsState()
    val isSearchVisible by chatViewModel.isSearchVisible.collectAsState()
    val searchQuery by chatViewModel.searchQuery.collectAsState()
    val userPrefs by settingsViewModel.userPreferences.collectAsState(initial = ai.moon.celestia.core.datastore.UserPreferences())

    val chatUiState by chatViewModel.uiState.collectAsState()
    val scannerUiState by scannerViewModel.uiState.collectAsState()
    val isSummarizing by summarizeViewModel.isSummarizing.collectAsState()
    val isTranslating by translateViewModel.isTranslating.collectAsState()
    val isExecutingWorkbench by workbenchViewModel.isExecuting.collectAsState()

    val scannerTokensPerSec by scannerViewModel.tokensPerSec.collectAsState()
    val summarizeTokensPerSec by summarizeViewModel.tokensPerSec.collectAsState()
    val translateTokensPerSec by translateViewModel.tokensPerSec.collectAsState()
    val workbenchTokensPerSec by workbenchViewModel.tokensPerSec.collectAsState()

    val activeTasks by ai.moon.celestia.core.data.CelestiaTaskManager.activeTasks.collectAsState()
    val hasActiveTasks = activeTasks.isNotEmpty()

    val (mascotState, tokensPerSecond) = when {
        chatUiState is ChatUiState.Streaming -> {
            val streaming = (chatUiState as ChatUiState.Streaming)
            MascotState.Streaming to streaming.currentTokensPerSec.toDouble()
        }
        chatUiState is ChatUiState.WarmingUp -> {
            MascotState.Thinking to null
        }
        scannerUiState is ai.moon.celestia.feature.scanner.ScannerUiState.Processing -> {
            MascotState.Processing to (if (scannerTokensPerSec > 0.0) scannerTokensPerSec else null)
        }
        isSummarizing -> {
            MascotState.Streaming to (if (summarizeTokensPerSec > 0.0) summarizeTokensPerSec else null)
        }
        isTranslating -> {
            MascotState.Streaming to (if (translateTokensPerSec > 0.0) translateTokensPerSec else null)
        }
        isExecutingWorkbench -> {
            MascotState.Executing to (if (workbenchTokensPerSec > 0f) workbenchTokensPerSec.toDouble() else null)
        }
        hasActiveTasks -> {
            MascotState.Streaming to null
        }
        chatUiState is ChatUiState.Error -> {
            MascotState.Error to null
        }
        else -> {
            MascotState.Idle to null
        }
    }

    val activeThemeStyle = CelestiaThemeStyle.fromId(userPrefs.themeStyle)

    val isDark = when (userPrefs.themeMode) {
        "Dark" -> true
        "Light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val window = (view.context as? android.app.Activity)?.window
            if (window != null) {
                val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    val globalBackdrop = rememberLayerBackdrop()
    val glassPrefs = GlassPreferences(
        blurRadius = userPrefs.glassBlur,
        refractionPower = userPrefs.glassRefraction,
        alpha = userPrefs.glassAlpha,
        shineIntensity = userPrefs.glassShineIntensity,
        chromaticAberration = userPrefs.glassChromaticAberration,
        warmRadiance = userPrefs.glassWarmRadiance,
        presetName = userPrefs.glassPreset
    )

    androidx.compose.runtime.CompositionLocalProvider(
        LocalLayerBackdrop provides globalBackdrop,
        LocalGlassPreferences provides glassPrefs
    ) {
        CelestiaTheme(
            darkTheme = isDark,
            dynamicColor = userPrefs.useDynamicColors,
            themeStyle = activeThemeStyle
        ) {
            val currentTab = (currentScreen as? AppScreen.Main)?.tab
            val isInActiveChat = currentTab == CelestiaTab.Chat && currentSessionId != null
            val isFilterVisible by chatViewModel.isFilterVisible.collectAsState()
            val selectedTag by chatViewModel.selectedTag.collectAsState()
            val isMultiSelectMode by chatViewModel.isMultiSelectMode.collectAsState()
            val selectedSessionIds by chatViewModel.selectedSessionIds.collectAsState()

            val topBarTitle = when (currentScreen) {
                is AppScreen.Main -> {
                    val tab = (currentScreen as AppScreen.Main).tab
                    if (tab == CelestiaTab.Chat && currentSessionId != null) {
                        val session = sessions.firstOrNull { it.id == currentSessionId }
                        val rawTitle = session?.title.orEmpty().trim()
                        if (rawTitle.isBlank() || rawTitle.startsWith("Chat with", ignoreCase = true)) {
                            "New Chat"
                        } else {
                            rawTitle
                        }
                    } else if (tab == CelestiaTab.Chat) {
                        "Celestia"
                    } else {
                        tab.title
                    }
                }
                AppScreen.Settings -> "Settings & Profile"
                AppScreen.DesignGuide -> "Design System Guide"
            }

            val onBack = when {
                currentScreen is AppScreen.Settings || currentScreen is AppScreen.DesignGuide -> {
                    { currentScreen = AppScreen.Main(CelestiaTab.Chat) }
                }
                isInActiveChat -> {
                    { chatViewModel.closeActiveChat() }
                }
                currentScreen is AppScreen.Main && (currentScreen as AppScreen.Main).tab != CelestiaTab.Chat -> {
                    { currentScreen = AppScreen.Main(CelestiaTab.Chat) }
                }
                else -> null
            }

            val isHome = currentScreen == AppScreen.Main(CelestiaTab.Chat) && currentSessionId == null && !isSearchVisible

            androidx.activity.compose.BackHandler(enabled = !isHome) {
                when {
                    isSearchVisible -> chatViewModel.setSearchVisible(false)
                    currentSessionId != null -> chatViewModel.closeActiveChat()
                    currentScreen is AppScreen.Settings || currentScreen is AppScreen.DesignGuide -> {
                        currentScreen = AppScreen.Main(CelestiaTab.Chat)
                    }
                    currentScreen is AppScreen.Main && (currentScreen as AppScreen.Main).tab != CelestiaTab.Chat -> {
                        currentScreen = AppScreen.Main(CelestiaTab.Chat)
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Capture Box: Everything inside is sampled by floating glass elements
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .layerBackdrop(globalBackdrop)
                ) {
                    // Main App Background - Inside capture box so it is visible in glass effects
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    )

                    // 1. Content Layer with Paging Awareness & Spatial Integrity
                    androidx.compose.runtime.CompositionLocalProvider(LocalLayerBackdrop provides globalBackdrop) {
                        @OptIn(ExperimentalSharedTransitionApi::class)
                        SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
                            AnimatedContent(
                                targetState = currentScreen,
                                contentKey = { it.depth },
                                transitionSpec = {
                                    val spatialSpring = spring<androidx.compose.ui.unit.IntOffset>(
                                        stiffness = 250f, // Looser for fluidity
                                        dampingRatio = 0.85f
                                    )
                                    val fadeSpring = spring<Float>(
                                        stiffness = 250f,
                                        dampingRatio = 0.85f
                                    )

                                    val isPush = targetState.depth > initialState.depth
                                    val enterOffset: (Int) -> Int = if (isPush) { width -> width } else { width -> -(width / 3) }
                                    val exitOffset: (Int) -> Int = if (isPush) { width -> -(width / 3) } else { width -> width }

                                    (slideInHorizontally(animationSpec = spatialSpring, initialOffsetX = enterOffset) +
                                     fadeIn(animationSpec = fadeSpring))
                                        .togetherWith(
                                            slideOutHorizontally(animationSpec = spatialSpring, targetOffsetX = exitOffset) +
                                            fadeOut(animationSpec = fadeSpring)
                                        ).apply {
                                            targetContentZIndex = targetState.depth.toFloat()
                                        }
                                },
                                modifier = Modifier.fillMaxSize(),
                                label = "spatialScreenTransition"
                            ) { screen ->
                                when (screen.depth) {
                                    0 -> {
                                        val tabs = CelestiaTab.entries
                                        val currentMainTab = (currentScreen as? AppScreen.Main)?.tab ?: CelestiaTab.Chat
                                        
                                        // Disable pager scrolling if we are actively in a chat session
                                        val isUserScrollEnabled = currentSessionId == null

                                        val pagerState = rememberPagerState(
                                            initialPage = tabs.indexOf(currentMainTab).coerceAtLeast(0),
                                            pageCount = { tabs.size }
                                        )

                                        LaunchedEffect(currentMainTab) {
                                            val targetIndex = tabs.indexOf(currentMainTab)
                                            if (targetIndex >= 0 && pagerState.currentPage != targetIndex) {
                                                pagerState.animateScrollToPage(
                                                    targetIndex,
                                                    animationSpec = spring(stiffness = 350f, dampingRatio = 0.9f)
                                                )
                                            }
                                        }

                                        LaunchedEffect(pagerState) {
                                            snapshotFlow { pagerState.targetPage }.collect { targetIndex ->
                                                val targetTab = tabs.getOrNull(targetIndex)
                                                if (targetTab != null && (currentScreen as? AppScreen.Main)?.tab != targetTab) {
                                                    currentScreen = AppScreen.Main(targetTab)
                                                }
                                            }
                                        }

                                        HorizontalPager(
                                            state = pagerState,
                                            userScrollEnabled = isUserScrollEnabled,
                                            modifier = Modifier.fillMaxSize()
                                        ) { page ->
                                            val tab = tabs[page]
                                            val availableModels by chatViewModel.availableModels.collectAsState()
                                            when (tab) {
                                                CelestiaTab.Hub -> ai.moon.celestia.feature.hub.HubScreen(
                                                    availableModels = availableModels,
                                                    chatModel = userPrefs.chatModel,
                                                    ocrModel = userPrefs.ocrModel,
                                                    summarizeModel = userPrefs.languageModel,
                                                    translateModel = userPrefs.languageModel,
                                                    workbenchModel = userPrefs.codingModel,
                                                    onSelectModelForScope = { scope, model ->
                                                        settingsViewModel.updateModelForScope(scope, model)
                                                    },
                                                    onRefreshModels = { chatViewModel.loadAvailableModels() },
                                                    onNavigateToTab = { t -> currentScreen = AppScreen.Main(t) },
                                                    onOpenSettings = { currentScreen = AppScreen.Settings }
                                                )
                                                CelestiaTab.Chat -> ChatScreen(
                                                    viewModel = chatViewModel,
                                                    userAvatar = userPrefs.userAvatar,
                                                    onNavigateToTab = { t -> currentScreen = AppScreen.Main(t) }
                                                )
                                                CelestiaTab.Scanner -> ai.moon.celestia.feature.scanner.ScannerScreen(
                                                    viewModel = scannerViewModel,
                                                    onNavigateToTab = { t -> currentScreen = AppScreen.Main(t) }
                                                )
                                                CelestiaTab.Summarize -> SummarizeScreen(
                                                    viewModel = summarizeViewModel,
                                                    onNavigateToTab = { t -> currentScreen = AppScreen.Main(t) }
                                                )
                                                CelestiaTab.Translate -> TranslateScreen(
                                                    viewModel = translateViewModel,
                                                    onNavigateToTab = { t -> currentScreen = AppScreen.Main(t) }
                                                )
                                                CelestiaTab.Workbench -> WorkbenchScreen(
                                                    viewModel = workbenchViewModel,
                                                    onStartWorkflowChat = { presetId ->
                                                        when (presetId) {
                                                            "math_solver" -> chatViewModel.startMathSolverChat()
                                                            "refactor" -> chatViewModel.startCodeRefactorChat()
                                                            "code_generator" -> chatViewModel.startCodeGeneratorChat()
                                                            "email" -> chatViewModel.startEmailWriterChat()
                                                            "grammar" -> chatViewModel.startGrammarAuditChat()
                                                            "json_extract" -> chatViewModel.startJsonExtractorChat()
                                                            else -> chatViewModel.startMathSolverChat()
                                                        }
                                                        currentScreen = AppScreen.Main(CelestiaTab.Chat)
                                                    },
                                                    onNavigateToTab = { t -> currentScreen = AppScreen.Main(t) }
                                                )
                                            }
                                        }
                                    }
                                    1 -> {
                                        SettingsScreen(
                                            viewModel = settingsViewModel,
                                            onOpenDesignGuide = { currentScreen = AppScreen.DesignGuide }
                                        )
                                    }
                                    2 -> {
                                        GuidingDesignGuideScreen()
                                    }
                                }
                            }
                        }
                    }
                }

            // Top Backdrop Scrim (translucent background to transparent gradient)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        )
                    )
            )

            val allTags = remember(sessions) { sessions.flatMap { it.tags }.distinct() }
            val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

            val handleCloseSearch = {
                focusManager.clearFocus()
                chatViewModel.updateSearchQuery("")
                chatViewModel.setSearchVisible(false)
            }

            CelestiaTopBar(
                title = topBarTitle,
                isConnected = isConnected,
                mascotState = mascotState,
                tokensPerSecond = tokensPerSecond,
                showMascot = !isSearchVisible,
                isSearchMode = isSearchVisible && currentTab == CelestiaTab.Chat && currentSessionId == null,
                searchQuery = searchQuery,
                onSearchQueryChange = { chatViewModel.updateSearchQuery(it) },
                onCloseSearch = handleCloseSearch,
                onOpenSettings = if (currentSessionId == null) { { currentScreen = AppScreen.Settings } } else null,
                availableTags = allTags,
                selectedTag = selectedTag,
                onTagSelected = { tag -> chatViewModel.selectTag(tag) },
                isFilterActive = isFilterVisible || selectedTag != null,
                showFilterButton = currentTab == CelestiaTab.Chat && currentSessionId == null && currentScreen is AppScreen.Main,
                onBack = onBack,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            if (isMultiSelectMode) {
                // Multi-Select Floating Action Glass Pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(12.dp, shape = RoundedCornerShape(50.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(50.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                androidx.compose.material3.IconButton(onClick = { chatViewModel.clearMultiSelect() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Cancel selection", tint = MaterialTheme.colorScheme.onSurface)
                                }
                                Text(
                                    text = "${selectedSessionIds.size} Selected",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ai.moon.celestia.core.designsystem.component.CelestiaChip(
                                    label = "Select All",
                                    isSelected = selectedSessionIds.size == sessions.size,
                                    onSelect = {
                                        if (selectedSessionIds.size == sessions.size) {
                                            chatViewModel.clearMultiSelect()
                                        } else {
                                            chatViewModel.selectAllSessions()
                                        }
                                    }
                                )

                                androidx.compose.material3.IconButton(
                                    onClick = { chatViewModel.deleteSelectedSessions() },
                                    enabled = selectedSessionIds.isNotEmpty()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete selected",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (currentTab != null && !isInActiveChat) {
                // Bottom Backdrop Scrim (translucent background to transparent gradient)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                                )
                            )
                        )
                )

                CelestiaGlassBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        if (tab == CelestiaTab.Chat) {
                            chatViewModel.closeActiveChat()
                        }
                        currentScreen = AppScreen.Main(tab)
                    },
                    onSearchClick = {
                        if (currentScreen !is AppScreen.Main || (currentScreen as AppScreen.Main).tab != CelestiaTab.Chat) {
                            currentScreen = AppScreen.Main(CelestiaTab.Chat)
                        }
                        chatViewModel.closeActiveChat()
                        chatViewModel.setSearchVisible(true)
                    },
                    onNewChatClick = {
                        if (currentScreen !is AppScreen.Main || (currentScreen as AppScreen.Main).tab != CelestiaTab.Chat) {
                            currentScreen = AppScreen.Main(CelestiaTab.Chat)
                        }
                        chatViewModel.closeActiveChat()
                        chatViewModel.startNewChat()
                    },
                    onStartPreset = { presetId ->
                        if (currentScreen !is AppScreen.Main || (currentScreen as AppScreen.Main).tab != CelestiaTab.Chat) {
                            currentScreen = AppScreen.Main(CelestiaTab.Chat)
                        }
                        chatViewModel.closeActiveChat()
                        when (presetId) {
                            "math_solver" -> chatViewModel.startMathSolverChat()
                            "code_generator" -> chatViewModel.startCodeGeneratorChat()
                            "email" -> chatViewModel.startEmailWriterChat()
                            "grammar" -> chatViewModel.startGrammarAuditChat()
                            else -> chatViewModel.startMathSolverChat()
                        }
                    },
                    isSearchVisible = isSearchVisible,
                    onCloseSearch = handleCloseSearch,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 12.dp)
                )
            }
        }
        
        if (!userPrefs.hasCompletedFirstRun) {
            FirstRunDiscoveryDialog(
                settingsViewModel = settingsViewModel,
                onComplete = {
                    settingsViewModel.completeFirstRun()
                }
            )
        }
    }
}
}
