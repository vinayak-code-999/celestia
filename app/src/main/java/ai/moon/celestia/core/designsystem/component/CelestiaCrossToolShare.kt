package ai.moon.celestia.core.designsystem.component

import ai.moon.celestia.core.data.SharedContentBus
import ai.moon.celestia.core.designsystem.util.rememberCelestiaHaptics
import ai.moon.celestia.core.util.DocumentExporter
import ai.moon.celestia.ui.CelestiaTab
import ai.moon.celestia.ui.LocalGlassPreferences
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

data class ShareTargetOption(
    val tab: CelestiaTab,
    val title: String,
    val icon: ImageVector,
    val actionPrompt: String? = null
)

@Composable
fun CelestiaShareSaveButton(
    contentToShare: String,
    modifier: Modifier = Modifier,
    documentTitle: String = "Celestia_Export"
) {
    val context = LocalContext.current
    val haptics = rememberCelestiaHaptics()
    val clipboardManager = LocalClipboardManager.current
    val glassPrefs = LocalGlassPreferences.current
    val documentExporter = remember(context) { DocumentExporter(context) }

    var menuExpanded by remember { mutableStateOf(false) }

    if (contentToShare.isBlank()) return

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    haptics.performActionPop()
                    menuExpanded = true
                },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share & Save",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Share & Save",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = glassPrefs.alpha.coerceAtLeast(0.85f)),
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.transparentGlassEffect(shape = RoundedCornerShape(16.dp), alpha = glassPrefs.alpha, elevation = 6.dp)
        ) {
            Text(
                text = "Export & Share Options",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            DropdownMenuItem(
                text = { Text("Export as PDF", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)) },
                leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                onClick = {
                    menuExpanded = false
                    haptics.performActionSuccess()
                    val pdfFile = documentExporter.exportToPdf(documentTitle, contentToShare)
                    if (pdfFile != null) {
                        documentExporter.shareDocumentFile(pdfFile, "application/pdf")
                    }
                }
            )

            DropdownMenuItem(
                text = { Text("Export as Word (.doc)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)) },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Article, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                onClick = {
                    menuExpanded = false
                    haptics.performActionSuccess()
                    val docFile = documentExporter.exportToWordDoc(documentTitle, contentToShare)
                    if (docFile != null) {
                        documentExporter.shareDocumentFile(docFile, "application/msword")
                    }
                }
            )

            DropdownMenuItem(
                text = { Text("Export as Markdown (.md)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)) },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                onClick = {
                    menuExpanded = false
                    haptics.performActionSuccess()
                    val mdFile = documentExporter.exportToMarkdown(documentTitle, contentToShare)
                    if (mdFile != null) {
                        documentExporter.shareDocumentFile(mdFile, "text/markdown")
                    }
                }
            )

            DropdownMenuItem(
                text = { Text("Export as Plain Text (.txt)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)) },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.TextSnippet, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                onClick = {
                    menuExpanded = false
                    haptics.performActionSuccess()
                    val txtFile = File(context.cacheDir, "${documentTitle.replace(Regex("[^a-zA-Z0-9_]"), "_")}.txt").apply {
                        writeText(contentToShare, Charsets.UTF_8)
                    }
                    documentExporter.shareDocumentFile(txtFile, "text/plain")
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            DropdownMenuItem(
                text = { Text("Share via Apps...", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)) },
                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                onClick = {
                    menuExpanded = false
                    haptics.performActionSuccess()
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, contentToShare)
                    }
                    val chooser = Intent.createChooser(intent, "Share Content")
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooser)
                }
            )

            DropdownMenuItem(
                text = { Text("Copy Text", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)) },
                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                onClick = {
                    menuExpanded = false
                    haptics.performActionSuccess()
                    clipboardManager.setText(AnnotatedString(contentToShare))
                }
            )
        }
    }
}

@Composable
fun CelestiaSendToActionBar(
    currentToolName: String,
    contentToShare: String,
    modifier: Modifier = Modifier,
    onNavigateToTab: (CelestiaTab) -> Unit = {}
) {
    val haptics = rememberCelestiaHaptics()

    val options = remember(currentToolName) {
        listOf(
            ShareTargetOption(CelestiaTab.Chat, "Chat", Icons.Default.ChatBubbleOutline, "Analyze this content from $currentToolName:"),
            ShareTargetOption(CelestiaTab.Summarize, "Summarize", Icons.Default.Summarize, "Summarize this output:"),
            ShareTargetOption(CelestiaTab.Translate, "Translate", Icons.Default.Translate),
            ShareTargetOption(CelestiaTab.Workbench, "Workbench", Icons.Default.AutoAwesome)
        ).filter { it.title.lowercase() != currentToolName.lowercase() }
    }

    if (contentToShare.isBlank()) return

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        item(key = "share_save_button") {
            CelestiaShareSaveButton(
                contentToShare = contentToShare,
                documentTitle = "${currentToolName}_Output"
            )
        }

        item(key = "copy_button") {
            CelestiaCopyButton(textToCopy = contentToShare)
        }

        items(options, key = { it.title }) { option ->
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        haptics.performActionSuccess()
                        SharedContentBus.shareContent(
                            sourceTool = currentToolName,
                            content = contentToShare,
                            targetTab = option.tab,
                            actionPrompt = option.actionPrompt
                        )
                        onNavigateToTab(option.tab)
                    },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = option.title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = option.title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

