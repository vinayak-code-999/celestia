package ai.moon.celestia

import ai.moon.celestia.core.data.ChatStorageManager
import ai.moon.celestia.core.data.OllamaRepository
import ai.moon.celestia.core.datastore.UserPreferencesRepository
import ai.moon.celestia.feature.chat.ChatViewModel
import ai.moon.celestia.feature.settings.SettingsViewModel
import ai.moon.celestia.feature.summarize.SummarizeViewModel
import ai.moon.celestia.feature.translate.TranslateViewModel
import ai.moon.celestia.feature.workbench.WorkbenchViewModel
import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

class CelestiaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@CelestiaApplication)
            modules(appModule)
        }
    }
}

val appModule = module {
    single { ChatStorageManager(get()) }
    single { OllamaRepository() }
    single { UserPreferencesRepository(get()) }

    viewModel { ChatViewModel(get(), get(), get(), get()) }
    viewModel { SummarizeViewModel(get()) }
    viewModel { TranslateViewModel(get()) }
    viewModel { WorkbenchViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { ai.moon.celestia.feature.scanner.ScannerViewModel(get(), get(), get()) }
}
