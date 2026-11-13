package ai.moon.celestia.feature.summarize

import ai.moon.celestia.core.data.SharedContentBus
import ai.moon.celestia.core.designsystem.component.CelestiaHardwareErrorChip
import ai.moon.celestia.feature.summarize.components.SummarizeInputCard
import ai.moon.celestia.feature.summarize.components.SummarizeOutputCard
import ai.moon.celestia.ui.CelestiaTab
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SummarizeScreen(
    viewModel: SummarizeViewModel,
    modifier: Modifier = Modifier,
    onNavigateToTab: (CelestiaTab) -> Unit = {}
) {
    val summaryOutput by viewModel.summaryOutput.collectAsState()
    val selectedStyle by viewModel.selectedStyle.collectAsState()
    val isSummarizing by viewModel.isSummarizing.collectAsState()
    val tokensPerSec by viewModel.tokensPerSec.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val availableModels by viewModel.availableModels.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()

    var sourceText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    LaunchedEffect(summaryOutput.length, isSummarizing) {
        if (isSummarizing && summaryOutput.isNotEmpty()) {
            scrollState.scrollTo(scrollState.maxValue)
        } else if (summaryOutput.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    LaunchedEffect(Unit) {
        val payload = SharedContentBus.consumePayload()
        if (payload != null && payload.targetTab == CelestiaTab.Summarize) {
            sourceText = payload.content
        }
    }

    val topContentPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 62.dp
    val bottomContentPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 80.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(topContentPadding))

        // Input Card (Model Selector, Style Chips, Quick Prompts, Source Text, Summarize Button)
        SummarizeInputCard(
            selectedModel = selectedModel,
            availableModels = availableModels,
            onModelSelected = { viewModel.selectModel(it) },
            onRefreshModels = { viewModel.loadAvailableModels() },
            selectedStyle = selectedStyle,
            availableStyles = viewModel.availableStyles,
            onSelectStyle = { viewModel.selectStyle(it) },
            sourceText = sourceText,
            onSourceTextChange = { sourceText = it },
            isSummarizing = isSummarizing,
            showQuickStarters = sourceText.isEmpty() && summaryOutput.isEmpty(),
            onSelectStarterPrompt = { prompt ->
                sourceText = "Please summarize this text into $selectedStyle:\n$prompt"
            },
            onSummarize = { viewModel.generateSummary(sourceText) }
        )

        // Error Display
        if (errorMessage != null) {
            CelestiaHardwareErrorChip(
                message = errorMessage ?: "Error generating summary",
                onSwitchModel = { viewModel.clearError() }
            )
        }

        // Output Result Card
        SummarizeOutputCard(
            selectedStyle = selectedStyle,
            summaryOutput = summaryOutput,
            isSummarizing = isSummarizing,
            tokensPerSec = tokensPerSec,
            onNavigateToTab = onNavigateToTab
        )

        Spacer(modifier = Modifier.height(bottomContentPadding))
    }
}
