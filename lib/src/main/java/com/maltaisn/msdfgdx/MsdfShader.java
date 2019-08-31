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
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;


/**
 * The shader used to render the MSDF text to the sprite batch.
 */
public class MsdfShader extends ShaderProgram {

    public MsdfShader() {
        super(Gdx.files.classpath("font.vert"), Gdx.files.classpath("font.frag"));

        if (!isCompiled()) {
            throw new GdxRuntimeException("Distance field font shader compilation failed: " + getLog());
        }
    }

    public void updateForFont(MsdfFont font, FontStyle style) {
        setUniformf("distanceRange", font.getDistanceRange());
        setUniformf("fontWeight", style.getWeight());
    }

}
