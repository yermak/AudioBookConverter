# Preview playback of ALAC books

- **Status:** Known limitation, decision needed if anyone asks
- **Location:** `src/main/java/uk/yermak/audiobookconverter/fx/MediaPlayerController.java` (`playMedias`)
- **Issue:** https://github.com/yermak/AudioBookConverter/issues/593 (closed: chapter playback for imported m4b fixed in c34c5d5)
- **Found:** 2026-10-11

## What is known

The JavaFX `MediaPlayer` cannot decode ALAC, so preview playback of Apple books encoded with ALAC does not work. The issue closed on the chapter-window part; the ALAC part stays open in practice.

## Options

Re-encode a short preview with ffmpeg (for example the first minutes of the chapter to AAC in a temp file), or show a message that ALAC cannot be previewed.
