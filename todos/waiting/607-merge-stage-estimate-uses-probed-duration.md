# Merge-stage size, progress and time left use the probed duration

- **Status:** Decision needed (owner)
- **Location:** `src/main/java/uk/yermak/audiobookconverter/fx/ConversionProgress.java` (`totalDuration`, `converted`), `src/main/java/uk/yermak/audiobookconverter/FFMpegConcatenator.java:53`
- **Issue:** https://github.com/yermak/AudioBookConverter/issues/607 (closed: the final size is now the real file size, 95e2b4c)
- **Found:** 2026-10-11, debugging #607

## What is known

- `ConversionProgress.totalDuration` is `final` and captured when the job is created, from ffprobe durations. For a VBR mp3 without a Xing header ffprobe only guesses: 76s probed for 120s real, or 259s for 120s, depending on content.
- `DurationVerifier` fixes the media durations after each encode, but `totalDuration` keeps the guess. During the merge, the estimated size, progress bar and time left are off by the same ratio (the bar can sit at 46% at the end of the merge).
- Your comment on #607 said the size should not update during the merge. That would be `callback.reset(true)` in `FFMpegConcatenator`. a71fe2a chose `reset(false)` on purpose, so this is your call.

## Also

- `Mp4v2ArtBuilder.coverArt` passes `posters.size()*step` (a duration) as the size to `converted()`. `reset(true)` hides this today. Pass 0, or skip the size.

## Next step

Decide: freeze the size during the merge (`reset(true)`), or read `convertable.getDuration()` live instead of the final field.
