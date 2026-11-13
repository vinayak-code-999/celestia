# Final Performance & Chat Persistence Fixes

This plan addresses:
1. **Token Speed Optimization (Restoring 10-15+ tokens/sec)**: Remove forced `OllamaOptions` objects in repository calls (`options = null` by default) so Ollama uses its native high-speed auto-detection for GPU offloading and threading.
2. **Mid-Response Chat Persistence**: Save messages to storage (`storageManager.saveMessages`) in real-time during streaming updates and when exiting chat (`closeActiveChat()`), ensuring mid-response exits are never lost.

## Proposed Changes

### Core Data & Repository
#### [MODIFY] [OllamaRepository.kt](file:///C:/Users/vinay/AndroidStudioProjects/celestia2/app/src/main/java/ai/moon/celestia/core/data/OllamaRepository.kt)
- Make `options: OllamaOptions? = null` default in `streamChat` and `streamGenerate` so Ollama's native auto-detection handles hardware acceleration without throttling.

---

### ViewModels
#### [MODIFY] [ChatViewModel.kt](file:///C:/Users/vinay/AndroidStudioProjects/celestia2/app/src/main/java/ai/moon/celestia/feature/chat/ChatViewModel.kt)
- Pass `options = null` to `repository.streamChat`.
- Call `storageManager.saveMessages(sessionId, _messages.value)` continuously during streaming token accumulation.
- In `closeActiveChat()`, persist current messages before setting `_currentSessionId.value = null`.

#### [MODIFY] [SummarizeViewModel.kt](file:///C:/Users/vinay/AndroidStudioProjects/celestia2/app/src/main/java/ai/moon/celestia/feature/summarize/SummarizeViewModel.kt), [TranslateViewModel.kt](file:///C:/Users/vinay/AndroidStudioProjects/celestia2/app/src/main/java/ai/moon/celestia/feature/translate/TranslateViewModel.kt), [WorkbenchViewModel.kt](file:///C:/Users/vinay/AndroidStudioProjects/celestia2/app/src/main/java/ai/moon/celestia/feature/workbench/WorkbenchViewModel.kt)
- Pass `options = null` to `repository.streamGenerate`.

## Verification Plan

### Automated Tests
- Run unit tests (`gradle_build("app:testDebugUnitTest")`) and build (`gradle_build("app:assembleDebug")`).
