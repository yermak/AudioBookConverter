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


MP3 to iPod/iPhone Audio Book Converter
=============



Legacy
=============
Author
---------
Florian Fankhauser


Desciption
---------
* Convert MP3 audiobooks to iPod and iPhone audio books file format.
* Use your iPod's Audiobook Features.
* Combine multible MP3 files into a single file for seamless listening.
* Easy to use. Free. Open Source.


About
--------
MP3 to iPod/iPhone Audio Book Converter converts any number of MP3 files into one big iPod Audio Book File. The advantage of doing this is that you can make use of the advanced audio book functionality of your iPod.

Your iPod or iPhone remembers the last position you were listening in your audio book. So you can alway start listening where you stopped the last time. Even if you were listening to music or another audio book in the meantime. Read more about your iPod's Audio Book features.

This feature gets really valuable for audio books consiting of many small files. With this software you can combine all this files to a single file and you don't have to worry anymore on which file you stopped listening the last time.

MP3 to iPod Audio Book Converter runs on Windows 98, Me, 2000, XP, Vista and Windows 7. There is no version for Mac or Linux yet.


Development
--------
Before i wrote this program, i used two free command line programs for this kind of conversion. Faac and madplay. This solution worked fine, but was not very convenient. So I decided to write a simple gui frontend. And MP3 to iPod Audio Book Converter is the result. I released it under the GNU General Public License in hope someone may find it usefull, too.


Enjoy it!
--------------------

