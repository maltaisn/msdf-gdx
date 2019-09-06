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

package com.maltaisn.msdfgdx.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.StringBuilder;
import com.maltaisn.msdfgdx.FontStyle;
import com.maltaisn.msdfgdx.MsdfFont;
import com.maltaisn.msdfgdx.MsdfShader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * A label widget to display distance field font text.
 * Note that unlike {@link Label}, this widget makes an internal copy of its text.
 *
 * This widget expects to find a {@link MsdfShader} named "default" in the skin.
 * The following should be done before: {@code skin.add("default", new MsdfShader())}
 */
public class MsdfLabel extends Label implements Disableable {

    private final Skin skin;
    private final MsdfShader shader;

    private MsdfFont font;
    private FontStyle fontStyle;

    private final StringBuilder txt = new StringBuilder();

    private boolean disabled = false;


    /**
     * Create a new label.
     *
     * @param text     The label initial text, can be null.
     * @param skin     The skin, used to retrieve the shader under the "default" name.
     * @param fontName The name of the font style obtained from the skin.
     */
    public MsdfLabel(@Nullable CharSequence text, @NotNull Skin skin, @NotNull String fontName) {
        this(text, skin, skin.get(fontName, FontStyle.class));
    }

    /**
     * Create a new label.
     *
     * @param text      The label initial text, can be null.
     * @param skin      The skin, used to retrieve the shader under the "default" name.
     * @param fontStyle The label font style.
     */
    @SuppressWarnings("ConstantConditions")
    public MsdfLabel(@Nullable CharSequence text, @NotNull Skin skin, @NotNull FontStyle fontStyle) {
        super(null, (LabelStyle) null);

        if (skin == null) throw new NullPointerException("Skin cannot be null");
        if (fontStyle == null) throw new NullPointerException("Font style cannot be null");

        this.skin = skin;
        this.shader = skin.get(MsdfShader.class);

        txt.append(text == null ? "" : text);
        setFontStyle(fontStyle);
    }


    @Override
    public void draw(@NotNull Batch batch, float parentAlpha) {
        // Draw the text
        batch.setShader(shader);
        shader.updateForFont(font, fontStyle);
        super.draw(batch, parentAlpha * (disabled ? 0.5f : 1f));
        batch.setShader(null);
    }

    public void setTxt(@Nullable CharSequence newText) {
        if (newText == null) {
            newText = "";
        }
        if (newText != txt) {
            txt.setLength(0);
            txt.append(newText);
        }

        if (fontStyle.isAllCaps()) {
            // Not very memory-efficient...
            newText = newText.toString().toUpperCase();
        }
        super.setText(newText);
    }

    @Override
    public void setText(@Nullable CharSequence newText) {
        setTxt(newText);
    }

    /**
     * Returns the label text that was set.
     * If font style is all caps, the original text will be returned.
     *
     * @return the text, never null.
     */
    public CharSequence getTxt() {
        return txt;
    }

    @NotNull
    @Override
    public StringBuilder getText() {
        return txt;
    }

    public void setFontStyle(@NotNull FontStyle fontStyle) {
        this.fontStyle = fontStyle;
        this.font = skin.get(fontStyle.getFontName(), MsdfFont.class);
        setFontScale(fontStyle.getSize() / font.getGlyphSize());
        super.setStyle(new LabelStyle(font.getFont(), Color.WHITE));
        setTxt(txt);
    }

    @NotNull
    public FontStyle getFontStyle() {
        return fontStyle;
    }

    public MsdfFont getFont() {
        return font;
    }

    /**
     * @deprecated Use {@link #setFillParent(boolean)}
     */
    @Deprecated
    @Override
    public void setStyle(@NotNull LabelStyle style) {
        // Do nothing. Can't throw exception since Label uses the method in its constructor.
    }

    /**
     * @deprecated Use {@link #getFontStyle()}
     */
    @Deprecated
    @Override
    @NotNull
    public LabelStyle getStyle() {
        throw new UnsupportedOperationException("Use getFontStyle() instead.");
    }

    /**
     * Set the label background drawable.
     *
     * @param background The new background, may be null.
     */
    public void setBackground(@Nullable Drawable background) {
        super.getStyle().background = background;
        invalidateHierarchy();
    }

    /**
     * @return The label's background, may be null if none is set.
     */
    @Nullable
    public Drawable getBackground() {
        return super.getStyle().background;
    }

    /**
     * Set whether the label is disabled or not.
     * Disabled label will be drawn at 50% alpha.
     * Not that this doesn't change the {@link #isTouchable()} status.
     *
     * @param disabled The new disabled state.
     */
    @Override
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }

}
