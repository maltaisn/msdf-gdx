@echo off
msdf-bmfont -f xml -i charset.txt -s 32 -r 5 -t sdf --pot --smart-size %1
pause
