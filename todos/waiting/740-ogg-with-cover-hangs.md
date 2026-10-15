# Converting an OGG with an embedded cover hangs

- **Status:** Can't reproduce; needs a log, the output format and the file from the reporter
- **Location:** unknown. Candidates: `OGGFormat.prepareOggMetaFile` (cover as `metadata_block_picture`), `Mp4v2ArtBuilder`, `FFmpegArtWorkExtractor`
- **Issue:** https://github.com/yermak/AudioBookConverter/issues/740 (open, labelled bug), reported on 6.6.8
- **Found:** 2026-10-11

## Symptom

An OGG input with an embedded JPEG cover (870 KiB, 1461x2200) hangs the conversion "with different profiles". With the cover removed in the cover tab it works. Re-adding the same JPEG from disk hangs again, so the poster itself triggers it, not its extraction.

## Ruled out (bundled ffmpeg 8.0.1, 2.1 MB JPEG 1461x2200, app's own argument lists)

- M4B: concat, `mp4art --add`, optimize with `-map 0:v? -map 0:a`: each step under 0.4 s.
- OGG output, Opus and Vorbis temp files: concat with the cover as a 2.8 MB base64 `metadata_block_picture` line, built by the real `FlacPicture.load().write()`: ~240 ms.
- The same with 6.6.8's unescaped FFMETADATA: ~250 ms.
- stderr deadlock: `StreamCopier` drains asynchronously, and ffmpeg truncates long tags in its log (4.5 KB).

## Possibly already fixed after 6.6.8

- f0295e1: ffmpeg stdin is closed, so an "Overwrite? [y/N]" prompt can no longer hang a job.
- 9dea6d6: `FlacPicture.load` no longer keeps the poster file open.
- 61f1ef3: `ArtWorkImage.getBean` race.

## Next step

Ask the reporter for the output format and preset, and for `AudioBookConverter.log` (in `%APPDATA%`), or a link to the file. Or retest on the next release. Repro scripts: scratchpad `r740.sh`.
