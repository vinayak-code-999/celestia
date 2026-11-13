package ai.moon.celestia.feature.settings

import ai.moon.celestia.BuildConfig
import ai.moon.celestia.core.data.CelestiaTaskManager
import ai.moon.celestia.core.datastore.UserPreferences
import ai.moon.celestia.core.designsystem.component.CelestiaCard
import ai.moon.celestia.feature.settings.components.CategoryDefaultsCard
import ai.moon.celestia.feature.settings.components.GlobalDefaultModelCard
import ai.moon.celestia.feature.settings.components.HostServerCard
import ai.moon.celestia.feature.settings.components.ProfileAndAppearanceCard
import ai.moon.celestia.feature.settings.components.ThemeSelectorCard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onOpenDesignGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userPrefs by viewModel.userPreferences.collectAsState(initial = UserPreferences())
    val discoveredServers by viewModel.discoveredServers.collectAsState()
    val isSearchingMdns by viewModel.isSearchingMdns.collectAsState()
    val healthStatus by viewModel.healthStatus.collectAsState()
    val availableModels by viewModel.availableModels.collectAsState()
    val hfDownloadState by viewModel.hfDownloadState.collectAsState()
    val selectedSlmModelId by viewModel.selectedSlmModelId.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.startMdnsScan()
        viewModel.testConnection(userPrefs.hostUrl)
    }

    val topContentPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 62.dp
    val bottomContentPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 80.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(modifier = Modifier.height(topContentPadding))

        // 1. Personality Theme Selector Card
        ThemeSelectorCard(
            selectedThemeStyle = userPrefs.themeStyle,
            onSelectThemeStyle = { viewModel.updateThemeStyle(it) }
        )

        // 2. Profile Avatar & Appearance Card
        ProfileAndAppearanceCard(
            userAvatar = userPrefs.userAvatar,
            onUpdateUserAvatar = { viewModel.updateUserAvatar(it) },
            themeMode = userPrefs.themeMode,
            onUpdateThemeMode = { viewModel.updateThemeMode(it) },
            isDarkMode = userPrefs.isDarkMode,
            onUpdateDarkMode = { viewModel.updateDarkMode(it) },
            glassPreset = userPrefs.glassPreset,
            onApplyGlassPreset = { viewModel.applyGlassPreset(it) },
            glassAlpha = userPrefs.glassAlpha,
            onUpdateGlassAlpha = { viewModel.updateGlassAlpha(it) },
            glassShineIntensity = userPrefs.glassShineIntensity,
            onUpdateGlassShineIntensity = { viewModel.updateGlassShineIntensity(it) },
            isHapticsEnabled = userPrefs.isHapticsEnabled,
            onUpdateHapticsEnabled = { viewModel.updateHapticsEnabled(it) },
            onOpenDesignGuide = onOpenDesignGuide
        )

        // 3. Host Server Configuration Card
        HostServerCard(
            hostUrl = userPrefs.hostUrl,
            onUpdateHostUrl = { viewModel.updateHostUrl(it) },
            healthStatus = healthStatus,
            isSearchingMdns = isSearchingMdns,
            discoveredServers = discoveredServers,
            savedHosts = userPrefs.savedHostsList,
            onStartMdnsScan = { viewModel.startMdnsScan() },
            onConnectToDiscoveredServer = { viewModel.connectToDiscoveredServer(it) },
            onRemoveSavedHost = { viewModel.removeSavedHost(it) }
        )

        // 4. Global Default Model & Performance Card
        GlobalDefaultModelCard(
            defaultModel = userPrefs.defaultModel,
            availableModels = availableModels,
            onSelectDefaultModel = { viewModel.updateDefaultModel(it) },
            onRefreshModels = { viewModel.testConnection(userPrefs.hostUrl) },
            systemPrompt = userPrefs.systemPrompt,
            onSystemPromptChange = { viewModel.updateSystemPrompt(it) },
            maxContextTokens = userPrefs.maxContextTokens,
            onMaxContextTokensChange = { viewModel.updateMaxContextTokens(it) },
            autoUnloadModel = userPrefs.autoUnloadModel,
            onAutoUnloadModelChange = { viewModel.updateAutoUnloadModel(it) },
            numThreads = userPrefs.numThreads,
            onNumThreadsChange = { viewModel.updateNumThreads(it) },
            onPurgeVram = {
                CelestiaTaskManager.purgeVram(
                    hostUrl = userPrefs.hostUrl,
                    modelName = userPrefs.defaultModel
                )
            }
        )

        // 4.5 Hugging Face On-Device Model Manager Card
        ai.moon.celestia.feature.settings.components.HuggingFaceModelManagerCard(
            downloadState = hfDownloadState,
            onDownloadModel = { viewModel.downloadHfModel(context, it) },
            onDownloadCustomUrl = { viewModel.downloadCustomHfUrl(context, it) },
            onDeleteModel = { viewModel.deleteHfModel(context, it) },
            selectedSlmModelId = selectedSlmModelId,
            onSelectSlmModel = { viewModel.selectSlmModel(it) }
        )

        // 5. Category-Specific Default Models & Word Count Limits Card
        CategoryDefaultsCard(
            availableModels = availableModels,
            chatModel = userPrefs.chatModel,
            onChatModelChange = { viewModel.updateChatModel(it) },
            mathModel = userPrefs.mathModel,
            onMathModelChange = { viewModel.updateMathModel(it) },
            codingModel = userPrefs.codingModel,
            onCodingModelChange = { viewModel.updateCodingModel(it) },
            languageModel = userPrefs.languageModel,
            onLanguageModelChange = { viewModel.updateLanguageModel(it) },
            ocrModel = userPrefs.ocrModel,
            onOcrModelChange = { viewModel.updateOcrModel(it) },
            chatMaxWords = userPrefs.chatMaxWords,
            onChatMaxWordsChange = { viewModel.updateChatMaxWords(it) },
            summarizeMaxWords = userPrefs.summarizeMaxWords,
            onSummarizeMaxWordsChange = { viewModel.updateSummarizeMaxWords(it) },
            translateMaxWords = userPrefs.translateMaxWords,
            onTranslateMaxWordsChange = { viewModel.updateTranslateMaxWords(it) },
            workbenchMaxWords = userPrefs.workbenchMaxWords,
            onWorkbenchMaxWordsChange = { viewModel.updateWorkbenchMaxWords(it) }
        )

        // App Build Information Card
        CelestiaCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "App & Build Information",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (BuildConfig.IS_DEBUG) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (BuildConfig.IS_DEBUG) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Text(
                            text = BuildConfig.BUILD_TYPE_NAME.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Version",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = BuildConfig.VERSION_NAME,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Application ID",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = BuildConfig.APPLICATION_ID,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Debug Logging",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (BuildConfig.ENABLE_DEBUG_LOGS) "Enabled" else "Disabled",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(bottomContentPadding))
    }
}
