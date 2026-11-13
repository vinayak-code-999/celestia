package ai.moon.celestia.feature.settings.components

import ai.moon.celestia.core.designsystem.animation.bounceClickable
import ai.moon.celestia.core.designsystem.component.CelestiaButton
import ai.moon.celestia.core.designsystem.component.CelestiaCard
import ai.moon.celestia.core.designsystem.component.CelestiaChip
import ai.moon.celestia.core.designsystem.util.rememberCelestiaHaptics
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ProfileAndAppearanceCard(
    userAvatar: String,
    onUpdateUserAvatar: (String) -> Unit,
    themeMode: String,
    onUpdateThemeMode: (String) -> Unit,
    isDarkMode: Boolean,
    onUpdateDarkMode: (Boolean) -> Unit,
    glassPreset: String,
    onApplyGlassPreset: (String) -> Unit,
    glassAlpha: Float,
    onUpdateGlassAlpha: (Float) -> Unit,
    glassShineIntensity: Float,
    onUpdateGlassShineIntensity: (Float) -> Unit,
    isHapticsEnabled: Boolean,
    onUpdateHapticsEnabled: (Boolean) -> Unit,
    onOpenDesignGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val celestiaHaptics = rememberCelestiaHaptics()

    val avatars = listOf(
        "Person" to Icons.Default.Person,
        "AutoAwesome" to Icons.Default.AutoAwesome,
        "Face" to Icons.Default.Face,
        "Star" to Icons.Default.Star,
        "Psychology" to Icons.Default.Psychology,
        "Rocket" to Icons.Default.Rocket
    )

    CelestiaCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "Profile & Appearance",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            // Avatar Picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "User Avatar",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    avatars.forEach { (avatarKey, icon) ->
                        val isSelected = userAvatar == avatarKey
                        Surface(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .bounceClickable {
                                    celestiaHaptics.performActionPop()
                                    onUpdateUserAvatar(avatarKey)
                                },
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = icon, contentDescription = avatarKey, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }
            }

            // Theme Mode & Dark Mode
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Theme Mode",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("System", "Dark", "Light").forEach { mode ->
                        CelestiaChip(
                            label = mode,
                            isSelected = themeMode == mode,
                            onSelect = {
                                celestiaHaptics.performActionPop()
                                onUpdateThemeMode(mode)
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Force Dark Mode",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Override system color scheme to always use dark palette",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { checked ->
                            celestiaHaptics.performActionPop()
                            onUpdateDarkMode(checked)
                        }
                    )
                }
            }

            // Glassmorphism Settings
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Glassmorphism & Opacity",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Glass Presets
                Text(
                    text = "Glass Presets",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("PeachWarm", "DarkSmoked", "PillSearch", "FrostedCrystal")) { preset ->
                        CelestiaChip(
                            label = preset,
                            isSelected = glassPreset == preset,
                            onSelect = {
                                celestiaHaptics.performActionPop()
                                onApplyGlassPreset(preset)
                            }
                        )
                    }
                }

                // Glass Surface Opacity Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🎨 Surface Opacity (Alpha)", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = String.format(java.util.Locale.US, "%.2f", glassAlpha),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                    }
                    Slider(
                        value = glassAlpha,
                        onValueChange = { onUpdateGlassAlpha(it) },
                        valueRange = 0.05f..1.0f
                    )
                }

                // Shine Border Highlight Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("✨ Shine Border Highlight", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = String.format(java.util.Locale.US, "%.2f", glassShineIntensity),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                    }
                    Slider(
                        value = glassShineIntensity,
                        onValueChange = { onUpdateGlassShineIntensity(it) },
                        valueRange = 0.1f..1.0f
                    )
                }
            }

            // Haptics Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Haptic Feedback",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Enable sensory tactile vibration feedback on interactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = isHapticsEnabled,
                    onCheckedChange = { checked ->
                        celestiaHaptics.performActionPop()
                        onUpdateHapticsEnabled(checked)
                    }
                )
            }

            // Design Guide Button
            CelestiaButton(
                text = "Open Design System Guide & Program",
                onClick = {
                    celestiaHaptics.performActionPop()
                    onOpenDesignGuide()
                },
                icon = Icons.Default.Palette,
                containerColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
