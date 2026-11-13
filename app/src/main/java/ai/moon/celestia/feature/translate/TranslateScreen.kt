package ai.moon.celestia.feature.translate

import ai.moon.celestia.core.data.SharedContentBus
import ai.moon.celestia.core.designsystem.component.CelestiaHardwareErrorChip
import ai.moon.celestia.feature.translate.components.LanguageSelectorCard
import ai.moon.celestia.feature.translate.components.TranslateInputCard
import ai.moon.celestia.feature.translate.components.TranslateOutputCard
import ai.moon.celestia.ui.CelestiaTab
import android.speech.tts.TextToSpeech
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun TranslateScreen(
    viewModel: TranslateViewModel,
    modifier: Modifier = Modifier,
    onNavigateToTab: (CelestiaTab) -> Unit = {}
) {
    val context = LocalContext.current
    val sourceLang by viewModel.sourceLang.collectAsState()
    val targetLang by viewModel.targetLang.collectAsState()
    val selectedTone by viewModel.selectedTone.collectAsState()
    val translationOutput by viewModel.translationOutput.collectAsState()
    val isTranslating by viewModel.isTranslating.collectAsState()
    val tokensPerSec by viewModel.tokensPerSec.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val availableModels by viewModel.availableModels.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsEngine?.language = Locale.getDefault()
            }
        }
        ttsEngine = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    LaunchedEffect(translationOutput.length, isTranslating) {
        if (isTranslating && translationOutput.isNotEmpty()) {
            scrollState.scrollTo(scrollState.maxValue)
        } else if (translationOutput.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    LaunchedEffect(Unit) {
        val payload = SharedContentBus.consumePayload()
        if (payload != null && payload.targetTab == CelestiaTab.Translate) {
            inputText = payload.content
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

        // Language & Model Configuration Card
        LanguageSelectorCard(
            selectedModel = selectedModel,
            availableModels = availableModels,
            onModelSelected = { viewModel.selectModel(it) },
            onRefreshModels = { viewModel.loadAvailableModels() },
            sourceLang = sourceLang,
            targetLang = targetLang,
            availableLanguages = viewModel.availableLanguages,
            onSwapLanguages = { viewModel.swapLanguages() },
            onSelectTargetLang = { viewModel.selectTargetLang(it) },
            selectedTone = selectedTone,
            availableTones = viewModel.availableTones,
            onSelectTone = { viewModel.selectTone(it) }
        )

        // Translation Input Card
        TranslateInputCard(
            sourceLang = sourceLang,
            targetLang = targetLang,
            inputText = inputText,
            onInputTextChange = { inputText = it },
            isListening = isListening,
            onToggleListening = { isListening = !isListening },
            onClearInput = { inputText = "" },
            isTranslating = isTranslating,
            onTranslate = { viewModel.translateText(inputText) },
            showQuickPrompts = inputText.isEmpty() && translationOutput.isEmpty(),
            onSelectQuickPrompt = { prompt ->
                inputText = "Translate this draft to $targetLang in $selectedTone tone: $prompt"
            }
        )

        // Error Display
        if (errorMessage != null) {
            CelestiaHardwareErrorChip(
                message = errorMessage ?: "Error generating translation",
                onSwitchModel = { viewModel.clearError() }
            )
        }

        // Translation Output Card
        TranslateOutputCard(
            targetLang = targetLang,
            selectedTone = selectedTone,
            translationOutput = translationOutput,
            isTranslating = isTranslating,
            tokensPerSec = tokensPerSec,
            onReadAloud = {
                if (translationOutput.isNotBlank()) {
                    ttsEngine?.speak(translationOutput, TextToSpeech.QUEUE_FLUSH, null, "TranslationSpeech")
                }
            },
            onNavigateToTab = onNavigateToTab
        )

        Spacer(modifier = Modifier.height(bottomContentPadding))
    }
}
