# M4B merge hangs on "Merging chapters" with covers in the source files

- **Status:** One cause fixed (7a4c4c9); the reporter's trigger is not proven, needs their log
- **Location:** `src/main/java/uk/yermak/audiobookconverter/ConversionJob.java` (`run`), `ConversionContext.addJob` (`executorService.execute`)
- **Issue:** https://github.com/yermak/AudioBookConverter/issues/737 (open, labelled bug), reported on 6.6.8
- **Found:** 2026-10-11; diagnosed 2026-10-12/13 (sub-agent, Fable 5.1)

## What is known

- Proven mechanism: `ConversionJob.run` caught only `Exception`; the job runs via `execute()` (no Future), with no uncaught exception handler and stderr not logged. An `Error` thrown during the merge window killed the thread silently: the status stayed IN_PROGRESS, the row stayed on "Merging chapters..." with Pause and Stop enabled and the time ticking, and nothing reached the log. Present in 6.6.8. Fixed in 7a4c4c9 (`catch (Exception | Error e)`), guarded by `ConversionJobErrorTest`.
- A non-zero ffmpeg exit was always handled (shows "Error!"), in 6.6.8 too. The reporter's mjpeg error came from their own command; the app drops video (`-vn`) in the per-file step and the concat.
- 6.6.8 only: ffmpeg waiting on "Overwrite? [y/N]" if the concat output already existed (fixed by f0295e1 and 08a0145); no way found to hit it with a single-part merge.
- The real pipeline (chaptered m4b parts, 30 to 36 chapter jobs, several and large covers, temp folder with a space and an apostrophe) completes on HEAD and on 6.6.8 built from the tag.
- Untested: the JavaFX thread blocked (the whole window frozen). The fix doesn't cover that.

## Ask the reporter

The log is `%APPDATA%\AudioBookConverter\6.6.8\AudioBookConverter.log`. What to check while it is stuck:

| While stuck | Points to |
|---|---|
| "Time elapsed" has stopped | JavaFX thread blocked (a separate bug) |
| `ffmpeg.exe` still running, log ends at "Starting concat" without "Concat Error:" | concat never exits (the 6.6.8 overwrite prompt) |
| no ffmpeg, the `~abc_6.6.8_*` pieces in `%TEMP%` are gone | an Error inside the merge: fixed by 7a4c4c9 |
| no ffmpeg, the pieces are still there | blocked or died before the merge |

Also ask for their temp path, the number of files and chapters, the split setting and the output name, and for a retest on the next release.

## Optional

A default `UncaughtExceptionHandler` that logs through slf4j, because stderr is lost in the packaged app (`AudiobookConverter.java:69` is commented out).
