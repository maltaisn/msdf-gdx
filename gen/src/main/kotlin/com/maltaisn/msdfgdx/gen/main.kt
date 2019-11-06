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
import com.maltaisn.msdfgdx.gen.BMFontGenerator.GenerationStep
import java.io.File
import java.text.DecimalFormat
import kotlin.math.floor
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    val params = Parameters()
    val commander = JCommander.newBuilder().addObject(params).build()
    commander.programName = "msdfgen-bmfont"

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

        // Generate bitmap fonts
        val numberFmt = DecimalFormat.getInstance()
        numberFmt.minimumFractionDigits = 1
        numberFmt.maximumFractionDigits = 1

        for (fontPath in params.params) {
            val startTime = System.currentTimeMillis()
            var stepStartTime = 0L

            val fontFile = File(fontPath)
            val bmfont = BMFontGenerator(fontFile, params)

            println("Generating distance field font for '${fontFile.name}'.")

            var lastPercentProgress = -1
            bmfont.generate { step, progress ->
                val percentProgress = floor(progress * 100).toInt()
                if (percentProgress != lastPercentProgress) {
                    if (percentProgress == 0) stepStartTime = System.currentTimeMillis()
                    val stepDurationStr = numberFmt.format((System.currentTimeMillis() - stepStartTime) / 1000.0)

                    val stepName = when (step) {
                        GenerationStep.GLYPH -> "Generating glyph images"
                        GenerationStep.PACK -> "Packing glyphs into atlas"
                        GenerationStep.FONT_FILE -> "Generating BMFont file"
                    }.padEnd(30, ' ')

                    // Print step name, show progress bar and percent progress.
                    print("\r$stepName[${"#".repeat((progress * 40).toInt())}" +
                            "${"-".repeat((40 - progress * 40).toInt())}] " +
                            "  $percentProgress % ($stepDurationStr s)")

                    if (percentProgress == 100) println()  // New line for next step
                    lastPercentProgress = percentProgress
                }
            }

            // Done generating font, show duration.
            val durationStr = numberFmt.format((System.currentTimeMillis() - startTime) / 1000.0)
            println("DONE in $durationStr s\n")
        }

        exitProcess(0)

    } catch (e: ParameterException) {
        println("ERROR: ${e.message}\n")
        commander.usage()
        exitProcess(1)
    }
}
