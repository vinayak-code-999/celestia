package ai.moon.celestia.feature.settings.components

import ai.moon.celestia.core.data.HfDownloadState
import ai.moon.celestia.core.data.HuggingFaceModelInfo
import ai.moon.celestia.core.data.HuggingFaceModelManager
import ai.moon.celestia.core.designsystem.component.CelestiaCard
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HuggingFaceModelManagerCard(
    downloadState: HfDownloadState,
    onDownloadModel: (HuggingFaceModelInfo) -> Unit,
    onDownloadCustomUrl: (String) -> Unit,
    onDeleteModel: (HuggingFaceModelInfo) -> Unit,
    selectedSlmModelId: String,
    onSelectSlmModel: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var customUrlInput by remember { mutableStateOf("") }

    CelestiaCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hugging Face On-Device SLM Manager",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Text(
                text = "Download and manage local Small Language Models (SLMs) from Hugging Face for on-device agent intelligence.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val defaultModel = HuggingFaceModelManager.DEFAULT_SMOL_LM
            val isSelected = selectedSlmModelId == defaultModel.id

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = defaultModel.name,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = defaultModel.repoId,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "Active",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            } else {
                                OutlinedButton(onClick = { onSelectSlmModel(defaultModel.id) }) {
                                    Text("Select")
                                }
                            }

                            Button(
                                onClick = { onDownloadModel(defaultModel) }
                            ) {
                                Text("Download")
                            }

                            OutlinedButton(
                                onClick = { onDeleteModel(defaultModel) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Delete")
                            }
                        }
                    }

                    if (downloadState is HfDownloadState.Downloading && downloadState.modelId == defaultModel.id) {
                        LinearProgressIndicator(
                            progress = { downloadState.progress },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Downloading... ${(downloadState.progress * 100).toInt()}% (${downloadState.downloadedMb}MB / ${downloadState.totalMb}MB)",
                            style = MaterialTheme.typography.labelSmall
                        )
                    } else if (downloadState is HfDownloadState.Success && downloadState.modelId == defaultModel.id) {
                        Text(
                            text = "Downloaded successfully to local storage.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            OutlinedTextField(
                value = customUrlInput,
                onValueChange = { customUrlInput = it },
                label = { Text("Custom Hugging Face GGUF URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    if (customUrlInput.isNotBlank()) {
                        onDownloadCustomUrl(customUrlInput.trim())
                        customUrlInput = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = customUrlInput.isNotBlank()
            ) {
                Text("Download Custom GGUF Model")
            }
        }
    }
}
