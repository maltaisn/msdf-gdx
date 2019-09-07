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
import com.maltaisn.msdfgdx.FontStyle
import com.maltaisn.msdfgdx.widget.MsdfLabel
import ktx.actors.alpha
import ktx.style.get
import java.text.NumberFormat


class TestLayout(skin: Skin) : Table(skin) {

    private val style: TestLayoutStyle = skin.get()

    private val fontStyle = FontStyle().apply {
        fontName = FONT_NAMES[2]
        color = FONT_COLORS.first().cpy()
        weight = FontStyle.WEIGHT_REGULAR
        shadowColor.set(1f, 1f, 1f, 0f)
        shadowOffset.set(2f, 2f)
        innerShadowColor.set(0f, 0f, 0f, 0f)
    }

    private val textScrollPane: ScrollPane
    private val labels = mutableListOf<MsdfLabel>()

    init {
        val textTable = Table()
        textTable.pad(20f)
        textTable.background = style.background
        textTable.color = BG_COLORS.first()

        val btnTable = ButtonTable(skin)
        val btnScrollPane = ScrollPane(btnTable)
        btnScrollPane.setScrollingDisabled(true, false)
        btnScrollPane.setOverscroll(false, false)
        btnTable.addListener(object : InputListener() {
            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                stage.scrollFocus = btnTable
            }
        })

