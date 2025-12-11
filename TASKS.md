# Proposed Cleanup and Bugfix Tasks

## Unused Methods to Address
1. Remove or integrate the unused `DurationVerifier.mp4v2UpdateDuration` helper; it is never invoked and risks diverging from the FFprobe-based path. 【F:src/main/java/uk/yermak/audiobookconverter/DurationVerifier.java†L45-L69】
2. Decide whether `MediaPlayerController.disablePlayer` should be wired back to the UI selection listener or removed as dead code. 【F:src/main/java/uk/yermak/audiobookconverter/fx/MediaPlayerController.java†L49-L57】
3. Drop the unused `JfxEnv.addMnemonic` wrapper or add mnemonic bindings where shortcuts are expected. 【F:src/main/java/uk/yermak/audiobookconverter/fx/JfxEnv.java†L36-L43】
4. Prune the dormant listener hook in `MediaPlayerController.initialize` (commented-out code) or re-enable it to prevent drift. 【F:src/main/java/uk/yermak/audiobookconverter/fx/MediaPlayerController.java†L41-L53】
5. Remove the unused `ProgressComponent.isOver` pass-through or refactor callers to rely directly on `ConversionJob` status to avoid redundant API surface. 【F:src/main/java/uk/yermak/audiobookconverter/fx/ProgressComponent.java†L133-L138】

## Bugs to Fix
1. Prevent `MediaPlayerController.findNext` from throwing `IndexOutOfBoundsException` when the last track finishes by guarding the `i + 1` access. 【F:src/main/java/uk/yermak/audiobookconverter/fx/MediaPlayerController.java†L98-L112】
2. Handle the `null` result from `findNext` before calling `playMedias` to avoid `NullPointerException` on the final track. 【F:src/main/java/uk/yermak/audiobookconverter/fx/MediaPlayerController.java†L92-L115】
3. In `FFMpegConcatenator.concat`, the empty `URISyntaxException` catch block hides errors; ensure the exception is logged and aborts gracefully. 【F:src/main/java/uk/yermak/audiobookconverter/FFMpegConcatenator.java†L46-L66】
4. `ConversionJob.convert` discards the original interrupt status on `InterruptedException`, which can leave threads in an inconsistent state; restore the interrupt flag. 【F:src/main/java/uk/yermak/audiobookconverter/ConversionJob.java†L69-L86】
5. `DurationVerifier.parseDuration` trusts the probe output format; add bounds checks before `columns[j]` substring to avoid `NumberFormatException` when unexpected tokens appear. 【F:src/main/java/uk/yermak/audiobookconverter/DurationVerifier.java†L23-L44】
