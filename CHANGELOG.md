## v0.2.1 (2022-12-30)
- Updated LibGDX to 1.11.0.

## v0.2.0 (2019-11-07)
- MsdfFontLoader now parses font file to get `size` (in info tag) and `distanceRange` 
(in common tag) attributes to avoid having to pass them in the loader parameter.
- Fixed font texture filters not set when loading without specifying parameter.

### v0.1.1 (2019-10-29)
- Changed antialiasing method, removing need for GL_OES_standard_derivatives OpenGL extensions in fragment shader which caused crashes on older OpenGL version/Open GL ES.

## v0.1.0 (2019-09-11)
- **Initial release**
- Shader for drawing MSDF text.
- Font style class with many options:
    - Font color, size and weight.
    - Glyph shadow with options for color, opacity, offset, smoothing and clipping.
    - Inner glyph shadow with options for color, opacity and smoothing.
- Font loader for integration with AssetManager.
