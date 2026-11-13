package ai.moon.celestia.ui

import ai.moon.celestia.core.designsystem.component.transparentGlassEffect
import ai.moon.celestia.feature.settings.SettingsViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun FirstRunDiscoveryDialog(
    settingsViewModel: SettingsViewModel,
    onComplete: () -> Unit
) {
    val discoveredServers by settingsViewModel.discoveredServers.collectAsState()
    val isSearching by settingsViewModel.isSearchingMdns.collectAsState()
    
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        settingsViewModel.startMdnsScan()
        showContent = true
    }

    Dialog(
        onDismissRequest = { showContent = false },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { showContent = false },
            contentAlignment = Alignment.Center
        ) {
            // Backdrop Scrim
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(150))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                )
            }

            // Dialog Content
            AnimatedVisibility(
                visible = showContent,
                enter = scaleIn(
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = 300f),
                    initialScale = 0.85f
                ) + fadeIn(tween(200)),
                exit = scaleOut(
                    animationSpec = tween(150),
                    targetScale = 0.85f
                ) + fadeOut(tween(150))
            ) {
                DisposableEffect(Unit) {
                    onDispose {
                        if (!showContent) {
                            settingsViewModel.stopMdnsScan()
                            onComplete()
                        }
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .transparentGlassEffect(shape = RoundedCornerShape(24.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* Consume clicks to prevent dismiss */ }
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Welcome to Celestia",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Scanning your local network for active Ollama servers. Please select one to connect.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (discoveredServers.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                if (isSearching) {
                                    CircularProgressIndicator()
                                } else {
                                    Text("No servers found. You can set it manually in Settings.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.heightIn(max = 200.dp)
                            ) {
                                items(discoveredServers) { server ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                settingsViewModel.connectToDiscoveredServer(server)
                                                showContent = false
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(text = server.serviceName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                Text(text = server.httpUrl, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showContent = false }) {
                                Text("Skip / Setup Later")
                            }
                        }
                    }
                }
            }
        }
    }
}
