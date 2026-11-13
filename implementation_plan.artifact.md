# Comprehensive Feature Implementation Plan for Celestia

This implementation plan details the addition of four major feature sets to Celestia:
1. **Voice Input (STT) & Text-to-Speech (TTS)**
2. **Multi-Modal Vision Support (Ollama Vision Models)**
3. **Advanced Chat Organization (Search & Tags)**
4. **Developer Workbench & Prompt Playground Enhancements**

---

## User Review Required

> [!IMPORTANT]
> - **Permissions**: Voice STT requires `RECORD_AUDIO` permission. Vision support requires permission to pick images from storage/camera.
> - **Ollama Compatibility**: Vision features require a vision-capable Ollama model (e.g., `llava`, `llama3.2-vision`) installed on the server.

---

## Proposed Changes

### 1. Voice Input (STT) & Text-to-Speech (TTS)
- **Files**:
  - [NEW] `app/src/main/java/ai/moon/celestia/core/util/VoiceManager.kt`
  - [MODIFY] `app/src/main/java/ai/moon/celestia/feature/chat/ChatScreen.kt`
  - [MODIFY] `app/src/main/java/ai/moon/celestia/feature/chat/ChatViewModel.kt`
- **Details**: Implement `SpeechRecognizer` wrapper for dictating prompts and Android `TextToSpeech` engine to read AI assistant messages aloud. Add microphone and speaker buttons to the chat input and message bubbles.

### 2. Multi-Modal Vision Support
- **Files**:
  - [MODIFY] `app/src/main/java/ai/moon/celestia/core/network/OllamaApiService.kt`
  - [MODIFY] `app/src/main/java/ai/moon/celestia/core/data/OllamaRepository.kt`
  - [MODIFY] `app/src/main/java/ai/moon/celestia/feature/chat/ChatScreen.kt`
  - [MODIFY] `app/src/main/java/ai/moon/celestia/feature/chat/ChatViewModel.kt`
- **Details**: Add image attachment picker (gallery/camera) to chat, convert images to Base64, and transmit them via Ollama's multi-modal request payload (`images` array field).

### 3. Advanced Chat Search & Tags
- **Files**:
  - [MODIFY] `app/src/main/java/ai/moon/celestia/core/database/Entities.kt`
  - [MODIFY] `app/src/main/java/ai/moon/celestia/core/database/CelestiaDao.kt`
  - [MODIFY] `app/src/main/java/ai/moon/celestia/feature/chat/ChatViewModel.kt`
  - [MODIFY] `app/src/main/java/ai/moon/celestia/feature/chat/ChatScreen.kt`
- **Details**: Add `tags` and `pinned` fields to chat session entities, update Room DAO queries for full-text search and tag filtering, and add a search bar and tag pills in the chat drawer/screen.

### 4. Developer Workbench & Prompt Playground
- **Files**:
  - [MODIFY] `app/src/main/java/ai/moon/celestia/feature/workbench/WorkbenchScreen.kt`
  - [MODIFY] `app/src/main/java/ai/moon/celestia/feature/workbench/WorkbenchViewModel.kt`
- **Details**: Add interactive sliders for `temperature`, `top_p`, `repetition_penalty`, custom system prompt input, and live token generation latency benchmarks.

---

## Verification Plan

### Automated Tests
- Build verification via `gradle_build(":app:assembleDebug")`.
- Unit tests for database queries and ViewModels.

### Manual Verification
- Test voice recording and text-to-speech playback on emulator/device.
- Test attaching an image and sending it to a vision model.
- Test searching chat sessions and filtering by tags.
- Test adjusting parameters in the Workbench playground.
