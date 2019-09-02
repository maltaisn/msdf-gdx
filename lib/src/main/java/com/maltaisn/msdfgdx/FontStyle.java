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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;


public final class FontStyle {

    public static final float WEIGHT_LIGHT = -0.1f;
    public static final float WEIGHT_REGULAR = 0f;
    public static final float WEIGHT_BOLD = 0.1f;


    /**
     * The font face for this font style. Must not be null.
     */
    @NotNull
    private String fontName = "default";

    /**
     * The font size in pixels.
     */
    private float size = 42f;

    /**
     * The font weight, from -0.5 to 0.5. Higher values result in thicker fonts.
     * The resulting effect depends on {@link MsdfFont#getDistanceRange()} and values
     * near -0.5 and 0.5 will most always produce rendering artifacts.
     * 0 should always look the most like the original font.
     */
    private float weight = WEIGHT_REGULAR;

    /**
     * The font color, cannot be null.
     */
    @NotNull
    private Color color = Color.BLACK;


    /**
     * Whether only capital letters should be displayed or not.
     */
    private boolean allCaps = false;

    /**
     * The color of the outer shadow, can be translucent.
     * Use transparent for no shadow.
     */
    @NotNull
    private Color shadowColor = new Color();

    /**
     * The drawn shadow offset in pixels, relative to the size of glyph in the font image.
     * Offset is in a Y positive down coordinate system.
     * Placing the shadow too far from the glyph can create 2 issues:
     * <ul>
     * <li>Other glyphs of the texture atlas may appear on the sides. Increasing the padding
     * value when generating the font can prevent it.</li>
     * <li>Some parts of the shadow may be cut by the next glyph drawn.</li>
     * </ul>
     */
    @NotNull
    private Vector2 shadowOffset = new Vector2(0, 0);

    /**
     * Defines the smoothess of the shadow edges. Value should be between 0 to 0.5.
     * A value of 0 looks rough because it doesn't have antialiasing.
     */
    private float shadowSmoothing = 0.1f;


    /**
     * The color of the inner shadow, can be translucent.
     * Use transparent for no shadow.
     */
    @NotNull
    private Color innerShadowColor = new Color();

    /**
     * The inner shadow range, from 0 to 0.5.
     */
    private float innerShadowRange = 0.3f;


    public FontStyle() {
        // Default constructor
    }

    public FontStyle(FontStyle style) {
        fontName = style.fontName;
        size = style.size;
        weight = style.weight;
        color = style.color.cpy();
        allCaps = style.allCaps;
        shadowOffset = style.shadowOffset.cpy();
        shadowColor = style.shadowColor.cpy();
        shadowSmoothing = style.shadowSmoothing;
        innerShadowColor = style.innerShadowColor.cpy();
        innerShadowRange = style.innerShadowRange;
    }


    @NotNull
    public String getFontName() {
        return fontName;
    }

    public FontStyle setFontName(@NotNull String fontName) {
        //noinspection ConstantConditions
        if (fontName == null) throw new NullPointerException("Font name cannot be null.");

        this.fontName = fontName;
        return this;
    }

    public float getSize() {
        return size;
    }

    public FontStyle setSize(float size) {
        this.size = size;
        return this;
    }

    public float getWeight() {
        return weight;
    }

    public FontStyle setWeight(float weight) {
        this.weight = weight;
        return this;
    }

    @NotNull
    public Color getColor() {
        return color;
    }

    public FontStyle setColor(@NotNull Color color) {
        //noinspection ConstantConditions
        if (color == null) throw new NullPointerException("Color cannot be null.");

        this.color = color;
        return this;
    }

    public boolean isAllCaps() {
        return allCaps;
    }

    public FontStyle setAllCaps(boolean allCaps) {
        this.allCaps = allCaps;
        return this;
    }

    @NotNull
    public Color getShadowColor() {
        return shadowColor;
    }

    public FontStyle setShadowColor(@NotNull Color shadowColor) {
        //noinspection ConstantConditions
        if (shadowColor == null) throw new NullPointerException("Shadow color cannot be null.");

        this.shadowColor = shadowColor;
        return this;
    }

    @NotNull
    public Vector2 getShadowOffset() {
        return shadowOffset;
    }

    public FontStyle setShadowOffset(@NotNull Vector2 shadowOffset) {
        //noinspection ConstantConditions
        if (shadowColor == null) throw new NullPointerException("Shadow offset cannot be null.");

        this.shadowOffset = shadowOffset;
        return this;
    }

    public float getShadowSmoothing() {
        return shadowSmoothing;
    }

    public FontStyle setShadowSmoothing(float shadowSmoothing) {
        this.shadowSmoothing = shadowSmoothing;
        return this;
    }

    @NotNull
    public Color getInnerShadowColor() {
        return innerShadowColor;
    }

    public FontStyle setInnerShadowColor(@NotNull Color innerShadowColor) {
        this.innerShadowColor = innerShadowColor;
        return this;
    }

    public float getInnerShadowRange() {
        return innerShadowRange;
    }

    public FontStyle setInnerShadowRange(float innerShadowRange) {
        this.innerShadowRange = innerShadowRange;
        return this;
    }


    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("FontStyle{");
        str.append("fontName='");
        str.append(fontName);
        str.append("', size=");
        str.append(size);
        str.append(", weight=");
        str.append(weight);
        str.append(", color=");
        str.append(color);
        str.append(", allCaps=");
        str.append(allCaps);
        if (shadowColor.a != 0) {
            str.append(", shadowColor=");
            str.append(shadowColor);
            str.append(", shadowOffset=");
            str.append(shadowOffset);
            str.append(", shadowSmoothing=");
            str.append(shadowSmoothing);
        }
        if (innerShadowColor.a != 0) {
            str.append(", innerShadowColor=");
            str.append(innerShadowColor);
        }
        str.append('}');
        return str.toString();
    }

}
