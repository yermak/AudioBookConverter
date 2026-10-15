# #715: 6.6.8 .zip crash on Windows 10 not checked

- **Status:** Reminder, check after the next release
- **Issue:** https://github.com/yermak/AudioBookConverter/issues/715 (closed)
- **Found:** 2026-10-11, issue triage

deddfe3 fixes the crash on a locale without a translation (cs_CZ: there is no `messages_cs`). A commenter also said the 6.6.8 `.zip` crashes on Windows 10 while the `.msi` works. That part was not checked. The closing comment asks them to reopen with a log.

## Next step

Unpack the next release's `.zip` on Windows 10 and start it, with the system language set to English and then to Czech.
