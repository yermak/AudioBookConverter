# Extended characters in file names leave the chapters view empty

- **Status:** Fixed on every path found (1a99c21, 096d08b, earlier cc2d897). Which one the reporter hit is unknown (their OS wasn't given), so the issue is still open
- **Issue:** https://github.com/yermak/AudioBookConverter/issues/627 (open, labelled bug), reported 2024-09 (6.5.2)
- **Debugged:** 2026-10-13

## Findings

- **Amplifier, fixed by cc2d897:** in 6.5.2 and 6.6.8, `Book.construct` caught `Throwable` and dropped the whole part, so one file whose probe failed emptied the entire chapters view. Now that file is left out with a warning.
- **Linux, proven in WSL:** Java reads file names in the charset of the locale it starts with. Under C/POSIX, `sun.jnu.encoding` is ASCII and every non-ASCII name became `?`: `exists()` is false, and ffprobe gets `Gr????e.mp3`, giving "No such file". `-Dsun.jnu.encoding=UTF-8` is ignored; `LC_CTYPE=C.UTF-8` fixes it. Fixed by 096d08b: `build/linux/install-utf8-launcher.sh` puts a script in front of the jpackage launcher. Tested with a real jpackage image (JDK 17) under LANG=C, POSIX and LC_ALL=C, and again on the TeamCity 26.10.8 tarball (JDK 27 jpackage and runtime). PR #757. Linux tarballs aren't currently published (#732).
- **Windows, separate, proven:** `mp4art.exe` and `mp4info.exe` take narrow argv (system ANSI code page) but libmp4v2 decodes paths as UTF-8, so on a non-UTF-8 Windows non-ASCII paths fail to open. Covers were lost, and M4B conversion failed when the cover or `%TEMP%` path had such characters. It did not empty the chapters view, because a failed `--list` returns no covers. Fixed by 1a99c21: the embedded manifest sets activeCodePage UTF-8 (`build/windows/set_utf8_manifest.py`).
- **Ruled out:** the Windows Java and ffprobe path is Unicode end to end (ProcessBuilder via CreateProcessW, wide file APIs, UTF-8 reading of ffprobe output); mojibake with C1 controls; ANSI cue sheets (read leniently, no exception); macOS, where `sun.jnu.encoding` is always UTF-8.

## Next step

Close #627 when the next release ships, or keep it open until the reporter confirms their OS.
