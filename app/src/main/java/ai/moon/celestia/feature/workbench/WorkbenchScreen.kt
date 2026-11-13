package ai.moon.celestia.feature.workbench

import ai.moon.celestia.core.data.AiFunctionScope
import ai.moon.celestia.core.data.SharedContentBus
import ai.moon.celestia.core.designsystem.component.CelestiaCard
import ai.moon.celestia.core.designsystem.component.CelestiaHardwareErrorChip
import ai.moon.celestia.core.designsystem.component.ModelSelectorChip
import ai.moon.celestia.feature.workbench.components.WorkflowSelectionGrid
import ai.moon.celestia.feature.workbench.components.WorkbenchAdvancedParamsCard
import ai.moon.celestia.feature.workbench.components.WorkbenchInputCard
import ai.moon.celestia.feature.workbench.components.WorkbenchOutputCard
import ai.moon.celestia.ui.CelestiaTab
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun WorkbenchScreen(
    viewModel: WorkbenchViewModel,
    modifier: Modifier = Modifier,
    onStartWorkflowChat: (String) -> Unit = {},
    onNavigateToTab: (CelestiaTab) -> Unit = {}
) {
    val selectedPreset by viewModel.selectedPreset.collectAsState()
    val taskOutput by viewModel.taskOutput.collectAsState()
    val isExecuting by viewModel.isExecuting.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val availableModels by viewModel.availableModels.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()

    val temperature by viewModel.temperature.collectAsState()
    val topP by viewModel.topP.collectAsState()
    val repetitionPenalty by viewModel.repetitionPenalty.collectAsState()
    val customSystemPrompt by viewModel.customSystemPrompt.collectAsState()
    val tokensPerSec by viewModel.tokensPerSec.collectAsState()

    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val recognizedText by viewModel.voiceManager.recognizedText.collectAsState()

    var inputContent by remember { mutableStateOf("") }
    var initialInputContent by remember { mutableStateOf("") }
    var showAdvancedParams by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.voiceManager.startListening()
        }
    }

    LaunchedEffect(isListening) {
        if (isListening) {
            initialInputContent = inputContent
        }
    }

    LaunchedEffect(Unit) {
        val payload = SharedContentBus.consumePayload()
        if (payload != null && payload.targetTab == CelestiaTab.Workbench) {
            inputContent = payload.content
        }
    }

    val scrollState = rememberScrollState()

    LaunchedEffect(taskOutput.length, isExecuting) {
        if (isExecuting && taskOutput.isNotEmpty()) {
            scrollState.scrollTo(scrollState.maxValue)
        } else if (taskOutput.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    LaunchedEffect(recognizedText) {
        if (recognizedText.isNotBlank()) {
            inputContent = if (initialInputContent.isBlank()) recognizedText else "$initialInputContent $recognizedText"
        }
    }

    val topContentPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 62.dp
    val bottomContentPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 80.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(topContentPadding))

        // Curated Top Header Card (Model + Workbench Title)
        CelestiaCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI Code Lab & Workbench",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Execute specialized coding and logic workflows",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                ModelSelectorChip(
                    scope = AiFunctionScope.Workbench,
                    selectedModel = selectedModel,
                    availableModels = availableModels,
                    onModelSelected = { viewModel.selectModel(it) },
                    onRefreshModels = { viewModel.loadAvailableModels() }
                )
            }
        }

        // 1. Workflow Selection Grid
        WorkflowSelectionGrid(
            presets = viewModel.presets,
            selectedPreset = selectedPreset,
            onSelectPreset = { viewModel.selectPreset(it) },
            onStartWorkflowChat = onStartWorkflowChat
        )

        // 2. Input Content Box & Run Tool Button
        WorkbenchInputCard(
            selectedPresetTitle = selectedPreset.title,
            inputContent = inputContent,
            onInputChange = { inputContent = it },
            isListening = isListening,
            isExecuting = isExecuting,
            onToggleListening = {
                if (isListening) {
                    viewModel.voiceManager.stopListening()
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            onClearInput = { inputContent = "" },
            onExecuteTask = { viewModel.executeTask(inputContent) }
        )

        // Error Display
        if (errorMessage != null) {
            CelestiaHardwareErrorChip(
                message = errorMessage ?: "Error executing workbench workflow",
                onSwitchModel = { viewModel.clearError() }
            )
        }

        // 3. Output Result Card
        WorkbenchOutputCard(
            selectedPresetTitle = selectedPreset.title,
            taskOutput = taskOutput,
            isExecuting = isExecuting,
            tokensPerSec = tokensPerSec,
            onNavigateToTab = onNavigateToTab
        )

        // 4. Advanced Model Parameters Card (Collapsible)
        WorkbenchAdvancedParamsCard(
            showAdvancedParams = showAdvancedParams,
            onToggleShowAdvancedParams = { showAdvancedParams = !showAdvancedParams },
            customSystemPrompt = customSystemPrompt,
            onCustomSystemPromptChange = { viewModel.setCustomSystemPrompt(it) },
            temperature = temperature,
            onTemperatureChange = { viewModel.setTemperature(it) },
            topP = topP,
            onTopPChange = { viewModel.setTopP(it) },
            repetitionPenalty = repetitionPenalty,
            onRepetitionPenaltyChange = { viewModel.setRepetitionPenalty(it) }
        )

        Spacer(modifier = Modifier.height(bottomContentPadding))
    }
}
