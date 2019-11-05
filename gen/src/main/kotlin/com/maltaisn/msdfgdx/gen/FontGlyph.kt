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

package com.maltaisn.msdfgdx.gen

import java.awt.image.BufferedImage


class FontGlyph {

    var image: BufferedImage? = null

    // Glyph position in atlas
    var page: Int = 0
    var x: Int = 0
    var y: Int = 0
    var width: Int = 0
    var height: Int = 0

    // Glyph metrics
    var xOffset: Int = 0
    var yOffset: Int = 0
    var xAdvance: Int = 0

    /** Bit field of texture channels where glyph is encoded. **/
    var channels: Int = CHANNELS_NONE

    /** Map of kerning distances in pixels between this character (first) and other characters (second). */
    val kernings = mutableMapOf<Char, Int>()


    companion object {
        const val CHANNELS_NONE = 0
        const val CHANNELS_RGB = 7
        const val CHANNELS_RGBA = 15
    }

}
