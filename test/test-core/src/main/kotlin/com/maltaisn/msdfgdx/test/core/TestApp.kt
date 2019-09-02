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

package com.maltaisn.msdfgdx.test.core

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.SkinLoader
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.maltaisn.msdfgdx.MsdfFont
import com.maltaisn.msdfgdx.MsdfShader
import ktx.assets.loadOnDemand

class TestApp : ApplicationListener {

    private lateinit var stage: Stage
    private val assetManager = AssetManager()


    override fun create() {
        stage = Stage(ExtendViewport(1920f, 1080f))
        Gdx.input.inputProcessor = stage

        // Load skin
        val skin = assetManager.loadOnDemand<Skin>(TestRes.SKIN,
                SkinLoader.SkinParameter(TestRes.ATLAS)).asset

        // Create fonts and shader and add them to skin
        skin.add("default", MsdfShader())

        skin.addTestFont("roboto", TestRes.FONT_ROBOTO)
        skin.addTestFont("roboto-sdf", TestRes.FONT_ROBOTO_SDF)
        skin.addTestFont("roboto-bold", TestRes.FONT_ROBOTO_BOLD)
        skin.addTestFont("roboto-mono", TestRes.FONT_ROBOTO_MONO)
        skin.addTestFont("satisfy", TestRes.FONT_SATISFY)
        skin.addTestFont("lora", TestRes.FONT_LORA)

        // Do the stage layout
        val layout = TestLayout(skin)
        layout.setFillParent(true)
        stage.addActor(layout)
    }

    private fun Skin.addTestFont(name: String, fileName: String) =
            add(name, MsdfFont(Gdx.files.internal(fileName), 42f, 4f))

    override fun render() {
        stage.act()

        Gdx.gl.glClearColor(0.95f, 0.95f, 0.95f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.draw()
    }

    override fun pause() = Unit

    override fun resume() = Unit

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
        assetManager.dispose()
    }

}
