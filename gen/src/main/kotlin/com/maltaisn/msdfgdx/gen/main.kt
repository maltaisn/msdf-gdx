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
import java.io.File


fun main(args: Array<String>) {
    val params = Parameters()
    val commander = JCommander.newBuilder().addObject(params).build()
    commander.parse(*args)

    if (params.help) {
        // Show help message
        commander.usage()
        return
    }

    val message = params.validate()
    if (message != null) {
        // Invalid argument, show message
        println("ERROR: $message")
    } else if (!params.help) {
        // Generate font files
        val font = Font.createFont(Font.TRUETYPE_FONT, File(params.params[0])).deriveFont(params.fontSize)
        TODO()
    }
}