        textScrollPane = ScrollPane(textTable)
        textScrollPane.setOverscroll(false, false)
        textScrollPane.addListener(object : InputListener() {
            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                stage.scrollFocus = textScrollPane
            }
        })

        // Do the layout
        add(btnScrollPane).width(450f).growY()
        add(textScrollPane).grow()

        // Add labels
        val sizeIndicator = btnTable.addBtn("Size: --") {}
        sizeIndicator.enabled = false

        repeat(SIZES.size) {
            val label = MsdfLabel(TEXTS.first(), skin, fontStyle)
            label.touchable = Touchable.enabled
            label.addListener(object : InputListener() {
                override fun enter(event: InputEvent?, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                    if (pointer == -1) {
                        label.debug = true
                        sizeIndicator.title = "Size: ${label.fontStyle.size.toInt()} px"
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
        btnTable.apply {
            lateinit var shadowOpacityBtn: ButtonTable.ValueMsdfBtn
            lateinit var inShadowOpacityBtn: ButtonTable.ValueMsdfBtn

            addToggleBtn("Disabled") { _, disabled ->
                for (label in labels) {
                    label.isDisabled = disabled
                }
            }
            addValueBtn("Batch alpha", 0f, 1f, 1f, -0.1f,
                    NumberFormat.getPercentInstance()) { _, alpha, _ ->
                textTable.alpha = alpha
            }
            addEnumBtn("Change text", TEXTS, null) { _, text ->
                for (label in labels) {
                    label.txt = text
                }
            }
            addEnumBtn("Font", FONT_NAMES, initialIndex = 2) { _, fontName ->
                fontStyle.fontName = fontName
                updateFontStyle()
            }
            addValueBtn("Weight", -0.5f, 0.5f, 0f, 0.05f) { _, weight, _ ->
                fontStyle.weight = weight
                updateFontStyle()
            }
            addEnumBtn("Color", FONT_COLORS, FONT_COLOR_NAMES) { _, color ->
                val fontColor = color.cpy()
                fontColor.a = fontStyle.color.a
                fontStyle.color = fontColor
                stage?.debugColor?.set(fontColor)
                updateFontStyle()
            }
            addValueBtn("Opacity", 0f, 1f, 1f, -0.1f,
                    NumberFormat.getPercentInstance()) { _, opacity, _ ->
                fontStyle.color.a = opacity
                updateFontStyle()
            }
            addEnumBtn("Background color", BG_COLORS, BG_COLOR_NAMES) { _, color ->
                textTable.color = color.cpy()
            }
            addToggleBtn("All caps") { _, allCaps ->
                fontStyle.isAllCaps = allCaps
                updateFontStyle()
            }
            addToggleBtn("Draw shadow") { _, shadowDrawn ->
                fontStyle.shadowColor.a = if (shadowDrawn) shadowOpacityBtn.value else 0f
                updateFontStyle()
            }
            addToggleBtn("Clip shadow") { _, clip ->
                fontStyle.isShadowClipped = clip
                updateFontStyle()
            }
            addEnumBtn("Shadow color", SHADOW_COLORS, SHADOW_COLOR_NAMES) { _, color ->
                val shadowColor = color.cpy()
                shadowColor.a = fontStyle.shadowColor.a
                fontStyle.shadowColor = shadowColor
                updateFontStyle()
            }
            shadowOpacityBtn = addValueBtn("Shadow opacity", 0f, 1f, 1f, -0.1f,
                    NumberFormat.getPercentInstance()) { _, opacity, _ ->
                fontStyle.shadowColor.a = opacity
                updateFontStyle()
            }
            addValueBtn("Shadow offset X", -4f, 4f, 2f, 0.5f) { _, offset, _ ->
                fontStyle.shadowOffset.x = offset
                updateFontStyle()
            }
            addValueBtn("Shadow offset Y", -4f, 4f, 2f, 0.5f) { _, offset, _ ->
                fontStyle.shadowOffset.y = offset
                updateFontStyle()
            }
            addValueBtn("Shadow smoothing", 0f, 0.5f, 0.1f, 0.1f) { _, smoothing, _ ->
                fontStyle.shadowSmoothing = smoothing
                updateFontStyle()
            }
            addToggleBtn("Draw in shadow") { _, shadowDrawn ->
                fontStyle.innerShadowColor.a = if (shadowDrawn) inShadowOpacityBtn.value else 0f
                updateFontStyle()
            }
            addEnumBtn("In shadow color", SHADOW_COLORS, SHADOW_COLOR_NAMES, 1) { _, color ->
                val shadowColor = color.cpy()
                shadowColor.a = fontStyle.innerShadowColor.a
                fontStyle.innerShadowColor = shadowColor
                updateFontStyle()
            }
            inShadowOpacityBtn = addValueBtn("In shadow opacity", 0f, 1f, 1f, -0.1f,
                    NumberFormat.getPercentInstance()) { _, opacity, _ ->
                fontStyle.innerShadowColor.a = opacity
                updateFontStyle()
            }
            addValueBtn("In shadow range", 0f, 0.5f, 0.3f, 0.1f) { _, range, _ ->
                fontStyle.innerShadowRange = range
                updateFontStyle()
            }
        }

        btnTable.add().grow().row()  // For aligning button to the top
    }

    override fun setStage(stage: Stage?) {
        super.setStage(stage)
        stage?.debugColor?.set(fontStyle.color)
        stage?.scrollFocus = textScrollPane
    }

    private fun updateFontStyle() {
        for ((i, label) in labels.withIndex()) {
            val style = FontStyle(fontStyle)
            style.size = SIZES[i]
            label.fontStyle = style
        }
    }


    class TestLayoutStyle {
        lateinit var background: Drawable
    }

    companion object {
        private val SIZES = listOf(
                10f, 12f, 14f, 16f, 18f, 20f,
                24f, 28f, 32f, 36f, 40f,
                50f, 60f, 70f, 80f, 90f, 100f,
                128f, 196f, 256f, 384f, 512f)

        private val TEXTS = listOf("The quick brown fox jumps over the lazy dog",
                "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz",
                "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýÿŸþŽžŠšµŒœ",
                "!\"#\$%&'()*+,-./:;<=>?[\\]^_`{|}~¡¢£€¥§©ª«¬®¯°±²³¶·¹º»¿×")

        private val FONT_NAMES = listOf("roboto-16", "roboto-24", "roboto-32",
                "roboto-40", "roboto-40-sdf", "roboto-bold-40")

        private val FONT_COLORS = listOf(Color.BLACK, Color.WHITE, Color.BLUE, Color.RED)
        private val FONT_COLOR_NAMES = listOf("black", "white", "blue", "red")

        private val BG_COLORS = listOf(Color.WHITE, Color.BLACK, Color.YELLOW, Color.CYAN)
        private val BG_COLOR_NAMES = listOf("white", "black", "yellow", "cyan")

        private val SHADOW_COLORS = listOf(Color.WHITE, Color.BLACK, Color.YELLOW, Color.CYAN)
        private val SHADOW_COLOR_NAMES = listOf("white", "black", "yellow", "cyan")
    }

}
