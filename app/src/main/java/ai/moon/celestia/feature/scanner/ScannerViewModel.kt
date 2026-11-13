package ai.moon.celestia.feature.scanner

import ai.moon.celestia.core.data.OllamaRepository
import ai.moon.celestia.core.datastore.UserPreferencesRepository
import ai.moon.celestia.core.designsystem.util.StreamingTextBuffer
import ai.moon.celestia.core.model.OllamaModelItem
import ai.moon.celestia.core.model.OllamaOptions
import ai.moon.celestia.core.util.DocumentExporter
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

sealed interface ScannerUiState {
    data object Idle : ScannerUiState
    data class Processing(val progressText: String) : ScannerUiState
    data object Completed : ScannerUiState
    data class Error(val message: String) : ScannerUiState
}

class ScannerViewModel(
    private val context: Context,
    private val preferencesRepository: UserPreferencesRepository = UserPreferencesRepository(context),
    private val repository: OllamaRepository = OllamaRepository()
) : ViewModel() {

    private val textBuffer = StreamingTextBuffer(viewModelScope, flushIntervalMs = 40L)
    val documentExporter = DocumentExporter(context)

    private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Idle)
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private val _extractedText = MutableStateFlow("")
    val extractedText: StateFlow<String> = _extractedText.asStateFlow()

    private val _tokensPerSec = MutableStateFlow(0.0)
    val tokensPerSec: StateFlow<Double> = _tokensPerSec.asStateFlow()

    private val _availableModels = MutableStateFlow<List<OllamaModelItem>>(emptyList())
    val availableModels: StateFlow<List<OllamaModelItem>> = _availableModels.asStateFlow()

    private val _selectedModel = MutableStateFlow(preferencesRepository.userPreferencesFlow.value.ocrModel)
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _isAutoCropEnabled = MutableStateFlow(true)
    val isAutoCropEnabled: StateFlow<Boolean> = _isAutoCropEnabled.asStateFlow()

    private var hostUrl = preferencesRepository.userPreferencesFlow.value.hostUrl

    fun toggleAutoCrop() {
        _isAutoCropEnabled.value = !_isAutoCropEnabled.value
    }

    init {
        viewModelScope.launch {
            preferencesRepository.userPreferencesFlow.collect { prefs ->
                hostUrl = prefs.hostUrl
                _selectedModel.value = prefs.ocrModel
            }
        }
        viewModelScope.launch {
            textBuffer.displayState.collect { text ->
                _extractedText.value = text
            }
        }
        loadModels()
    }

    fun loadModels() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.fetchAvailableModels(hostUrl)
            result.onSuccess { models ->
                _availableModels.value = models
            }
        }
    }

    fun selectModel(modelName: String) {
        _selectedModel.value = modelName
        preferencesRepository.updateModelForScope(ai.moon.celestia.core.data.AiFunctionScope.Scanner, modelName)
    }

    fun processImageUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = ScannerUiState.Processing("Optimizing image resolution & auto-cropping for Vision AI...")
                val base64Image = compressAndEncodeImage(uri)
                if (base64Image == null) {
                    _uiState.value = ScannerUiState.Error("Failed to read image file.")
                    return@launch
                }

                runOllamaScan(listOf(base64Image))
            } catch (e: Exception) {
                _uiState.value = ScannerUiState.Error("Error: ${e.message}")
            }
        }
    }

    fun scanToDigitalPdf(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = ScannerUiState.Processing("Auto-cropping & tracing character positions with AI...")
                val base64Image = compressAndEncodeImage(uri)
                if (base64Image == null) {
                    _uiState.value = ScannerUiState.Error("Failed to process document image.")
                    return@launch
                }

                runOllamaScan(
                    images = listOf(base64Image),
                    customPrompt = "Trace all character positions, headings, paragraphs, lists, and tables in this scanned image. Output exact clean Markdown structure preserving layout alignment for digital PDF."
                )

                if (_extractedText.value.isNotBlank()) {
                    exportToPdf()
                }
            } catch (e: Exception) {
                _uiState.value = ScannerUiState.Error("Digital PDF error: ${e.message}")
            }
        }
    }

    fun processPdfUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = ScannerUiState.Processing("Opening PDF document...")
                val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
                if (fileDescriptor == null) {
                    _uiState.value = ScannerUiState.Error("Failed to open PDF file.")
                    return@launch
                }

                val pdfRenderer = PdfRenderer(fileDescriptor)
                if (pdfRenderer.pageCount == 0) {
                    _uiState.value = ScannerUiState.Error("PDF document is empty.")
                    return@launch
                }

                val base64Images = mutableListOf<String>()
                // Process up to 10 pages in chunks
                val pageLimit = minOf(pdfRenderer.pageCount, 10)

                for (i in 0 until pageLimit) {
                    _uiState.value = ScannerUiState.Processing("Rendering PDF page ${i + 1}/$pageLimit...")
                    val page = pdfRenderer.openPage(i)
                    
                    // Maintain high resolution (1280px max dimension) for crisp OCR
                    val maxPageDim = maxOf(page.width, page.height)
                    val scale = if (maxPageDim > 0) (1280f / maxPageDim).coerceIn(1f, 2.5f) else 2f
                    val finalWidth = (page.width * scale).toInt().coerceAtLeast(1)
                    val finalHeight = (page.height * scale).toInt().coerceAtLeast(1)

                    val bitmap = Bitmap.createBitmap(finalWidth, finalHeight, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                    val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                    base64Images.add(base64Image)
                    bitmap.recycle()
                }

                pdfRenderer.close()
                fileDescriptor.close()

                runOllamaScan(base64Images)
            } catch (e: Exception) {
                _uiState.value = ScannerUiState.Error("Error processing PDF: ${e.message}")
            }
        }
    }

    private suspend fun runOllamaScan(images: List<String>, customPrompt: String? = null) {
        _uiState.value = ScannerUiState.Processing("Analyzing document & extracting text with Vision AI...")
        _tokensPerSec.value = 0.0
        textBuffer.clear()
        textBuffer.startStreaming()

        val systemPrompt = "You are an expert AI Document Layout Engine and OCR Scanner. Trace all character positions, headings, lists, and tables (| Header | Header |). Output exact clean Markdown structure preserving page formatting."

        val visionOptions = OllamaOptions(
            numCtx = 4096,
            temperature = 0.2f,
            repeatPenalty = 1.10f
        )

        val startTime = System.currentTimeMillis()
        var tokenCount = 0

        val promptToUse = customPrompt ?: "Extract all text, headings, and tables from this document image in clean Markdown."

        for ((index, imageBase64) in images.withIndex()) {
            if (images.size > 1) {
                _uiState.value = ScannerUiState.Processing("Extracting text from page ${index + 1} of ${images.size}...")
                if (index > 0) {
                    textBuffer.appendChunk("\n\n---\n\n### Page ${index + 1}\n\n")
                }
            }

            try {
                repository.streamGenerate(
                    hostUrl = hostUrl,
                    model = _selectedModel.value,
                    prompt = promptToUse,
                    system = systemPrompt,
                    images = listOf(imageBase64),
                    options = visionOptions
                ).collect { chunk ->
                    val text = chunk.response.orEmpty()
                    if (text.isNotEmpty()) {
                        textBuffer.appendChunk(text)
                        tokenCount += maxOf(1, text.length / 4)
                        val elapsedSec = maxOf(0.1, (System.currentTimeMillis() - startTime) / 1000.0)
                        _tokensPerSec.value = if (chunk.evalCount != null && chunk.evalDuration != null && chunk.evalDuration > 0) {
                            chunk.evalCount.toDouble() / (chunk.evalDuration.toDouble() / 1_000_000_000.0)
                        } else {
                            tokenCount / elapsedSec
                        }
                    }
                }
            } catch (e: Exception) {
                textBuffer.completeStreaming()
                _uiState.value = ScannerUiState.Error("Extraction error on page ${index + 1}: ${e.message}")
                return
            }
        }

        textBuffer.completeStreaming()
        _uiState.value = ScannerUiState.Completed
    }

    private fun compressAndEncodeImage(uri: Uri): String? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        val maxTargetDim = 1280
        var inSampleSize = 1

        if (options.outHeight > maxTargetDim || options.outWidth > maxTargetDim) {
            val halfHeight: Int = options.outHeight / 2
            val halfWidth: Int = options.outWidth / 2
            while (halfHeight / inSampleSize >= maxTargetDim && halfWidth / inSampleSize >= maxTargetDim) {
                inSampleSize *= 2
            }
        }

        options.inJustDecodeBounds = false
        options.inSampleSize = inSampleSize
        var scaledBitmap = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return null

        val maxDim = maxOf(scaledBitmap.width, scaledBitmap.height)
        if (maxDim > maxTargetDim) {
            val scale = maxTargetDim.toFloat() / maxDim
            val finalWidth = (scaledBitmap.width * scale).toInt().coerceAtLeast(1)
            val finalHeight = (scaledBitmap.height * scale).toInt().coerceAtLeast(1)
            val resized = Bitmap.createScaledBitmap(scaledBitmap, finalWidth, finalHeight, true)
            scaledBitmap.recycle()
            scaledBitmap = resized
        }

        // Apply Intelligent Auto-Crop if enabled
        val processedBitmap = if (_isAutoCropEnabled.value) {
            val cropped = ai.moon.celestia.core.util.ImageCropUtils.autoCropDocumentMargins(scaledBitmap)
            val enhanced = ai.moon.celestia.core.util.ImageCropUtils.preprocessDocumentBitmap(cropped)
            if (cropped != scaledBitmap) cropped.recycle()
            scaledBitmap.recycle()
            enhanced
        } else {
            val enhanced = ai.moon.celestia.core.util.ImageCropUtils.preprocessDocumentBitmap(scaledBitmap)
            scaledBitmap.recycle()
            enhanced
        }

        val outputStream = ByteArrayOutputStream()
        processedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        processedBitmap.recycle()
        return base64
    }

    fun exportAndShare() {
        if (_extractedText.value.isBlank()) return
        val docFile = documentExporter.exportToWordDoc("Scanned_Document", _extractedText.value)
        if (docFile != null) {
            documentExporter.shareDocumentFile(docFile, "application/msword")
        }
    }

    fun exportToPdf() {
        if (_extractedText.value.isBlank()) return
        val pdfFile = documentExporter.exportToPdf("Scanned_Document", _extractedText.value)
        if (pdfFile != null) {
            documentExporter.shareDocumentFile(pdfFile, "application/pdf")
        }
    }

    fun exportToMarkdown() {
        if (_extractedText.value.isBlank()) return
        val mdFile = documentExporter.exportToMarkdown("Scanned_Document", _extractedText.value)
        if (mdFile != null) {
            documentExporter.shareDocumentFile(mdFile, "text/markdown")
        }
    }
}
