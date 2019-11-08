## Bitmap font generation with msdf-bmfont-xml and Gimp

This is the previous method that was used for generating font, which isn't as straightforward
as the new method since there are a few steps that must be done manually and more
things have to be installed. I recommended using the [small utility](../gen/README.md) 
I made instead to simplify the process.

If you still want to use this method, you'll need the following:
- [msdf-bmfont-xml][msdf-bmfont-xml], a npm package for generating MSDF font texture atlas and `.fnt` file.
  This requires npm installed, then running `npm install msdf-bmfont-xml -g` will install the package globally. 
- Gimp or any image editing tool that can manipulate channels and PNG. This tutorial uses Gimp.
- [The python script][xml-to-fnt] to convert the XML *.fnt* file into the format that LibGDX's BitmapFont uses.
  The script requires Python 3.

Here's the steps to generate the files from a *font.ttf* font file.

1. Generate the SDF font with the command below. You can change the glyph size `-s`, 
   the distance range `-r` (the range in px to encode the distance field) and the charset `-i` file.
   Here's a [great charset to use][charset] for complete coverage in over 30 languages ([latin-9][charset-wiki]).
   ```text
   msdf-bmfont -f xml -i charset.txt -s 32 -r 5 -t sdf --pot --smart-size font.ttf
   ```
   If you're planning on using the outer shadow effect, adding a few pixels of padding on the border
   and between glyph is a good idea. You can do this with the `-p` (padding) and `-b` (border) arguments.
   Because the shadow has some offset, neighboring glyphs in the atlas may appear if padding is too small.

2. Rename the generated PNG file to *font-sdf.png*
3. Generate the MSDF font just like the SDF font but changing the `-t` parameter.
   It is important to keep the same parameters used for the MSDF font since they will be merged afterwards.
   ```text
   msdf-bmfont -f xml -i charset.txt -s 32 -r 5 -t msdf --pot --smart-size font.ttf
   ```
   
4. Run the python script to convert the generated *font.fnt* file to LibGDX's format.
   ```text
   python bmfont_converter.py font.fnt
   ```
  
5. Use Gimp to merge both distance fields:
    - Open the MSDF png file.
    - Add a new layer and copy the SDF png file there.
    - Select SDF layer and click Layer > Transparency > Alpha to Selection.
    - Select MSDF layer and click Layer > Mask > Add layer mask...
    - Initialize layer mask to "Selection" and click Add.
    - Remove the SDF layer.
    - Export image to PNG under *font.png*.
        - "Save color values from transparent pixels" option must be checked.
        - Reduce size by unchecking options like "Save thumbnail" and increasing compression level.
6. Make sure the font atlas file name in *.fnt* file is correct. (The `file=...` attribute)
7. For some reason, the *font.fnt* `base=...` attribute will be too small, so increment it by a few pixels
   until it looks good. I added about 5px for a 32px font for reference.

The result should look like this:
![MSDFA Roboto font](../test/assets/font/roboto-32.png)

The library can also render normal MSDF and SDF but shadows won't work.


[msdf-bmfont-xml]: https://github.com/soimy/msdf-bmfont-xml
[xml-to-fnt]: bmfont_converter.py
[charset]: charset.txt
[charset-wiki]: https://en.wikipedia.org/wiki/ISO/IEC_8859-15
