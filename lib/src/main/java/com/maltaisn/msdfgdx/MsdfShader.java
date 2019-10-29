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


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;


/**
 * The shader used to render the text to the sprite batch.
 * The glyph uses the values encoded in the RGB channels (MSDF).
 * The shadow uses the values encoded in the alpha channel (SDF).
 * Since a standard SDF font also encodes values in the RGB channels,
 * this shader can also be used to render standard SDF fonts.
 *
 * References:
 * <ul>
 * <li>https://github.com/Chlumsky/msdfgen/files/3050967/thesis.pdf</li>
 * <li>https://github.com/Chlumsky/msdfgen/issues/36</li>
 * <li>http://inter-illusion.com/assets/I2SmartEdgeManual/SmartEdge.html?WhatSDFFormattouse.html</li>
 * </ul>
 */
public class MsdfShader extends ShaderProgram {

    public MsdfShader() {
        super(Gdx.files.classpath("font.vert"), Gdx.files.classpath("font.frag"));

        if (!isCompiled()) {
            throw new GdxRuntimeException("Distance field font shader compilation failed: " + getLog());
        }
    }

    public void updateForFont(MsdfFont font, FontStyle style) {
        // Texture size is available from the shader with textureSize(u_texture, 0), however it
        // doesn't work with OpenGL ES 2.0 and there doesn't seem be any built-in alternative...
        TextureRegion region = font.getFont().getRegion();
        setUniformf("u_textureSize", region.getRegionWidth(), region.getRegionHeight());

        // https://github.com/Chlumsky/msdfgen/issues/36#issuecomment-429240110
        setUniformf("distanceFactor", font.getDistanceRange() * style.getSize() / font.getGlyphSize());

        setUniformf("color", style.getColor());
        setUniformf("fontWeight", style.getWeight());

        setUniformf("shadowClipped", style.isShadowClipped() ? 1f : 0f);
        setUniformf("shadowColor", style.getShadowColor());
        setUniformf("shadowOffset", style.getShadowOffset());
        setUniformf("shadowSmoothing", style.getShadowSmoothing());

        setUniformf("innerShadowColor", style.getInnerShadowColor());
        setUniformf("innerShadowRange", style.getInnerShadowRange());
    }

}
