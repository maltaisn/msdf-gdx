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

import com.beust.jcommander.Parameter
import java.io.File


class Parameters {

    @Parameter
    var params: List<String> = mutableListOf()

    @Parameter(names = ["-g", "--msdfgen"], description = "Path of the msdfgen executable.", order = 0)
    var msdfgen: String = "msdfgen.exe"

    @Parameter(names = ["-o", "--output"], description = "Name and path of generated font textures", order = 1)
    var output: String? = null

    @Parameter(names = ["-t", "--field-type"], description = "Field type: sdf | psdf | msdf", order = 2)
    var fieldType: String = "msdf"

    @Parameter(names = ["-a", "--alpha-field-type"], description = "Alpha field type: none | sdf | psdf", order = 3)
    var alphaFieldType: String = "sdf"

    @Parameter(names = ["-s", "--font-size"], description = "Font size for generated textures", order = 4)
    var fontSize: Float = 32f

    @Parameter(names = ["-r", "--distance-range"], description = "Distance range in which SDF is encoded", order = 5)
    var distanceRange: Float = 5f

    @Parameter(names = ["-m", "--texture-size"], arity = 2, description = "Width and height of generated textures", order = 6)
    var textureSize: List<Int> = listOf(512, 512)

    @Parameter(names = ["-p", "--glyph-padding"], description = "Padding between glyphs", order = 7)
    var glyphPadding: Float = 2f

    @Parameter(names = ["-b", "--border-padding"], description = "Padding on the texture border", order = 8)
    var borderPadding: Float = 2f

    @Parameter(names = ["-c", "--charset"], description = "File containing the characters to use", order = 9)
    var charset: String? = null

    @Parameter(names = ["-h", "--help"], help = true, order = 9)
    var help = false

    /**
     * Validate arguments
     */
    fun validate(): String? = when {
        help -> null
        params.isEmpty() -> "No input file."
        params.any { !File(it).exists() } -> "Input file doesn't exist."
        fieldType !in listOf("sdf", "psdf", "msdf") -> "Invalid field type: $fieldType"
        alphaFieldType !in listOf("none", "sdf", "psdf") -> "Invalid field type: $alphaFieldType"
        fontSize < 8 -> "Font size must be at least 8."
        distanceRange < 1 -> "Distance range must be at least 1."
        textureSize.any { d -> d !in VALID_TEXTURE_SIZES } -> "Texture size must be power of two between 32 and 65536."
        glyphPadding < 0 || borderPadding < 0 -> "Padding values must be at least 0."
        charset != null && !File(charset!!).exists() -> "Charset file doesn't exist."
        else -> null
    }

    companion object {
        private val VALID_TEXTURE_SIZES = List(12) { 1 shl (it + 5) }
    }

}
