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
    val graphics = graphicsEnv.createGraphics(BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB))

    // Load font file
    val font = try {
        Font.createFont(Font.TRUETYPE_FONT, File(params.params[0])).deriveFont(params.fontSize.toFloat())
    } catch (e: FontFormatException) {
        println("ERROR: Could not load font file: ${e.message}")
        return
    }

    // Output MSDF images for each printable glyph.
    var i = 0
    for (char in params.charList) {
        // Create glyph vector and get its bounding box.
        val glyph = font.createGlyphVector(graphics.fontRenderContext, char.toString())
        val bounds = glyph.visualBounds

        if (bounds.width > 0.0 && bounds.height > 0.0) {
            val pad = params.glyphPadding
            val width = ceil(bounds.width + pad * 2).toInt().toString()
            val height = ceil(bounds.height + pad * 2).toInt().toString()
            val distRange = params.distanceRange.toString()

            // Get glyph path and translate it to center it.
            val path = glyph.outline as GeneralPath
            path.transform(AffineTransform.getTranslateInstance(
                    -bounds.x + pad, -bounds.y - bounds.height - pad))
            val shapeDescr = Shape.fromPath(path).toString()

            // Generate MSDF glyph with msdfgen.
            val pb = ProcessBuilder()
            val outputPath = File(params.outputDir, "${char.toInt()}.png").absolutePath
            pb.command(params.msdfgen,
                    "-o", outputPath,
                    "-size", width, height,
                    "-pxrange", distRange,
                    "-defineshape", shapeDescr)
            pb.start().waitFor()

            // Show progress
            i++
            println("$i / ${params.charList.length}")

        } else {
            // Blank character, no image to output.
            println("Char '$char' (${char.toInt()}) is blank.")
        }
    }
}
