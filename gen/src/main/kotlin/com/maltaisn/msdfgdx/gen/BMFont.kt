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

import com.badlogic.gdx.Files
import com.badlogic.gdx.backends.lwjgl.LwjglFileHandle
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.tools.hiero.Kerning
import com.badlogic.gdx.tools.texturepacker.TexturePacker
import java.awt.Font
import java.awt.FontFormatException
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import java.awt.geom.GeneralPath
import java.io.File
import kotlin.math.ceil


class BMFont(private val fontFile: File,
             private val params: Parameters) {

    private val font = try {
        Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(params.fontSize.toFloat())
    } catch (e: FontFormatException) {
        paramError("ERROR: Could not load font file: ${e.message}")
    }

    private val glyphs = mutableMapOf<Char, FontGlyph>()

    fun generate(progressListener: ProgressListener) {
        generateGlyphs(progressListener)
        pack(progressListener)
        generateFontFile(progressListener)
    }

    private fun generateGlyphs(progressListener: ProgressListener) {
        progressListener(GenerationStep.GLYPH, 0f)

        val fontRenderContext = FontRenderContext(AffineTransform(), true, true)
        val kernings = Kerning().apply { load(fontFile.inputStream(), params.fontSize) }.kernings

        val pad = params.distanceRange / 2f
        for ((i, char) in params.charList.withIndex()) {
            val glyph = FontGlyph(char)
            glyphs[char] = glyph

            // Set kerning distances
            for (other in params.charList) {
                val pair = (char.toInt() shl 16) or other.toInt()
                glyph.kernings[other] = kernings[pair, 0]
            }

            // Create glyph vector and get its bounding box.
            val glyphVector = font.createGlyphVector(fontRenderContext, char.toString())
            val bounds = glyphVector.visualBounds

            if (bounds.width > 0.0 && bounds.height > 0.0) {
                val w = ceil(bounds.width + pad * 2).toInt()
                val h = ceil(bounds.height + pad * 2).toInt()

                // Get glyph path and translate it to center it.
                val path = glyphVector.outline as GeneralPath
                path.transform(AffineTransform.getTranslateInstance(
                        -bounds.x + pad, -bounds.y - bounds.height - pad))

                // Generate main glyph image.
                val gen = MsdfGen(params.msdfgen, w, h, params.distanceRange, Shape.fromPath(path).toString())
                val glyphImage = gen.generateImage(params.fieldType)

                glyph.width = w
                glyph.height = h
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

                // Set progress
                progressListener(GenerationStep.GLYPH, i.toFloat() / params.charList.length)
            }
        }
    }

    /**
     * Pack the generate glyphs to a texture atlas.
     */
    private fun pack(progressListener: ProgressListener) {
        val packer = TexturePacker(File(params.outputDir), TexturePacker.Settings().apply {
            paddingX = params.padding
            paddingY = params.padding
            maxWidth = params.textureSize[0]
            maxHeight = params.textureSize[1]
            alias = false
            ignoreBlankImages = false
            silent = true
        })
        val atlasFile = File(params.outputDir, fontFile.nameWithoutExtension + ".atlas")

        progressListener(GenerationStep.PACK, 0f)
        packer.setProgressListener(object : TexturePacker.ProgressListener() {
            override fun progress(progress: Float) {
                progressListener(GenerationStep.PACK, progress)
            }
        })

        // Add glyph images to packer
        for ((char, glyph) in glyphs) {
            if (glyph.image != null) {
                packer.addImage(glyph.image, char.toInt().toString())
            }
        }

        // Remove any existing atlas related files.
        // This is necessary because otherwise the packer tries to add new textures to the existing atlas.
        atlasFile.delete()
        var atlasPageFile = File(params.outputDir, fontFile.nameWithoutExtension + ".png")
        var i = 2
        while (atlasPageFile.exists()) {
            atlasPageFile.delete()
            atlasPageFile = File(params.outputDir, fontFile.nameWithoutExtension + "$i.png")
            i++
        }

        // Pack to atlas
        packer.pack(File(params.outputDir), fontFile.nameWithoutExtension)

        // Set the glyph positions and page from the generated atlas file
        val atlasData = TextureAtlas.TextureAtlasData(LwjglFileHandle(atlasFile, Files.FileType.Absolute),
                LwjglFileHandle("", Files.FileType.Absolute), false)
        for (region in atlasData.regions) {
            val char = region.name.toInt().toChar()
            val glyph = glyphs[char] ?: continue
            glyph.page = atlasData.pages.indexOf(region.page, true)
            glyph.x = region.left
            glyph.y = region.top
        }

        // Delete atlas file, not used for BMFont.
        atlasFile.delete()
    }

    /**
     * Generate the font file from the generated `.atlas` file in the previous step.
     */
    private fun generateFontFile(progressListener: ProgressListener) {
        progressListener(GenerationStep.FONT_FILE, 0f)

        // TODO generate rest of font file.

        progressListener(GenerationStep.FONT_FILE, 1f)
    }

    enum class GenerationStep {
        GLYPH,
        PACK,
        FONT_FILE,
    }

}

typealias ProgressListener = (step: BMFont.GenerationStep, progress: Float) -> Unit
