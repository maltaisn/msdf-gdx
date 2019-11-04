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

import com.beust.jcommander.JCommander
import java.awt.Font
import java.awt.FontFormatException
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import java.awt.geom.GeneralPath
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.ceil
import kotlin.math.roundToInt


fun main(args: Array<String>) {
    val params = Parameters()
    val commander = JCommander.newBuilder().addObject(params).build()

    // Parse arguments
    try {
        commander.parse(*args)
    } catch (e: com.beust.jcommander.ParameterException) {
        println("ERROR: ${e.message}")
        return
    }

    if (params.help) {
        // Show help message
        commander.usage()
        return
    }

    // Validate arguments
    try {
        params.validate()
    } catch (e: ParameterException) {
        println("ERROR: ${e.message}")
        return
    }

    // Load font file
    val font = try {
        Font.createFont(Font.TRUETYPE_FONT, File(params.params[0])).deriveFont(params.fontSize.toFloat())
    } catch (e: FontFormatException) {
        println("ERROR: Could not load font file: ${e.message}")
        return
    }
    val fontRenderContext = FontRenderContext(AffineTransform(), true, true)

    // Create temp directory for outputting glyph images
    val tempDir = File(params.outputDir, "glyphs")
    tempDir.mkdirs()

    // Create font glyph objects
    val pad = params.distanceRange / 2f
    val glyphs = mutableListOf<FontGlyph>()
    for ((i, char) in params.charList.withIndex()) {
        val glyph = FontGlyph(char)
        glyphs += glyph

        // Create glyph vector and get its bounding box.
        val glyphVector = font.createGlyphVector(fontRenderContext, char.toString())
        val bounds = glyphVector.visualBounds

        if (bounds.width > 0.0 && bounds.height > 0.0) {
            glyph.width = ceil(bounds.width + pad * 2).toFloat()
            glyph.height = ceil(bounds.height + pad * 2).toFloat()

            val w = glyph.width.roundToInt()
            val h = glyph.height.roundToInt()

            // Get glyph path and translate it to center it.
            val path = glyphVector.outline as GeneralPath
            path.transform(AffineTransform.getTranslateInstance(
                    -bounds.x + pad, -bounds.y - bounds.height - pad))

            // Generate main glyph image.
            val gen = MsdfGen(params.msdfgen, w, h, params.distanceRange, Shape.fromPath(path).toString())
            val glyphImage = gen.generateImage(params.fieldType)
            glyph.image = glyphImage
            glyph.channels = FontGlyph.CHANNELS_RGB

            if (params.alphaFieldType != "none") {
                // Generate glyph image used for alpha layer.
                // Then keep RGB channel of glyph image and use red channel of alpha image as alpha channel.
                val alphaImage = gen.generateImage(params.alphaFieldType)
                val glyphPixels = glyphImage.getRGB(0, 0, w, h, null, 0, w)
                val alphaPixels = alphaImage.getRGB(0, 0, w, h, null, 0, w)
                for (j in glyphPixels.indices) {
                    glyphPixels[j] = (glyphPixels[j] and 0x00FFFFFF) or (alphaPixels[j] and 0xFF shl 24)
                }
                glyphImage.setRGB(0, 0, w, h, glyphPixels, 0, w)
                glyph.channels = FontGlyph.CHANNELS_RGBA
            }

            // Test output
            ImageIO.write(glyphImage, "png", File(tempDir, "${char.toInt()}.png"))

            // Show progress
            println("${i + 1} / ${params.charList.length}")

        } else {
            // Blank character, no image to output.
            println("Char '$char' (${char.toInt()}) is blank.")
        }
    }
}
