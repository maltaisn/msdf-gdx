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
import java.io.File
import kotlin.math.floor
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    val params = Parameters()
    val commander = JCommander.newBuilder().addObject(params).build()

    try {
        // Parse arguments
        try {
            commander.parse(*args)
        } catch (e: com.beust.jcommander.ParameterException) {
            paramError(e.message)
        }

        if (params.help) {
            // Show help message
            commander.usage()
            exitProcess(1)
        }

        // Validate arguments
        params.validate()

        // Generate BMFonts
        for (fontPath in params.params) {
            val fontFile = File(fontPath)
            println("Generating distance field font for '${fontFile.name}'.")

            val bmfont = BMFontGenerator(fontFile, params)
            var lastStep: BMFontGenerator.GenerationStep? = null
            bmfont.generate { step, progress ->
                print(if (step != lastStep) "\r" else "\n")
                val stepName = when (step) {
                    BMFontGenerator.GenerationStep.GLYPH -> "Generating glyph images"
                    BMFontGenerator.GenerationStep.PACK -> "Packing glyphs into atlas"
                    BMFontGenerator.GenerationStep.FONT_FILE -> "Generating BMFont file"
                }
                print("$stepName [${"#".repeat((progress * 40).toInt())}" +
                        "${"-".repeat((40 - progress * 40).toInt())}]" +
                        " ${floor(progress * 100).toInt()}%")
                lastStep = step
            }

            println("DONE\n")
        }
        exitProcess(0)

    } catch (e: ParameterException) {
        println("ERROR: ${e.message}")
        exitProcess(1)
    }
}
