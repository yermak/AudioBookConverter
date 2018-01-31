Audio Book Converter V2
===============
* Convert Audiobook from mp3 to m4b.
* Convert mp3 to Audiobook.
* Convert mp3 to ipod format.
* Convert mp3 to IBook (IPhone) format.

<a href="https://github.com/yermak/AudioBookConverter/releases/download/version_2.0.0/AudioBookConverter-Installer-2.0.0.exe">Download</a>
-------------
Main features:
--------------
* Parallel encoding: more cores - faster results
* Smart artwork support: combined from MP3 artwork and cover files from MP3 directory
* Unicode tags support: keep tags in own language
* Chapters support: jump to the right chapter quickly
* Simple UI (inherited from original version)
--------------
This project is based on freeipodsoftware release of AudioBookConverter.

---------------------

Original look and feel is preserved, all internals were completely rewritten to use benefits of modern hardware: 64bits, multi-core processors.
All libraries and dependencies are refreshed to the latests version available at this moment.
Project is tested on different sets of mp3 files and latest iOS devices.

--------------
Major differences vs original version:
--------------
* Performance improved form 5 to 15x times (depending on numbers of cores), old version of Faac replaced with fresh FFMpeg.
* Added super-fast mode of parallel encoding of MP3 files.
* Added Artwork support, smart combination of MP3 images and all images in MP3 folders.
* Added Chapters support, based on MP3 files.
* Improved (both speed and quality) of mp3 decoding due to switch to ffmpeg from java based decoder.
* Improved tags support according to MP4 specification.
* Fixed tags encoding issues due caused by legacy MP3 problems.
* Fixed tags encoding issues with non-latin characters.
* Faac replaced with FFMpeg
* Added MP4V2 to support media for m4b (due to existing issue in ffmpeg https://trac.ffmpeg.org/ticket/2798)
* 64-bits only support.

--------------
History
--------------
Original version was developed by Florian Fankhauser.
<a href="https://github.com/yermak/AudioBookConverter/wiki/History">Read history</a>