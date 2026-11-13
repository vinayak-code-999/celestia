package ai.moon.celestia.core.designsystem.component.navigation

import ai.moon.celestia.core.designsystem.component.CelestiaMascotAvatar
import ai.moon.celestia.core.designsystem.component.CelestiaTransparentGlassPill
import ai.moon.celestia.core.designsystem.component.MascotState
import ai.moon.celestia.core.designsystem.component.applePressScale
import ai.moon.celestia.core.designsystem.component.transparentGlassEffect
import ai.moon.celestia.core.designsystem.util.rememberCelestiaHaptics
import ai.moon.celestia.ui.LocalGlassPreferences
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    val celestiaHaptics = rememberCelestiaHaptics()
    val displayTitle = remember(title) {
        val clean = title.trim()
        if (clean.isBlank() || clean.startsWith("Chat with", ignoreCase = true)) "New Chat" else clean
    }
    val glassPrefs = LocalGlassPreferences.current

    var optionsMenuExpanded by remember { mutableStateOf(false) }
    var filterMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Top-Left Back Button with Liquid Spring Animation (when navigating into Settings or an active chat session)
        AnimatedVisibility(
            visible = onBack != null,
            enter = fadeIn(animationSpec = tween(220)) +
                    expandHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), expandFrom = Alignment.Start) +
                    scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), initialScale = 0.6f),
            exit = fadeOut(animationSpec = tween(180)) +
                   shrinkHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium), shrinkTowards = Alignment.Start) +
                   scaleOut(animationSpec = tween(180), targetScale = 0.6f)
        ) {
            if (onBack != null) {
                CelestiaTransparentGlassPill(
                    modifier = Modifier
                        .size(48.dp)
                        .applePressScale {
                            celestiaHaptics.performActionPop()
                            onBack()
                        },
                    shape = CircleShape,
                    alpha = glassPrefs.alpha
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 2. Compact Title / Search Container with Liquid Animated Morphing
        CelestiaTransparentGlassPill(
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = CircleShape,
            alpha = glassPrefs.alpha
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AnimatedContent(
                    targetState = isSearchMode,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(250)) +
                         scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), initialScale = 0.88f) +
                         expandHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))) togetherWith
                        (fadeOut(animationSpec = tween(200)) +
                         scaleOut(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium), targetScale = 0.88f) +
                         shrinkHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)))
                    },
                    label = "topBarSearchModeTransition",
                    modifier = Modifier.weight(1f)
                ) { searching ->
                    if (searching) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                textStyle = MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp
                                ),
                                modifier = Modifier.weight(1f).padding(start = 4.dp),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search chats...",
                                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            IconButton(
                                onClick = onCloseSearch,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close search",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (showMascot) {
                                    CelestiaMascotAvatar(
                                        state = mascotState,
                                        size = 38.dp,
                                        showLabel = false
                                    )
                                }
                                Text(
                                    text = displayTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        letterSpacing = (-0.2).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }

                            if (tokensPerSecond != null && tokensPerSecond > 0.0) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Speed,
                                            contentDescription = null,
                                            modifier = Modifier.size(11.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "%.1f t/s".format(tokensPerSecond),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            ),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Combined Filter/Settings Menu Button on Right with Bouncy Liquid Entry
        AnimatedVisibility(
            visible = onBack == null && showFilterButton,
            enter = fadeIn(animationSpec = tween(220)) +
                    scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), initialScale = 0.5f) +
                    expandHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), expandFrom = Alignment.End),
            exit = fadeOut(animationSpec = tween(180)) +
                   scaleOut(animationSpec = tween(180), targetScale = 0.5f) +
                   shrinkHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium), shrinkTowards = Alignment.End)
        ) {
            Box {
                CelestiaTransparentGlassPill(
                    modifier = Modifier
                        .size(48.dp)
                        .applePressScale {
                            celestiaHaptics.performActionPop()
                            optionsMenuExpanded = true
                        },
                    shape = CircleShape,
                    alpha = glassPrefs.alpha
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Options & Filter",
                            tint = if (isFilterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Polished Glass Dropdown Menu with Single Surface Elevation
                DropdownMenu(
                    expanded = optionsMenuExpanded,
                    onDismissRequest = { optionsMenuExpanded = false },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = glassPrefs.alpha),
                    tonalElevation = 2.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier.transparentGlassEffect(shape = RoundedCornerShape(20.dp), alpha = glassPrefs.alpha, elevation = 6.dp)
                ) {
                    Text(
                        text = "Options & Settings",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    DropdownMenuItem(
                        text = { Text("Filter chats", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)) },
                        leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        onClick = {
                            optionsMenuExpanded = false
                            filterMenuExpanded = true
                        }
                    )
                    if (onOpenSettings != null) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        DropdownMenuItem(
                            text = { Text("Settings & Profile", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)) },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                optionsMenuExpanded = false
                                onOpenSettings.invoke()
                            }
                        )
                    }
                }

                // Single Surface Tag Filter Selection Menu
                DropdownMenu(
                    expanded = filterMenuExpanded,
                    onDismissRequest = { filterMenuExpanded = false },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = glassPrefs.alpha),
                    tonalElevation = 2.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier.transparentGlassEffect(shape = RoundedCornerShape(20.dp), alpha = glassPrefs.alpha, elevation = 6.dp)
                ) {
                    Text(
                        text = "Filter by Tag",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    DropdownMenuItem(
                        text = { Text("All Chats (Clear Filter)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)) },
                        onClick = {
                            onTagSelected(null)
                            filterMenuExpanded = false
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    if (availableTags.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No tags available", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            onClick = {}
                        )
                    } else {
                        availableTags.forEach { tag ->
                            val isSelected = selectedTag == tag
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(text = "#$tag", style = MaterialTheme.typography.bodyMedium)
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    if (isSelected) {
                                        onTagSelected(null)
                                    } else {
                                        onTagSelected(tag)
                                    }
                                    filterMenuExpanded = false
                                }
                            )
                        }
                    }
                    if (selectedTag != null) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        DropdownMenuItem(
                            text = { Text("Clear All Filters", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)) },
                            onClick = {
                                onTagSelected(null)
                                filterMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
