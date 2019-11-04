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
import java.io.BufferedReader
import java.io.InputStreamReader


class MsdfGen(private val msdfgen: String,
              private var width: Int,
              private var height: Int,
              private var distanceRange: Int,
              private var shapeDescr: String) {

    /**
     * Generate the glyph image for the specified shape, dimensions and distance range.
     * The output is an image with [BufferedImage.TYPE_INT_ARGB] format.
     */
    fun generateImage(fieldType: String): BufferedImage {
        val process = ProcessBuilder().command(
                msdfgen,
                "-format", "text",
                "-stdout",
                "-size", width.toString(), height.toString(),
                "-pxrange", distanceRange.toString(),
                "-defineshape", shapeDescr,
                fieldType)
                .start()

        // Convert text output to byte array.
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val pixels = IntArray(width * height)

        var line: String? = reader.readLine()
        val channels = (line!!.length + 1) / width / 3
        check(channels == 1 || channels == 3) { "msdfgen generated image with unsupported channels count." }

        // Output is lines of hex-encoded bytes separated by spaces, one for each image line.
        // There are either 1 or 3 bytes per pixel. Y coordinate must be reversed.
        var i = pixels.size
        while (line != null) {
            i -= width
            for (j in 0 until width) {
                // Build pixel width 3 consecutive bytes.
                val pos = j * channels * 3
                val r: Int
                val g: Int
                val b: Int
                if (channels == 1) {
                    r = line.substring(pos, pos + 2).toInt(16)
                    g = r
                    b = r
                } else {
                    r = line.substring(pos, pos + 2).toInt(16)
                    g = line.substring(pos + 3, pos + 5).toInt(16)
                    b = line.substring(pos + 6, pos + 8).toInt(16)
                }
                pixels[i + j] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
            line = reader.readLine()
        }

        // Build image from array of pixels.
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        image.setRGB(0, 0, width, height, pixels, 0, width)
        return image
    }

}
