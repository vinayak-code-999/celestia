# Celestia System Functions & Workflows Reference

## 1. ViewModels & State Management
- **`ChatViewModel`**: Manages chat sessions, streaming generation via `OllamaRepository`, active model overrides, web search integration, Python/math tool execution, and session persistence (`ChatStorageManager`).
- **`ScannerViewModel`**: Handles image/PDF compression, Base64 encoding, OCR prompt generation, document export (`DocumentExporter`), and share actions.
- **`SummarizeViewModel`**: Manages summarization styles, text buffering, and background task execution (`CelestiaTaskManager`).
- **`TranslateViewModel`**: Manages source/target languages, translation tones, and multilingual streaming requests.
- **`WorkbenchViewModel`**: Manages workflow presets, custom system prompts, temperature/top-p sliders, and code generation streaming.
- **`SettingsViewModel`**: Manages mDNS auto-discovery (`OllamaDiscoveryManager`), server health checks, glassmorphism theme presets, and user preferences (`UserPreferencesRepository`).

---

## 2. Cross-Tool Data Bridge (`SharedContentBus`)
- Enables zero-friction handoffs between tools. When a user taps "Send to Chat / Summarize / Translate / Workbench" on any result card, `SharedContentBus` posts a `SharedPayload` containing the source tool, content, and target tab, which is automatically consumed and pre-loaded upon navigation.

---

## 3. Ollama Network Integration
- **`OllamaApiService`**: Ktor-based HTTP client handling streaming SSE chunks, model discovery (`/api/tags`), keep-alive model retention (`10m`), and health checks.
- **`OllamaContextManager`**: Prunes and manages context windows to ensure optimal memory consumption.
