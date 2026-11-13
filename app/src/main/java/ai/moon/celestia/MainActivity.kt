package ai.moon.celestia

import ai.moon.celestia.feature.chat.ChatViewModel
import ai.moon.celestia.feature.settings.SettingsViewModel
import ai.moon.celestia.feature.summarize.SummarizeViewModel
import ai.moon.celestia.feature.translate.TranslateViewModel
import ai.moon.celestia.feature.workbench.WorkbenchViewModel
import ai.moon.celestia.ui.CelestiaApp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.koin.androidx.viewmodel.ext.android.getViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val chatViewModel: ChatViewModel = getViewModel()
        val scannerViewModel: ai.moon.celestia.feature.scanner.ScannerViewModel = getViewModel()
        val summarizeViewModel: SummarizeViewModel = getViewModel()
        val translateViewModel: TranslateViewModel = getViewModel()
        val workbenchViewModel: WorkbenchViewModel = getViewModel()
        val settingsViewModel: SettingsViewModel = getViewModel()

        setContent {
            CelestiaApp(
                chatViewModel = chatViewModel,
                scannerViewModel = scannerViewModel,
                summarizeViewModel = summarizeViewModel,
                translateViewModel = translateViewModel,
                workbenchViewModel = workbenchViewModel,
                settingsViewModel = settingsViewModel
            )
        }
    }
}
