package ai.moon.celestia.core.designsystem.component.navigation

import ai.moon.celestia.core.designsystem.component.CelestiaTransparentGlassPill
import ai.moon.celestia.core.designsystem.component.applePressScale
import ai.moon.celestia.core.designsystem.component.transparentGlassEffect
import ai.moon.celestia.core.designsystem.util.rememberCelestiaHaptics
import ai.moon.celestia.ui.CelestiaTab
import ai.moon.celestia.ui.LocalGlassPreferences
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

fun Modifier.glassPressScale(onClick: () -> Unit): Modifier = this.composed {
    var isPressed by remember { mutableStateOf(value = false) }
    val scale = animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "glassPressScale",
    )

    this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    try {
                        awaitRelease()
                    } finally {
                        isPressed = false
                    }
                },
                onTap = {
                    onClick()
                },
            )
        }
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
    val pillShape = RoundedCornerShape(50.dp)
    val glassPrefs = LocalGlassPreferences.current
    val celestiaHaptics = rememberCelestiaHaptics()
    val isChatTab = currentTab == CelestiaTab.Chat
    var quickPresetsExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Left Compact Floating Glass Pill (Shadow & Glass Effect)
        CelestiaTransparentGlassPill(
            modifier = Modifier
                .height(56.dp),
            shape = pillShape
        ) {
            val activeXOffset by animateDpAsState(
                targetValue = if (currentTab == CelestiaTab.Hub) 0.dp else 54.dp,
                animationSpec = spring(
                    dampingRatio = 0.9f,
                    stiffness = 350f
                ),
                label = "liquidIndicatorOffset"
            )

            Box(
                modifier = Modifier
                    .height(56.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Morphing Liquid Spring Highlight Container
                Box(
                    modifier = Modifier
                        .offset(x = activeXOffset)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f))
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1a. Hub Tab Button (Icon Only)
                    BottomBarIconItem(
                        icon = CelestiaTab.Hub.icon,
                        contentDescription = "Hub",
                        isSelected = currentTab == CelestiaTab.Hub,
                        onClick = { onTabSelected(CelestiaTab.Hub) }
                    )

                    // 1b. Shared Button in a Pill with Morphing Icon Animation (Icon Only)
                    val (sharedIcon, sharedDesc, sharedAction) = when {
                        isSearchVisible -> Triple(Icons.Default.Close, "Close Search", { onCloseSearch?.invoke() })
                        isChatTab -> Triple(Icons.Default.Search, "Search", { onSearchClick?.invoke() })
                        else -> Triple(CelestiaTab.Chat.icon, "Chat", { onTabSelected(CelestiaTab.Chat) })
                    }

                    BottomBarAnimatedIconItem(
                        icon = sharedIcon,
                        contentDescription = sharedDesc,
                        isSelected = isChatTab || isSearchVisible,
                        onClick = { sharedAction() }
                    )
                }
            }
        }

        // 2. Right Circular Glass Button for New Chat (Transitions IN/OUT with Chat Screen Only)
        AnimatedVisibility(
            visible = isChatTab && onNewChatClick != null,
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
                        .size(56.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    celestiaHaptics.performActionPop()
                                    onNewChatClick?.invoke()
                                },
                                onLongPress = {
                                    celestiaHaptics.performActionPop()
                                    quickPresetsExpanded = true
                                }
                            )
                        },
                    shape = CircleShape
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Chat",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = quickPresetsExpanded,
                    onDismissRequest = { quickPresetsExpanded = false },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = glassPrefs.alpha),
                    tonalElevation = 2.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier.transparentGlassEffect(shape = RoundedCornerShape(20.dp), alpha = glassPrefs.alpha, elevation = 6.dp)
                ) {
                    Text(
                        text = "Quick Start Preset",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    DropdownMenuItem(
                        text = { Text("🧮 Math Solver", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)) },
                        onClick = {
                            quickPresetsExpanded = false
                            onStartPreset?.invoke("math_solver")
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))

                    DropdownMenuItem(
                        text = { Text("💻 Code Generator", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)) },
                        onClick = {
                            quickPresetsExpanded = false
                            onStartPreset?.invoke("code_generator")
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))

                    DropdownMenuItem(
                        text = { Text("✍️ Email Assistant", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)) },
                        onClick = {
                            quickPresetsExpanded = false
                            onStartPreset?.invoke("email")
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))

                    DropdownMenuItem(
                        text = { Text("📝 Grammar Audit", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)) },
                        onClick = {
                            quickPresetsExpanded = false
                            onStartPreset?.invoke("grammar")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomBarIconItem(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val celestiaHaptics = rememberCelestiaHaptics()
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .applePressScale {
                celestiaHaptics.performActionPop()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun BottomBarAnimatedIconItem(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val celestiaHaptics = rememberCelestiaHaptics()
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .applePressScale {
                celestiaHaptics.performActionPop()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = icon to contentDescription,
            transitionSpec = {
                (fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.8f)) togetherWith
                (fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 0.8f))
            },
            label = "bottomBarIconMorph"
        ) { (iconVector, iconDesc) ->
            Icon(
                imageVector = iconVector,
                contentDescription = iconDesc,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
