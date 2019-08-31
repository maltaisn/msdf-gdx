@echo off

rem %1 should be a .ttf file.
rem Generate a MSDF texture atlas and XML .fnt file.
rem The texture atlas is square, as small as possible and uses power of twos for dimensions.
rem Glyph size is 42px and distance range is 4px.
rem The script expects a charset text file with chars to generate, named "charset.txt".
rem This requires having https://github.com/soimy/msdf-bmfont-xml globally installed with npm.

msdf-bmfont -f xml -i charset.txt -s 42 -r 4 -t msdf --pot --square --smart-size %1
pause
