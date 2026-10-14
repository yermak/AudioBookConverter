"""
Makes a Windows exe receive its command line in UTF-8, by replacing its embedded application manifest with one that
sets activeCodePage UTF-8 (Windows 10 1903+; older versions ignore it).

mp4art.exe and mp4info.exe take narrow argv, which Windows converts to the system ANSI code page, but libmp4v2 decodes
paths as UTF-8: on a non-UTF-8 Windows every non-ASCII path failed to open (issue #627). Re-apply after rebuilding mp4v2.

The new manifest must fit in the space of the old one (the resource size is not changed): it keeps only activeCodePage,
which is fine for x64 exes, where asInvoker is the default.

Usage: python set_utf8_manifest.py external/x64/windows/mp4art.exe external/x64/windows/mp4info.exe
"""
import sys

MANIFEST = ("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>\r\n"
            "<assembly xmlns='urn:schemas-microsoft-com:asm.v1' manifestVersion='1.0'>"
            "<application xmlns='urn:schemas-microsoft-com:asm.v3'><windowsSettings>"
            "<activeCodePage xmlns='http://schemas.microsoft.com/SMI/2019/WindowsSettings'>UTF-8</activeCodePage>"
            "</windowsSettings></application></assembly>").encode()

for path in sys.argv[1:]:
    b = bytearray(open(path, 'rb').read())
    start = b.find(b'<?xml')
    end = b.find(b'</assembly>', start) + len(b'</assembly>')
    if start < 0 or end < start:
        sys.exit(f'{path}: no embedded manifest')
    if b'activeCodePage' in b[start:end]:
        print(f'{path}: already set')
        continue
    if len(MANIFEST) > end - start:
        sys.exit(f'{path}: manifest space {end - start} bytes is too small for {len(MANIFEST)}')
    b[start:end] = MANIFEST + b' ' * (end - start - len(MANIFEST))
    open(path, 'wb').write(b)
    print(f'{path}: activeCodePage UTF-8')
