# PocketCode Professional UI Pass

## Intent
Upgrade existing native Compose UI without new dependencies: Agent status gives visible processing feedback, model selection stays compact and actionable, and Editor reads like a small VS Code surface.

## Approach
Use existing Material 3 and Compose primitives. Agent uses a repeatable pulse animation and clipboard service. Settings uses a bounded scroll card with selectable rows. Editor keeps local multi-tab buffers keyed by file path, renders breadcrumb and line-number gutter, and applies regex-based `AnnotatedString` styling for keywords, strings, comments, function names, and braces.

## Constraints
No PTY rewrite, no external editor library, no full parser, no undo/redo. Keep `WorkspaceFileService` as file boundary. Existing SAF URI behavior remains: primary external storage can map to a safe File path; unsupported providers remain URI display only.

## Verification
Run `git diff --check`, `./gradlew :app:compileDebugKotlin --no-daemon`, `./gradlew :app:assembleDebug --no-daemon`, and `./gradlew :app:testDebugUnitTest --no-daemon`. If Java is unavailable, report that exact blocker. Inspect source for animation, clipboard actions, bounded model scroll, tabs/breadcrumb/line numbers, and required env keys.
