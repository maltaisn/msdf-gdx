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

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.maltaisn.msdfgdx.FontStyle
import com.maltaisn.msdfgdx.widget.MsdfLabel
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.style.get


class MsdfButton(skin: Skin, text: CharSequence? = null) : Table(skin) {

    private val style: MsdfButtonStyle = skin.get()


    var title: CharSequence?
        get() = titleLabel.txt
        set(value) {
            titleLabel.txt = value
        }

    var enabled = true
        set(value) {
            field = value
            if (!value) {
                hovered = false
                pressed = false
            }
        }

    var checked = false

    var checkable = false
        set(value) {
            field = value
            if (!value) {
                checked = false
            }
        }

    private var hovered = false
    private var pressed = false


    private val titleLabel = MsdfLabel(text, skin, style.titleFontStyle)


    init {
        touchable = Touchable.enabled
        titleLabel.touchable = Touchable.disabled

        add(titleLabel).grow().pad(20f)

        addListener(object : InputListener() {
            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                if (event.pointer == -1) {
                    hovered = enabled
                }
            }

            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                if (event.pointer == -1) {
                    hovered = false
                }
            }

            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (button == Input.Buttons.LEFT && enabled) {
                    pressed = true
                }
                return true
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                pressed = false
            }
        })
        onClick {
            if (enabled && checkable) {
                checked = !checked
            }
        }
    }

    override fun drawChildren(batch: Batch, parentAlpha: Float) {
        val alpha = alpha * parentAlpha * if (enabled) 1f else 0.6f
        val bgAlpha = alpha * (0.15f + (if (hovered) 0.05f else 0f) +
                (if (pressed) 0.05f else 0f) + (if (checked) 0.1f else 0f))

        // Draw background
        batch.setColor(color.r, color.g, color.b, bgAlpha)
        style.background.draw(batch, x, y, width, height)

        // Draw button content
        super.drawChildren(batch, alpha)
    }

    class MsdfButtonStyle {
        lateinit var background: Drawable
        lateinit var titleFontStyle: FontStyle
    }

}
