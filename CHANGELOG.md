### v0.1.1
- Removed need for GL_OES_standard_derivatives OpenGL extensions in fragment shader. Caused crashes on older OpenGL version/Open GL ES.

## v0.1.0
- **Initial release**
- Shader for drawing MSDF text.
- Font style class with many options:
    - Font color, size and weight.
    - Glyph shadow with options for color, opacity, offset, smoothing and clipping.
    - Inner glyph shadow with options for color, opacity and smoothing.
- Font loader for integration with AssetManager.
