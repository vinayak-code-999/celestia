package ai.moon.celestia.feature.scanner

import ai.moon.celestia.core.designsystem.component.CelestiaCard
import ai.moon.celestia.core.designsystem.component.CelestiaHardwareErrorChip
import ai.moon.celestia.core.designsystem.component.CelestiaMascotAvatar
import ai.moon.celestia.core.designsystem.component.MascotState
import ai.moon.celestia.feature.scanner.components.ScannerImagePickerCard
import ai.moon.celestia.feature.scanner.components.ScannerResultCard
import ai.moon.celestia.ui.CelestiaTab
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel,
    modifier: Modifier = Modifier,
    onNavigateToTab: (CelestiaTab) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val extractedText by viewModel.extractedText.collectAsState()
    val tokensPerSec by viewModel.tokensPerSec.collectAsState()
    val availableModels by viewModel.availableModels.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val isAutoCropEnabled by viewModel.isAutoCropEnabled.collectAsState()

    val scrollState = rememberScrollState()

    LaunchedEffect(extractedText.length, uiState) {
        if (uiState is ScannerUiState.Processing && extractedText.isNotEmpty()) {
            scrollState.scrollTo(scrollState.maxValue)
        } else if (extractedText.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.processImageUri(it) }
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.processPdfUri(it) }
    }

    val digitalPdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.scanToDigitalPdf(it) }
    }

    val topContentPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 62.dp
    val bottomContentPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 80.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(topContentPadding))

        // Scanner Launcher & Model Selector Card
        ScannerImagePickerCard(
            selectedModel = selectedModel,
            availableModels = availableModels,
            isAutoCropEnabled = isAutoCropEnabled,
            onToggleAutoCrop = { viewModel.toggleAutoCrop() },
            onModelSelected = { viewModel.selectModel(it) },
            onRefreshModels = { viewModel.loadModels() },
            onLaunchImagePicker = { imagePickerLauncher.launch("image/*") },
            onLaunchPdfPicker = { pdfPickerLauncher.launch("application/pdf") },
            onLaunchScanToDigitalPdf = { digitalPdfPickerLauncher.launch("image/*") }
        )

        // Processing Indicator
        if (uiState is ScannerUiState.Processing) {
            val progressText = (uiState as ScannerUiState.Processing).progressText
            CelestiaCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    CelestiaMascotAvatar(
                        state = MascotState.Streaming,
                        size = 42.dp,
                        showLabel = false
                    )
                    Text(
                        text = progressText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Error Display
        if (uiState is ScannerUiState.Error) {
            val errorMsg = (uiState as ScannerUiState.Error).message
            CelestiaHardwareErrorChip(
                message = errorMsg,
                onSwitchModel = {}
            )
        }

        // Output Result Card
        ScannerResultCard(
            extractedText = extractedText,
            tokensPerSec = tokensPerSec,
            onNavigateToTab = onNavigateToTab
        )

        Spacer(modifier = Modifier.height(bottomContentPadding))
    }
}
