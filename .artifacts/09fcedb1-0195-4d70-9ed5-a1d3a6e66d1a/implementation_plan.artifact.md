# Celestia UI/UX Refinement & Spatial Integrity Plan

This implementation plan addresses UI inconsistencies, uneven spacing, visual artifacts (such as the white bar in top bar glass cards), mispositioned floating action buttons, lack of spatial transition animations, and core chat UX enhancements outlined in our recent audit.

## User Review Required

> [!IMPORTANT]
> **Top Bar Glass Visual Artifact:** The white bar in the glass top bar is caused by conflicting background colors or tonal elevation mixing in `CelestiaTopBar`. We will unify its glassmorphic background and border rendering.

> [!IMPORTANT]
> **Spatial Navigation:** We will introduce directional slide animations (spatial integrity) when navigating between main tabs and conversation views.

## Open Questions

- None. All requirements for layout polish, spacing consistency, and spatial transitions are clear.

## Proposed Changes

### Design System Components
#### [MODIFY] [CelestiaComponents.kt](file:///C:/Users/vinay/AndroidStudioProjects/celestia2/app/src/main/java/ai/moon/celestia/core/designsystem/component/CelestiaComponents.kt)
- Fix `CelestiaTopBar` styling to eliminate the internal white bar artifact by cleaning up surface elevation and surface variant mixing.
- Refine glass card backgrounds and borders for consistent translucency across dark/light themes.

### Chat Feature & Layouts
#### [MODIFY] [ChatScreen.kt](file:///C:/Users/vinay/AndroidStudioProjects/celestia2/app/src/main/java/ai/moon/celestia/feature/chat/ChatScreen.kt)
- **FAB Position:** Fix "New Chat" FloatingActionButton positioning so it floats correctly above the bottom bar without overlapping content or rendering in an arbitrary box.
- **Message Bubbles:** Replace rigid `fillMaxWidth(0.85f)` with `widthIn(max = 320.dp)` and `wrapContentWidth()` for natural message bubble sizing.
- **Empty State:** Add interactive quick-start suggestion chips.
- **Streaming State:** Turn send button into a Stop/Cancel button during streaming (`ChatUiState.Streaming`).
- **Session Deletion:** Add a confirmation dialog before deleting chat sessions.

### App Navigation & Spatial Animations
#### [MODIFY] [CelestiaApp.kt](file:///C:/Users/vinay/AndroidStudioProjects/celestia2/app/src/main/java/ai/moon/celestia/ui/CelestiaApp.kt)
- Replace basic `Crossfade` with directional slide/fade transitions (`slideInHorizontally` / `slideOutHorizontally`) to establish spatial integrity between tabs and conversation views.

## Verification Plan

### Automated Tests
- Run existing unit and UI tests via Gradle build to ensure no regression.
- Command: `gradle_build(":app:assembleDebug")`

### Manual Verification
- Deploy app to connected device/emulator.
- Verify top bar glass rendering (no white bar artifact).
- Verify FAB position on chat history screen.
- Verify message bubble width wrapping for short vs long messages.
- Test spatial transitions between tabs and active chats.
