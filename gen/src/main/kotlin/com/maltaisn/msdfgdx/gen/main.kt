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
import java.awt.GraphicsEnvironment
import java.awt.geom.AffineTransform
import java.awt.geom.GeneralPath
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.ceil


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

    // Initialize graphics and environment
    System.setProperty("java.awt.headless", "true")  // Not sure if necessary
    val graphicsEnv = GraphicsEnvironment.getLocalGraphicsEnvironment()
    val fontGraphics = graphicsEnv.createGraphics(BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB))

    // Load font file
    val font = try {
        Font.createFont(Font.TRUETYPE_FONT, File(params.params[0])).deriveFont(params.fontSize.toFloat())
    } catch (e: FontFormatException) {
        println("ERROR: Could not load font file: ${e.message}")
        return
    }

    // Create temp directory for outputting glyph images
    val tempDir = File(params.outputDir, "glyphs")
    tempDir.mkdirs()

    // Create font glyph objects
    val pad = params.distanceRange / 2f
    val distRangeStr = params.distanceRange.toString()

    val glyphs = mutableListOf<FontGlyph>()
    for ((i, char) in params.charList.withIndex()) {
        val glyph = FontGlyph(char)
        glyphs += glyph

        // Create glyph vector and get its bounding box.
        val glyphVector = font.createGlyphVector(fontGraphics.fontRenderContext, char.toString())
        val bounds = glyphVector.visualBounds

        if (bounds.width > 0.0 && bounds.height > 0.0) {
            val file = tempDir.resolve("${char.toInt()}.png")
            glyph.file = file
            glyph.width = ceil(bounds.width + pad * 2).toFloat()
            glyph.height = ceil(bounds.height + pad * 2).toFloat()
            glyph.channels = FontGlyph.CHANNELS_RGB

            val widthStr = glyph.width.toInt().toString()
            val heightStr = glyph.height.toInt().toString()

            // Get glyph path and translate it to center it.
            val path = glyphVector.outline as GeneralPath
            path.transform(AffineTransform.getTranslateInstance(
                    -bounds.x + pad, -bounds.y - bounds.height - pad))
            val shapeDescr = Shape.fromPath(path).toString()

            // Base command
            val command = listOf(
                    params.msdfgen,
                    "-size", widthStr, heightStr,
                    "-pxrange", distRangeStr,
                    "-defineshape", shapeDescr)

            // Generate glyph image.
            ProcessBuilder().command(command +
                    listOf(params.fieldType, "-o", file.absolutePath))
                    .start().waitFor()

            if (params.alphaFieldType != "none") {
                // Generate glyph image used for alpha layer
                val alphaFile = tempDir.resolve("a${char.toInt()}.png")
                ProcessBuilder().command(command +
                        listOf(params.alphaFieldType, "-o", alphaFile.absolutePath))
                        .start().waitFor()

                // Merge two glyph textures
                val rgbImage = ImageIO.read(file)
                val alphaImage = ImageIO.read(alphaFile)
                val w = rgbImage.width
                val h = rgbImage.height

                // Change RGB image color model to RGBA.
                val mergedImage = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
                val graphics = mergedImage.createGraphics()
                graphics.drawImage(rgbImage, 0, 0, null)
                graphics.dispose()

                // Keep RGB channel of RGB image and use red channel of alpha image as alpha channel.
                val rgbPixels = rgbImage.getRGB(0, 0, w, h, null, 0, w)
                val alphaPixels = alphaImage.getRGB(0, 0, w, h, null, 0, w)
                for (j in rgbPixels.indices) {
                    rgbPixels[j] = (rgbPixels[j] and 0x00FFFFFF) or (alphaPixels[j] and 0xFF shl 24)
                }
                mergedImage.setRGB(0, 0, w, h, rgbPixels, 0, w)
                ImageIO.write(mergedImage, "png", file)
                alphaFile.delete()

                glyph.channels = FontGlyph.CHANNELS_RGBA
            }

            // Show progress
            println("${i + 1} / ${params.charList.length}")

        } else {
            // Blank character, no image to output.
            println("Char '$char' (${char.toInt()}) is blank.")
        }
    }
}
