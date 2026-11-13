package ai.moon.celestia.core.designsystem.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Throttling buffer that decouples rapid NDJSON stream chunks (20-50 tokens/sec)
 * from Compose recompositions by flushing accumulated tokens at a fixed frame interval (~50ms).
 */
class StreamingTextBuffer(
    private val scope: CoroutineScope,
    private val flushIntervalMs: Long = 20L
) {
    private val pendingBuffer = StringBuilder()
    private val _displayState = MutableStateFlow("")
    val displayState: StateFlow<String> = _displayState.asStateFlow()

    private var flushJob: Job? = null
    private var isStreaming = false

    fun startStreaming(initialText: String = "") {
        pendingBuffer.clear()
        pendingBuffer.append(initialText)
        _displayState.value = initialText
        isStreaming = true

        flushJob?.cancel()
        flushJob = scope.launch(Dispatchers.Default) {
            while (isStreaming) {
                delay(flushIntervalMs)
                val currentText = pendingBuffer.toString()
                if (_displayState.value != currentText) {
                    _displayState.value = currentText
                }
            }
        }
    }

    fun appendChunk(chunk: String) {
        pendingBuffer.append(chunk)
    }

    fun completeStreaming() {
        isStreaming = false
        flushJob?.cancel()
        _displayState.value = pendingBuffer.toString()
    }

    fun clear() {
        completeStreaming()
        pendingBuffer.clear()
        _displayState.value = ""
    }
}
