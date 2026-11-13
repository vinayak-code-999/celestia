package ai.moon.celestia.feature.designguide

import ai.moon.celestia.core.designsystem.animation.bounceClickable
import ai.moon.celestia.core.designsystem.animation.pulsingGlow
import ai.moon.celestia.core.designsystem.component.AiOrbAvatar
import ai.moon.celestia.core.designsystem.component.AiState
import ai.moon.celestia.core.designsystem.component.CelestiaButton
import ai.moon.celestia.core.designsystem.component.CelestiaCard
import ai.moon.celestia.core.designsystem.component.CelestiaChip
import ai.moon.celestia.core.designsystem.component.CelestiaHardwareErrorChip
import ai.moon.celestia.core.designsystem.component.CelestiaTextField
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GuidingDesignGuideScreen(
    modifier: Modifier = Modifier
) {
    var selectedChip by remember { mutableStateOf("M3 Expressive") }
    var bounceCount by remember { mutableIntStateOf(0) }
    var sampleInput by remember { mutableStateOf("") }

    val topContentPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 72.dp
    val bottomContentPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 100.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(topContentPadding))

        // Section 1: Color Tokens
        CelestiaCard {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cosmic Color Palette Tokens",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ColorSwatch("Primary", MaterialTheme.colorScheme.primary)
                    ColorSwatch("Secondary", MaterialTheme.colorScheme.secondary)
                    ColorSwatch("Tertiary", MaterialTheme.colorScheme.tertiary)
                    ColorSwatch("Surface", MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }

        // Section 2: Motion Engine & Spring Physics Sandbox
        CelestiaCard {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bouncy Spring Physics Playground",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap the button below to feel tactile spring scale dynamics on press/release.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CelestiaButton(
                        text = "Tap Bouncy Button ($bounceCount)",
                        onClick = { bounceCount++ },
                        icon = Icons.Default.AutoAwesome
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .pulsingGlow(enabled = true)
                    ) {
                        Text(
                            text = "AI Pulsing Glow",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        // Section 3: Reusable Component Library
        CelestiaCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Unified Component Gallery",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                // Chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("M3 Expressive", "Adaptive Rail", "Spring Motion", "Ollama Stream").forEach { label ->
                        CelestiaChip(
                            label = label,
                            isSelected = selectedChip == label,
                            onSelect = { selectedChip = label }
                        )
                    }
                }

                // Input Field
                CelestiaTextField(
                    value = sampleInput,
                    onValueChange = { sampleInput = it },
                    placeholder = "Type a message to test input styling...",
                    onSend = {}
                )
            }
        }

        // Section 4: 3D Gradient Orb Copilot Avatar Component
        CelestiaCard {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "3D Gradient Orb Copilot Avatar",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Text(
                    text = "A modern Jetpack Compose Canvas based glowing 3D orb that reacts smoothly to AI state changes.",
                    style = MaterialTheme.typography.bodyMedium
                )

                var currentOrbState by remember { mutableStateOf(AiState.IDLE) }

                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AiOrbAvatar(
                        state = currentOrbState,
                        modifier = Modifier.size(160.dp)
                    )
                }

                // State Selector Chips for Orb
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val states = AiState.entries
                    states.chunked(3).forEach { rowStates ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
                        ) {
                            rowStates.forEach { state ->
                                CelestiaChip(
                                    label = state.name,
                                    isSelected = currentOrbState == state,
                                    onSelect = { currentOrbState = state }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 5: Hardware Error Handling Chip
        CelestiaCard {
            Column {
                Text(
                    text = "Graceful Host Memory Error Alert",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                CelestiaHardwareErrorChip(
                    message = "Host machine ran out of VRAM/RAM trying to load 'llama3:70b'. Try selecting a smaller model like Gemma 2B or Phi-3.",
                    onSwitchModel = {}
                )
            }
        }

        Spacer(modifier = Modifier.height(bottomContentPadding))
    }
}

@Composable
private fun ColorSwatch(
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp
        )
    }
}
