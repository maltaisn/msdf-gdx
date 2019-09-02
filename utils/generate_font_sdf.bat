@echo off
msdf-bmfont -f xml -i charset.txt -s 42 -r 6 -t sdf --pot --square --smart-size %1
pause
