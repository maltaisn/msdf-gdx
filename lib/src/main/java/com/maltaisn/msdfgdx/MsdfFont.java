/*
 * Copyright 2019 Nicolas Maltais
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.maltaisn.msdfgdx;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import org.jetbrains.annotations.NotNull;


/**
 * Simple wrapper around {@link BitmapFont} to provide values
 * for the glyph size and distance range needed for rendering.
 */
public final class MsdfFont {

    /**
     * The underlying bitmap font, never null.
     */
    private final BitmapFont font;

    /**
     * The glyphs size in the font texture atlas.
     * This is needed to correctly set the font scale when drawing text.
     * This corresponds to the {@code -s, --font-size} argument
     * in <a href="https://github.com/soimy/msdf-bmfont-xml">msdf-bmfont-xml</a>.
     */
    private final float glyphSize;

    /**
     * The range in pixels around the glyphs used to encore the distance field data.
     * This corresponds to the {@code -r, --distance-range} argument
     * in <a href="https://github.com/soimy/msdf-bmfont-xml">msdf-bmfont-xml</a>.
     */
    private final float distanceRange;


    /**
     * Create a font from a .fnt file and a .png image file with the same name.
     */
    public MsdfFont(@NotNull FileHandle fontFile, float glyphSize, float distanceRange) {
        this(fontFile, fontFile.sibling(fontFile.nameWithoutExtension() + ".png"),
                glyphSize, distanceRange);
    }

    /**
     * Create a font from a .fnt file and an image file.
     */
    public MsdfFont(@NotNull FileHandle fontFile, @NotNull FileHandle fontRegionFile,
                    float glyphSize, float distanceRange) {
        this(fontFile, getFontRegionFromFile(fontRegionFile), glyphSize, distanceRange);
    }

    /**
     * Create a font from a .fnt file and a texture region.
     */
    public MsdfFont(@NotNull FileHandle fontFile, @NotNull TextureRegion fontRegion,
                    float glyphSize, float distanceRange) {
        this(new BitmapFont(fontFile, fontRegion), glyphSize, distanceRange);
    }

    /**
     * Create a font from a bitmap font.
     */
    public MsdfFont(@NotNull BitmapFont font, float glyphSize, float distanceRange) {
        //noinspection ConstantConditions
        if (font == null) throw new NullPointerException("Font cannot be null");
        this.font = font;
        this.glyphSize = glyphSize;
        this.distanceRange = distanceRange;
    }


    @NotNull
    public BitmapFont getFont() {
        return font;
    }

    public float getGlyphSize() {
        return glyphSize;
    }

    public float getDistanceRange() {
        return distanceRange;
    }


    private static TextureRegion getFontRegionFromFile(FileHandle file) {
        Texture texture = new Texture(file, Pixmap.Format.RGB888, true);
        texture.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Linear);
        return new TextureRegion(texture);
    }

    @Override
    public String toString() {
        return "MsdfFont{" +
                "font=" + font +
                ", glyphSize=" + glyphSize +
                ", distanceRange=" + distanceRange +
                '}';
    }

}
