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

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.*
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.maltaisn.msdfgdx.FontStyle
import com.maltaisn.msdfgdx.widget.MsdfLabel
import ktx.actors.onClick
import ktx.style.get


class TestLayout(skin: Skin) : Table(skin) {

    private val style: TestLayoutStyle = skin.get()

    private val buttonTable = Table()

    private val fontStyle = FontStyle().apply {
        fontName = FONT_NAMES.first()
        color = FONT_COLORS.first()

    }

    private val textScrollPane: ScrollPane
    private val labels = mutableListOf<MsdfLabel>()

    init {
        val textTable = Table()
        textTable.pad(20f)
        textTable.background = style.background
        textTable.color = BG_COLORS.first()

        textScrollPane = ScrollPane(textTable)
        textScrollPane.setOverscroll(false, false)
        add(buttonTable).pad(20f).growY()
        add(textScrollPane).grow()

        val sizeIndicator = addBtn("") {}
        sizeIndicator.enabled = false

        // Add labels
        repeat(20) {
            val label = MsdfLabel(TEXTS.first(), skin, fontStyle)
            label.touchable = Touchable.enabled
            label.addListener(object : InputListener() {
                override fun enter(event: InputEvent?, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                    if (pointer == -1) {
                        label.debug = true
                        sizeIndicator.title = "Size: ${label.fontStyle.size.toInt()}"
                    }
                }

                override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                    if (pointer == -1) {
                        label.debug = false
                        sizeIndicator.title = "Size: --"
                    }
                }
            })

            textTable.add(label).growX().row()
            labels += label
        }
        updateFontStyle()

        // Add action buttons
        addEnumBtn("Change text", TEXTS, null) { _, text ->
            for (label in labels) {
                label.txt = text
            }
        }
        addEnumBtn("Font", FONT_NAMES, FONT_NAMES) { _, fontName ->
            fontStyle.fontName = fontName
            updateFontStyle()
        }
        addEnumBtn("Color", FONT_COLORS, FONT_COLOR_NAMES) { _, color ->
            fontStyle.color = color
            stage?.debugColor?.set(fontStyle.color)
            updateFontStyle()
        }
        addEnumBtn("BG color", BG_COLORS, BG_COLOR_NAMES) { _, color ->
            textTable.color = color
        }
        addToggleBtn("All caps") { _, allCaps ->
            fontStyle.isAllCaps = allCaps
            updateFontStyle()
        }

        buttonTable.add().grow().row()
    }

    override fun setStage(stage: Stage?) {
        super.setStage(stage)
        stage?.debugColor?.set(fontStyle.color)
        stage?.scrollFocus = textScrollPane
    }

    private fun updateFontStyle() {
        for ((i, label) in labels.withIndex()) {
            val style = FontStyle(fontStyle)
            style.size = 10f + i * 10f
            if (i == 19) {
                style.size = 512f
            }
            label.fontStyle = style
        }
    }

    private fun addBtn(title: String, action: (MsdfButton) -> Unit): MsdfButton {
        val btn = MsdfButton(skin, title)
        btn.onClick { action(btn) }
        buttonTable.add(btn).align(Align.top).growX().padBottom(20f).row()
        return btn
    }

    private fun addToggleBtn(title: String, action: (MsdfButton, checked: Boolean) -> Unit): MsdfButton {
        val btn = addBtn(title) { action(it, it.checked) }
        btn.checkable = true
        return btn
    }

    private fun <T> addEnumBtn(title: String, values: List<T>,
                               valueTitles: List<Any?>? = values,
                               initialIndex: Int = 0,
                               action: (MsdfButton, value: T) -> Unit): MsdfButton {
        var selectedIndex = initialIndex
        fun getTitle() = title + if (valueTitles != null) ": ${valueTitles[selectedIndex]}" else ""
        return addBtn(getTitle()) {
            selectedIndex = (selectedIndex + 1) % values.size
            it.title = getTitle()
            action(it, values[selectedIndex])
        }
    }


    class TestLayoutStyle {
        lateinit var background: Drawable
    }

    companion object {
        val TEXTS = listOf("The quick brown fox jumps over the lazy dog",
                "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz",
                "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýÿþ",
                "!\"#\$%&'()*+,-./:;<=>?[\\]^_`{|}~¡¢£€¥Š§š©ª«¬®¯°±²³Žµ¶·ž¹º»ŒœŸ¿×")

        val FONT_NAMES = listOf("roboto")

        val FONT_COLORS = listOf(Color.BLACK, Color.WHITE, Color.BLUE, Color.RED)
        val FONT_COLOR_NAMES = listOf("black", "white", "blue", "red")

        val BG_COLORS = listOf(Color.WHITE, Color.BLACK, Color.YELLOW, Color.CYAN)
        val BG_COLOR_NAMES = listOf("white", "black", "yellow", "cyan")
    }

}
