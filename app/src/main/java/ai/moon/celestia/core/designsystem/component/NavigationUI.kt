package ai.moon.celestia.core.designsystem.component

import ai.moon.celestia.core.designsystem.component.navigation.CelestiaGlassBottomBar as NavigationGlassBottomBar
import ai.moon.celestia.core.designsystem.component.navigation.CelestiaTopBar as NavigationTopBar
import ai.moon.celestia.core.designsystem.component.navigation.glassPressScale as navigationGlassPressScale
import ai.moon.celestia.ui.CelestiaTab
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CelestiaTopBar(
    title: String,
    modifier: Modifier = Modifier,
    isConnected: Boolean = true,
    mascotState: MascotState = MascotState.Idle,
    tokensPerSecond: Double? = null,
    showMascot: Boolean = true,
    isSearchMode: Boolean = false,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onCloseSearch: () -> Unit = {},
    onOpenSettings: (() -> Unit)? = null,
    availableTags: List<String> = emptyList(),
    selectedTag: String? = null,
    onTagSelected: (String?) -> Unit = {},
    isFilterActive: Boolean = false,
    showFilterButton: Boolean = true,
    onBack: (() -> Unit)? = null
) {
    NavigationTopBar(
        title = title,
        modifier = modifier,
        isConnected = isConnected,
        mascotState = mascotState,
        tokensPerSecond = tokensPerSecond,
        showMascot = showMascot,
        isSearchMode = isSearchMode,
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        onCloseSearch = onCloseSearch,
        onOpenSettings = onOpenSettings,
        availableTags = availableTags,
        selectedTag = selectedTag,
        onTagSelected = onTagSelected,
        isFilterActive = isFilterActive,
        showFilterButton = showFilterButton,
        onBack = onBack
    )
}

@Composable
fun CelestiaGlassBottomBar(
    currentTab: CelestiaTab,
    onTabSelected: (CelestiaTab) -> Unit,
    modifier: Modifier = Modifier,
    onSearchClick: (() -> Unit)? = null,
    onNewChatClick: (() -> Unit)? = null,
    onStartPreset: ((String) -> Unit)? = null,
    isSearchVisible: Boolean = false,
    onCloseSearch: (() -> Unit)? = null
) {
    NavigationGlassBottomBar(
        currentTab = currentTab,
        onTabSelected = onTabSelected,
        modifier = modifier,
        onSearchClick = onSearchClick,
        onNewChatClick = onNewChatClick,
        onStartPreset = onStartPreset,
        isSearchVisible = isSearchVisible,
        onCloseSearch = onCloseSearch
    )
}

fun Modifier.glassPressScale(onClick: () -> Unit): Modifier = this.navigationGlassPressScale(onClick)
