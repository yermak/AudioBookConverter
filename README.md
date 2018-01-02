Audio Book Converter V2
===============
* Convert Audiobook from mp3 to m4b.
* Convert mp3 to ipod format.
* Convert mp3 to IBook (IPhone) format.

This project is based on freeipodsoftware release of AudioBookConverter.


Original look and feel is preserved, all internals were completely rewritten to use benefits of modern hardware: 64bits, multi-core processors.
All libraries and dependencies are refreshed to the latests version available at this moment.
Project is currently in alpha as has some minor ui bugs (progress bar, estimated file size, etc), however major functions work as expected.
There is existing bugs with tags in unicode, so I highly recommend to stay with latin charset.


--------------
Major differences vs original version:
--------------
* Performance improved form 5 to 15x times (depending on numbers of cores), old version of Faac replaced with fresh FFMpeg.
* Improved (both speed and quality) of mp3 decoding due to switch to ffmpeg.
* Improved tag support according to MP4 specification.
* Added chapters support via MP4v2 project (Thanks to https://github.com/TechSmith for maintaining the fork).
* Added super-fast experimental mode of parallel encoding of MP3 files. (Works well for me).
* Keeping the same bitrate as original files to preserve quality




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

