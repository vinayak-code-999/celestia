# Celestia App Thorough Analysis & Enhancement Roadmap

Celestia is a beautifully crafted, modern local AI client built for **Ollama** using **Jetpack Compose**, **Material 3**, **Room Database**, and a custom glassmorphic design system with animated mascot avatars (`AIAvatarView`, `CelestiaMascotAvatar`) and haptic feedback.

---

## 1. Current Architecture & Strengths

- **Declarative UI**: Built entirely with Jetpack Compose, utilizing custom animations (`AnimatedContent`, custom easing, glassmorphic blur effects), bottom bars, and custom design tokens (`CelestiaTheme`, `CelestiaThemeStyle`).
- **Local LLM Integration**: Robust communication with Ollama via `OllamaApiService`, `OllamaDiscoveryManager`, and `OllamaRepository`, supporting real-time streaming tokens and token speed calculation (`tokensPerSecond`).
- **Data Persistence**: Room database (`CelestiaDatabase`, `CelestiaDao`, `Entities`) managing chat sessions and messages with JSON export capabilities (`ChatJsonExporter`).
- **Feature Modularity**: Clean separation across features (`chat`, `settings`, `summarize`, `translate`, `workbench`, `designguide`).

---

## 2. Technical & UX Areas for Improvement

### [!IMPORTANT] 1. Error Resilience & Connection Recovery
- **Current State**: Connection health status is tracked (`ServerHealthStatus`), but when Ollama drops or times out mid-request, recovery relies on manual retries.
- **Improvement**: Implement automatic heartbeat polling, exponential backoff retry logic for API calls, and graceful fallback handling when the server is unreachable.

### [!NOTE] 2. Context Window & Token Management
- **Current State**: Chat history is sent to Ollama, but long conversations can exceed model context limits (`num_ctx`) or degrade performance.
- **Improvement**: Add sliding-window context truncation, configurable context window sizing in settings, and visual token count indicators per message/session.

### [!TIP] 3. Data Ownership & Backup/Restore
- **Current State**: Basic JSON export exists (`ChatJsonExporter`).
- **Improvement**: Implement full database backup and restore (JSON/SQLite archive export and import) so users never lose their conversation history across device changes.

### 4. Accessibility (a11y) & Localization
- Ensure all custom Compose interactive elements, buttons, and mascot states have proper `contentDescription`, support screen readers (TalkBack), and respect system font scale settings.

### 5. Testing & CI
- Expand test coverage beyond basic instrumentation tests by adding unit tests for `ChatViewModel`, `OllamaRepository`, and Room DAOs using `room-testing`.

---

## 3. High-Impact Feature Additions to Increase Functionality

### 1. Model Management & Pulling (`Ollama Model Hub`)
- **What it is**: A dedicated screen or dialog in Settings allowing users to view installed models, check their details (size, parameter count, quantization), and pull new models directly from the Ollama library (e.g., `ollama pull llama3.2`, `mistral`, `gemma2`) with live download progress bars.

### 2. Prompt Library & Templates
- **What it is**: A pre-built and user-customizable repository of prompt templates (e.g., Code Review, Debugging, Creative Writing, SQL Generation, Summarization) categorized by use case, allowing users to tap and instantly execute complex prompts.

### 3. Voice Input & Speech-to-Text (STT) / Text-to-Speech (TTS)
- **What it is**:
  - **Voice Dictation**: Use Android's SpeechRecognizer or Whisper integration for hands-free prompt input.
  - **TTS**: Read AI responses aloud using Android's built-in `TextToSpeech` engine, transforming Celestia into a voice assistant.

### 4. Multi-Modal Vision Support (`Ollama Vision Models`)
- **What it is**: Support for vision models (`llava`, `llama3.2-vision`) by allowing users to attach images from their camera or gallery directly into the chat input bar for image analysis and description.

### 5. Advanced Chat Organization (Search & Tags)
- **What it is**: Full-text search across past chat sessions, conversation pinning, tagging (e.g., #Work, #Code, #Personal), and filtering to easily retrieve historical knowledge.

### 6. Developer Workbench & Prompt Playground
- **What it is**: Expand the `WorkbenchScreen` into an advanced playground to experiment with temperature, top_p, system prompts, repetition penalty sliders, and token latency benchmarks.
