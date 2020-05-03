cd v%1
choco pack
choco push audiobookconverter.%1.nupkg -s https://push.chocolatey.org/
del audiobookconverter.%1.nupkg