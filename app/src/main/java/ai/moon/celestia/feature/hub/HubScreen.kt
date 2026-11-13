package ai.moon.celestia.feature.hub

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import ai.moon.celestia.core.data.AiFunctionScope
import ai.moon.celestia.core.data.OllamaRepository
import ai.moon.celestia.core.datastore.UserPreferences
import ai.moon.celestia.core.datastore.UserPreferencesRepository
import ai.moon.celestia.core.model.OllamaChatMessage
import ai.moon.celestia.core.model.OllamaModelItem
import ai.moon.celestia.core.util.VoiceManager
import ai.moon.celestia.feature.hub.components.HubHeaderCard
import ai.moon.celestia.feature.hub.components.HubToolsGrid
import ai.moon.celestia.feature.voice.LiveVoiceModeOverlay
import ai.moon.celestia.ui.CelestiaTab
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HubScreen(
    availableModels: List<OllamaModelItem>,
    chatModel: String,
    ocrModel: String,
    summarizeModel: String,
    translateModel: String,
    workbenchModel: String,
    onSelectModelForScope: (AiFunctionScope, String) -> Unit,
    onRefreshModels: () -> Unit,
    onNavigateToTab: (CelestiaTab) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val voiceManager = remember { VoiceManager(context) }
    val isListening by voiceManager.isListening.collectAsState()
    val isSpeaking by voiceManager.isSpeaking.collectAsState()
    val recognizedText by voiceManager.recognizedText.collectAsState()
    val audioRms by voiceManager.audioRms.collectAsState()

    val repository = remember { OllamaRepository() }
    val userPrefsRepo = remember { UserPreferencesRepository(context) }
    val userPrefs by userPrefsRepo.userPreferencesFlow.collectAsState(initial = UserPreferences())
    val coroutineScope = rememberCoroutineScope()

    var showLiveVoiceMode by remember { mutableStateOf(false) }
    var liveAiResponseText by remember { mutableStateOf("") }
    var lastProcessedPrompt by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            voiceManager.startListening()
        }
    }

    // Auto-restart listening after TTS finishes speaking
    LaunchedEffect(Unit) {
        voiceManager.onSpeechDoneListener = {
            if (showLiveVoiceMode && !voiceManager.isListening.value) {
                voiceManager.startListening()
            }
        }
    }

    // Process recognized speech prompt & stream AI response with spoken audio
    LaunchedEffect(isListening, recognizedText) {
        if (!isListening && recognizedText.isNotBlank() && recognizedText != lastProcessedPrompt && showLiveVoiceMode) {
            lastProcessedPrompt = recognizedText
            liveAiResponseText = "Thinking..."

            coroutineScope.launch(Dispatchers.IO) {
                val responseSb = StringBuilder()
                val systemPrompt = "You are Celestia Live Voice Assistant. Keep answers brief, conversational, and direct (1 to 3 sentences maximum)."
                val chatMessage = OllamaChatMessage(role = "user", content = recognizedText)

                try {
                    repository.streamChat(
                        hostUrl = userPrefs.hostUrl,
                        model = chatModel,
                        messages = listOf(OllamaChatMessage(role = "system", content = systemPrompt), chatMessage),
                        keepAlive = "15m"
                    ).collect { chunk ->
                        val token = chunk.message?.content.orEmpty()
                        if (token.isNotEmpty()) {
                            responseSb.append(token)
                            liveAiResponseText = responseSb.toString()
                        }
                    }

                    val finalAnswer = responseSb.toString()
                    if (finalAnswer.isNotBlank()) {
                        withContext(Dispatchers.Main) {
                            voiceManager.speak(finalAnswer)
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        liveAiResponseText = "Connection error: ${e.message}"
                        voiceManager.speak("Sorry, I encountered an error connecting to the AI model.")
                    }
                }
            }
        }
    }

    val topContentPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 62.dp
    val bottomContentPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 80.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(topContentPadding))

        // Hero Header & Server / Settings Card
        HubHeaderCard(
            activeModel = chatModel,
            onOpenSettings = onOpenSettings
        )

        // Tools Navigation & Model Selector Grid/List
        HubToolsGrid(
            chatModel = chatModel,
            ocrModel = ocrModel,
            summarizeModel = summarizeModel,
            translateModel = translateModel,
            workbenchModel = workbenchModel,
            availableModels = availableModels,
            onSelectModelForScope = onSelectModelForScope,
            onRefreshModels = onRefreshModels,
            onNavigateToTab = onNavigateToTab,
            onOpenLiveVoice = {
                showLiveVoiceMode = true
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        )

        Spacer(modifier = Modifier.height(bottomContentPadding))
    }

    LiveVoiceModeOverlay(
        isVisible = showLiveVoiceMode,
        isListening = isListening,
        isSpeaking = isSpeaking,
        recognizedText = recognizedText,
        aiResponseText = liveAiResponseText,
        activeModelName = chatModel,
        audioLevel = audioRms,
        onToggleListening = {
            if (isListening) {
                voiceManager.stopListening()
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        },
        onDismiss = {
            voiceManager.stopListening()
            voiceManager.stopSpeaking()
            showLiveVoiceMode = false
            liveAiResponseText = ""
            lastProcessedPrompt = ""
        }
    )
}
