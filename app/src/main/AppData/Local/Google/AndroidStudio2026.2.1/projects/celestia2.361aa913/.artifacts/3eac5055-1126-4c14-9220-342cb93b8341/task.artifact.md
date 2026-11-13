# Final Task List

- [x] Set `options = null` by default in Ollama repository/ViewModel calls to utilize Ollama's native high-speed auto-detection (restoring full `10-15+ tokens/sec`) `[1]`
- [x] Implement real-time message persistence (`storageManager.saveMessages`) during active streaming and when closing/exiting chats (`closeActiveChat()`) so mid-response exits are never lost `[2]`
- [x] Run build and unit tests verification (`app:assembleDebug`, `app:testDebugUnitTest` - all passed successfully) `[3]`
