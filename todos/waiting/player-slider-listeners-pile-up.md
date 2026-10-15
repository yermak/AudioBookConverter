# Player slider listeners pile up and can hit a null player

- **Status:** Suspected from code reading, not reproduced
- **Location:** `src/main/java/uk/yermak/audiobookconverter/fx/MediaPlayerController.java` (`playMedias`, the `timelapse.valueProperty().addListener` block)
- **Found:** 2026-10-11, while fixing #420

## Problem

Every `playMedias` call adds a new listener to `timelapse.valueProperty()`, and none is ever removed. Each listener uses the static `mediaPlayer` field. After the file ends, or after `release()`, `mediaPlayer` is null, so dragging the slider can throw a NullPointerException in the listeners from earlier plays. With several plays, one drag also seeks several times.

## Suggested fix

Add the slider listener once, in the constructor, and return early when `mediaPlayer == null`.
