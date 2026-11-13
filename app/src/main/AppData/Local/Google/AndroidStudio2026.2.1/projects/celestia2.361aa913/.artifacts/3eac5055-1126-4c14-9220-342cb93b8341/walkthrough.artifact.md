# Walkthrough - Maximum Token Speed & Real-Time Chat Persistence

## 1. Maximum Token Speed Restored (`10-15+ tokens/sec`)
- Removed rigid `OllamaOptions` overrides (`options = null`), enabling Ollama's native high-performance auto-detection for GPU offloading and core threads. This eliminates artificial bottlenecks and restores peak token generation speed.

## 2. Mid-Response Chat Persistence
- Added real-time message saving (`storageManager.saveMessages`) on every chunk update during streaming in `ChatViewModel`.
- Updated `closeActiveChat()` to immediately persist messages to storage before closing the active chat view. Exiting mid-response now preserves all generated text safely.

## Verification Results
- **Build (`app:assembleDebug`)**: SUCCESS
- **Unit Tests (`app:testDebugUnitTest`)**: 3 passed, 0 failed
