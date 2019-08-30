package com.maltaisn.msdfgdx.test.core

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport

class TestApp : ApplicationListener {

    private lateinit var stage: Stage
    private val assetManager = AssetManager()


    override fun create() {
        stage = Stage(ExtendViewport(1920f, 1080f))

        // Do the stage layout
        stage.addActor(TestLayout().apply {
            setFillParent(true)
        })
    }

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
