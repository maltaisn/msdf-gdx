# MSDF font library for LibGDX
Provides lightweight utilities to draw MSDF (multi-channel signed distance field) 
text on LibGDX. See LibGDX's [Distance field fonts][sdf-libgdx] wiki page for more information on
single channel SDF font. It also provides integration with scene2d and AssetManager. 
The library is written in Java but was also designed to be used seemlessly from Kotlin.

![Demo](demo.gif)

[MSDFA font][msdfa] files are used to draw text. This format combines both
multi channel (MSDF) and single channel (SDF) distance fields. 
The MSDF is encoded in 3 channels (RGB) and SDF is encoded in the alpha channel.
Each distance field has its advantages: MSDF is used to accurately draw glyphs with 
sharp corners at arbitrarly large sizes and SDF is used for shadow effects to keep 
round corners.

## Usage

##### Gradle dependency
```
implementation("com.maltaisn:msdf-gdx:X.Y.Z")
```
Replace `X.Y.Z` with the lastest version: [![Maven Central](https://img.shields.io/maven-central/v/com.maltaisn/msdf-gdx)](https://search.maven.org/artifact/com.maltaisn/msdf-gdx)

#### MsdfFont
The shader needs a `MsdfFont` object to work, which is a simple wrapper around BitmapFont.
The `MsdfFont` can be created from an existing BitmapFont or from a *.fnt* file. 
You can also use the AssetManager to load one:
```java
// Load the font with the asset manager.
AssetManager manager = new AssetManager();
manager.setLoader(MsdfFont.class, new MsdfFontLoader(new InternalFileHandleResolver()));
manager.load("roboto.fnt", MsdfFont.class);
manager.finishLoading();
MsdfFont font = manager.get("roboto.png");

// Add the font to a skin.
Skin skin = new Skin();
skin.add("roboto", font);
```
When creating a font, the glyph size and the distance range must be specified. When using the `MsdfFontLoader`, 
this can either be specified directly in the *.fnt* file or with a `MsdfFontParameter` loader parameter.

#### FontStyle
Font styles can be created by code:
```java
FontStyle fontStyle = new FontStyle()
        .setFontName("roboto")
        .setColor(Color.WHITE)
        .setSize(48f);
```
Or in a skin file:
```libgdxjson
com.maltaisn.msdfgdx.FontStyle: {
    titleFontStyle: {
        fontName: roboto
        color: { hex: #ffffff }
        size: 48
    }
}
```

Here's the list of options in a font style:
- **fontName**: Font name. When used with `MsdfLabel`, the font name is the name of the `MsdfFont` 
  in the skin used to create the label.
- **size**: Text size in pixels.
- **weight**: Text weight (light, bold) between -0.5 and 0.5.
- **color**: Text color
- **allCaps**: All caps text is uppercased automatically when used in `MsdfLabel`.
- **shadowClipped**: Whether shadow should appear behind glyph if glyph color is translucent.
- **shadowColor**: Color of the shadow. Default is transparent.
- **shadowOffset**: Shadow offset in pixels relative to glyph size.
- **shadowSmoothing**: Shadow smoothing between 0 and 0.5.
- **innerShadowColor**: Inner shadow color. Default is transparent.
- **innerShadowRange**: Inner shadow range from 0 to 0.5.

#### MsdfLabel
A subclass of Label used to render MSDF text in scene2d. The label is constructed using a skin instance
and a font style. The skin must contain the shader under the "default" name and the `MsdfFont`.
```java
Skin skin = new Skin();
skin.add("default", new MsdfShader());
skin.add("roboto", new MsdfFont(Gdx.files.internal("roboto.fnt"), 32f, 5f));

FontStyle fontStyle = new FontStyle()
        .setFontName("roboto")
        .setSize(48f);
MsdfLabel label = new MsdfLabel("My text", skin, fontStyle);
stage.addActor(label);
```

Label is the only widget provided by the library. You can make your own MsdfTextField implementation
by applying the code below for drawing text. However, `Label` is the only LibGDX widget to provide
a `setFontScale`, so other widgets may not be able to draw MSDF text by subclassing. 
(see [this issue](https://github.com/libgdx/libgdx/issues/5719))

#### Drawing text
You can also draw text without using MsdfLabel:
```java
MsdfShader shader = skin.get(MsdfShader.class);
MsdfFont font = skin.get("roboto", MsdfFont.class);
BitmapFont bmfont = font.getFont();

batch.setShader(shader);
bmfont.getData().setScale(fontStyle.getSize() / font.getGlyphSize());
shader.updateForFont(font, fontStyle);
bmfont.draw(batch, "My text", 100f, 100f);
batch.setShader(null);
```

## Generating fonts
Here are your options:
- I made a small utility for generating font files. It works great as far as I have tested 
it but it might not be perfect. [Check it out here][gen-util-old].
- You can also generate MSDF and SDF font files with other programs and
combine them manually with an image editing program. [A tutorial is available here][gen-util-old].
- The library can also render plain MSDF/SDF just fine and without having to change 
anything. The alpha channel must be encoded with SDF for shadows to work though.

## Changelog
See [changelog](CHANGELOG.md).

# License
- Code is licensed under [Apache License, Version 2.0](LICENSE).
- Test font (Roboto) is licensed under Apache License, Version 2.0.

### References
- [msdfgen][msdfgen] by Chlumsky who developed the MSDF technique.
- [msdf-bmfont-xml][msdf-bmfont-xml] by soimy, used to create font texture atlas.
- [SmartEdge's MSDFA font][msdfa], explains the advantages of MSDFA vs MSDF.
- [Font effects shader example][effects-shader] by Chlumsky, used to develop shadow effect.
- [Improved antialiasing shader][better-aa] used by the library.
- [LibGDX's tutorial on distance field fonts][sdf-libgdx] for basic distance field usage.


[msdfa]: http://inter-illusion.com/assets/I2SmartEdgeManual/SmartEdge.html?WhatSDFFormattouse.html
[msdf-bmfont-xml]: https://github.com/soimy/msdf-bmfont-xml
[msdfgen]: https://github.com/Chlumsky/msdfgen
[xml-to-fnt]: utils/bmfont_converter.py
[charset]: utils/charset.txt
[charset-wiki]: https://en.wikipedia.org/wiki/ISO/IEC_8859-15
[sdf-libgdx]: https://github.com/libgdx/libgdx/wiki/Distance-field-fonts
[effects-shader]: https://gist.github.com/Chlumsky/263c960ae0a7df59afc2da4051eb0553
[better-aa]: https://github.com/Chlumsky/msdfgen/issues/36
[gen-util]: gen/README.md
[gen-util-old]: utils/README.md
