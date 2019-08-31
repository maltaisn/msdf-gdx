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

import org.jetbrains.annotations.NotNull;


public final class FontStyle {

    public static final float WEIGHT_LIGHT = 0.3f;
    public static final float WEIGHT_REGULAR = 0.5f;
    public static final float WEIGHT_BOLD = 0.7f;


    /**
     * The font face for this font style. Must not be null.
     */
    @NotNull
    private String fontName = "default";

    /**
     * The font size in pixels.
     */
    private float size;

    /**
     * The font weight, from 0 to 1.
     * Higher values result in thicker fonts.
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


    public FontStyle() {
        // Default constructor
    }

    public FontStyle(FontStyle style) {
        this.fontName = style.fontName;
        this.size = style.size;
        this.weight = style.weight;
        this.color = style.color;
        this.allCaps = style.allCaps;
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

}
