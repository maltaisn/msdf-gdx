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
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import ktx.actors.onClick
import java.text.NumberFormat
import kotlin.math.round


/**
 * A table to add button to for the test layout.
 * Some special buttons can be added: toggle, enum and number value.
 */
class ButtonTable(skin: Skin) : Table(skin) {

    init {
        pad(20f)
    }


    fun addBtn(title: String, action: (MsdfButton) -> Unit): MsdfButton {
        val btn = MsdfButton(skin, title)
        btn.onClick { action(btn) }
        addBtn(btn)
        return btn
    }

    fun addToggleBtn(title: String, action: (MsdfButton, checked: Boolean) -> Unit): MsdfButton {
        val btn = addBtn(title) { action(it, it.checked) }
        btn.checkable = true
        return btn
    }

    fun <T> addEnumBtn(title: String, values: List<T>,
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

    fun addValueBtn(title: String,
                    minValue: Float, maxValue: Float, startValue: Float, step: Float,
                    numberFmt: NumberFormat? = NumberFormat.getInstance(),
                    action: (MsdfButton, Float, Float) -> Unit): ValueMsdfBtn {
        val btn = ValueMsdfBtn(skin, title, minValue, maxValue, startValue, step, numberFmt, action)
        addBtn(btn)
        return btn
    }

    class ValueMsdfBtn(skin: Skin, private val valueTitle: String,
                       minValue: Float, maxValue: Float, startValue: Float, step: Float,
                       private val numberFmt: NumberFormat? = NumberFormat.getInstance(),
                       private val action: (MsdfButton, value: Float, oldValue: Float) -> Unit) :
            MsdfButton(skin, null) {

        private val min = step * round(minValue / step)
        private val max = step * round(maxValue / step)

        var value = step * round(startValue / step)
            set(value) {
                val oldValue = field
                field = value
                if (field < min) field = max
                if (field > max) field = min
                updateTitle()
                action(this, field, oldValue)
            }

        init {
            addListener(object : ClickListener(Input.Buttons.LEFT) {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    value = step * round((value + step) / step)
                }
            })
            addListener(object : ClickListener(Input.Buttons.RIGHT) {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    value = step * round((value - step) / step)
                }
            })
            updateTitle()
        }

        private fun updateTitle() {
            title = if (numberFmt != null) "$valueTitle: ${numberFmt.format(value)}" else valueTitle
        }

    }

    private fun addBtn(btn: MsdfButton) {
        add(btn).growX().padBottom(15f).row()
    }

}
