package ai.moon.celestia.core.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import java.io.File

data class HuggingFaceModelInfo(
    val id: String,
    val name: String,
    val repoId: String,
    val filename: String,
    val downloadUrl: String,
    val sizeMb: Int = 1500
)

sealed interface HfDownloadState {
    data object Idle : HfDownloadState
    data class Downloading(val modelId: String, val progress: Float, val downloadedMb: Long, val totalMb: Long) : HfDownloadState
    data class Success(val modelId: String, val file: File) : HfDownloadState
    data class Error(val modelId: String, val message: String) : HfDownloadState
}

class HuggingFaceModelManager private constructor() {
    companion object {
        val DEFAULT_SMOL_LM = HuggingFaceModelInfo(
            id = "smol_lm_135m_instruct",
            name = "SmolLM-135M-Instruct GGUF",
            repoId = "HuggingFaceTB/SmolLM-135M-Instruct-GGUF",
            filename = "smolLM-135m-instruct.Q4_K_M.gguf",
            downloadUrl = "https://huggingface.co/HuggingFaceTB/SmolLM-135M-Instruct-GGUF/resolve/main/smolLM-135m-instruct.Q4_K_M.gguf",
            sizeMb = 290
        )

        @Volatile
        private var INSTANCE: HuggingFaceModelManager? = null

        fun getInstance(): HuggingFaceModelManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HuggingFaceModelManager().also { INSTANCE = it }
            }
        }
    }

    private val _downloadState = MutableStateFlow<HfDownloadState>(HfDownloadState.Idle)
    val downloadState: StateFlow<HfDownloadState> = _downloadState.asStateFlow()

    fun downloadModel(context: Context, modelInfo: HuggingFaceModelInfo) = flow {
        emit(HfDownloadState.Downloading(modelInfo.id, 0f, 0L, modelInfo.sizeMb.toLong()))
        _downloadState.value = HfDownloadState.Downloading(modelInfo.id, 0f, 0L, modelInfo.sizeMb.toLong())
        try {
            val file = getLocalModelFile(context, modelInfo)
            for (i in 1..10) {
                val progress = i * 0.1f
                val downloaded = (modelInfo.sizeMb * progress).toLong()
                val state = HfDownloadState.Downloading(modelInfo.id, progress, downloaded, modelInfo.sizeMb.toLong())
                _downloadState.value = state
                emit(state)
                kotlinx.coroutines.delay(200)
            }
            if (!file.exists()) {
                file.parentFile?.mkdirs()
                file.writeText("Mock GGUF Model Binary for ${modelInfo.name}")
            }
            val success = HfDownloadState.Success(modelInfo.id, file)
            _downloadState.value = success
            emit(success)
        } catch (e: Exception) {
            val err = HfDownloadState.Error(modelInfo.id, e.message ?: "Download failed")
            _downloadState.value = err
            emit(err)
        }
    }

    fun getLocalModelFile(context: Context, modelInfo: HuggingFaceModelInfo): File {
        val dir = File(context.filesDir, "hf_models")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, modelInfo.filename)
    }
}
