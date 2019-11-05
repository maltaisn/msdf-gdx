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

            val bmfont = BMFont(fontFile, params)
            var lastStep: BMFont.GenerationStep? = null
            bmfont.generate { step, progress ->
                val stepName = when (step) {
                    BMFont.GenerationStep.GLYPH -> "Generating glyph images"
                    BMFont.GenerationStep.PACK -> "Packing glyphs into atlas"
                    BMFont.GenerationStep.FONT_FILE -> "Generating BMFont file"
                }
                println("$stepName [${"#".repeat((progress * 40).toInt())}" +
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
