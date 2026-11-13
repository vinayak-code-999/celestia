package ai.moon.celestia.core.data

import ai.moon.celestia.ui.CelestiaTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SharedPayload(
    val sourceTool: String,
    val content: String,
    val targetTab: CelestiaTab,
    val actionPrompt: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

object SharedContentBus {
    private val _pendingPayload = MutableStateFlow<SharedPayload?>(null)
    val pendingPayload: StateFlow<SharedPayload?> = _pendingPayload.asStateFlow()

    fun shareContent(
        sourceTool: String,
        content: String,
        targetTab: CelestiaTab,
        actionPrompt: String? = null
    ) {
        if (content.isNotBlank()) {
            _pendingPayload.value = SharedPayload(
                sourceTool = sourceTool,
                content = content,
                targetTab = targetTab,
                actionPrompt = actionPrompt
            )
        }
    }

    fun consumePayload(): SharedPayload? {
        val payload = _pendingPayload.value
        _pendingPayload.value = null
        return payload
    }

    fun clear() {
        _pendingPayload.value = null
    }
}
