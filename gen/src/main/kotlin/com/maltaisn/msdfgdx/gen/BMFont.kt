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
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.tools.hiero.Kerning
import com.badlogic.gdx.tools.texturepacker.TexturePacker
import com.googlecode.pngtastic.core.PngImage
import com.googlecode.pngtastic.core.PngOptimizer
import kotlinx.coroutines.*
import java.awt.Canvas
import java.awt.Font
import java.awt.FontFormatException
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import java.awt.geom.GeneralPath
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.ceil
import kotlin.math.roundToInt


/**
 * Generate bitmap font files from a TTF [fontFile] with some [params].
 * Generated files are the texture atlas pages and a `.fnt` file (AngelCode BMFont specs, libGDX format).
 *
 * Note that font and glyph metrics seems kinda off sometimes. I used:
 * - https://github.com/libgdx/libgdx/tree/aff6dd4a2622d64a62196111eb018d4699a19c8a/extensions/gdx-tools/src/com/badlogic/gdx/tools/hiero
 * - https://github.com/soimy/msdf-bmfont-xml/blob/ff3669c2bfffd06f29bacedcdf3f073379b45e7e/index.js
 * To guess which font metrics would work but it might not work for all fonts.
 */
class BMFont(private val fontFile: File,
             private val params: Parameters) {

    private val font = try {
        Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(params.fontSize.toFloat())
    } catch (e: FontFormatException) {
        paramError("ERROR: Could not load font file: ${e.message}")
    }

    private val glyphs = sortedMapOf<Char, FontGlyph>()

    private val fontMetrics = Canvas().getFontMetrics(font)
    private val fontRenderContext = FontRenderContext(AffineTransform(), true, true)

    private var atlasData: TextureAtlas.TextureAtlasData? = null


    fun generate(progressListener: ProgressListener) {
        generateGlyphs(progressListener)
        pack(progressListener)
        generateFontFile(progressListener)
        compress(progressListener)
    }


    private fun generateGlyphs(progressListener: ProgressListener) {
        progressListener(GenerationStep.GLYPH, 0f)
        val glyphsGenerated = AtomicInteger()

        val kernings = Kerning().apply { load(fontFile.inputStream(), params.fontSize) }.kernings

        val pad = params.distanceRange / 2f
        runBlocking {
            // Create a new async job for each glyph to generate and await them all.
            val jobs = mutableListOf<Deferred<*>>()
            for (char in params.charList) {
                jobs += GlobalScope.async {
                    // Create glyph vector and get its bounding box.
                    val glyphVector = font.createGlyphVector(fontRenderContext, char.toString())
                    val bounds = glyphVector.visualBounds

                    if (bounds.width > 0.0 && bounds.height > 0.0) {
                        // Character is printable
                        val glyph = FontGlyph()
                        glyphs[char] = glyph

                        // Set kerning distances
                        for (other in params.charList) {
                            val pair = (char.toInt() shl 16) or other.toInt()
                            glyph.kernings[other] = kernings[pair, 0]
                        }

                        val w = ceil(bounds.width + pad * 2).toInt()
                        val h = ceil(bounds.height + pad * 2).toInt()

                        // Get glyph path and translate it to center it.
                        val tx = -bounds.x + pad
                        val ty = -bounds.y - bounds.height - pad
                        val path = glyphVector.getGlyphOutline(0, tx.toFloat(), ty.toFloat()) as GeneralPath

                        glyph.xOffset = (bounds.x - pad).roundToInt()
                        glyph.yOffset = (fontMetrics.ascent + bounds.y - pad).roundToInt()
                        glyph.xAdvance = glyphVector.getGlyphMetrics(0).advanceX.roundToInt()

                        // Generate main glyph image.
                        val gen = MsdfGen(params.msdfgen, w, h, params.distanceRange, Shape.fromPath(path).toString())
                        val glyphImage = gen.generateImage(params.fieldType)

                        glyph.width = w
                        glyph.height = h
                        glyph.image = glyphImage
                        glyph.channels = FontGlyph.CHANNELS_RGB

                        if (params.hasAlphaChannel) {
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
                        progressListener(GenerationStep.GLYPH,
                                glyphsGenerated.incrementAndGet().toFloat() / params.charList.length)
                    }
                }
            }
            jobs.awaitAll()
            progressListener(GenerationStep.GLYPH, 1f)
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

            format = if (params.hasAlphaChannel) Pixmap.Format.RGBA8888 else Pixmap.Format.RGB888
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
            packer.addImage(glyph.image, char.toInt().toString())
        }

        // Remove any existing atlas related files.
        // This is necessary because otherwise the packer tries to add new textures to the existing atlas.
        File(params.outputDir, fontFile.nameWithoutExtension + ".atlas").delete()
        var pageIndex = 0
        while (true) {
            val pageFile = getTextureAtlasFile(pageIndex)
            if (!pageFile.exists()) break
            pageFile.delete()
            pageIndex++
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
        this.atlasData = atlasData

        // Delete atlas file, not used anymore.
        atlasFile.delete()
    }

    /**
     * Generate the font file from the generated `.atlas` file in the previous step.
     */
    private fun generateFontFile(progressListener: ProgressListener) {
        progressListener(GenerationStep.FONT_FILE, 0f)

        val bmfont = StringBuilder()
        val atlasData = checkNotNull(atlasData)

        // Info tag
        bmfont.appendln("info face=\"${font.fontName}\" size=${params.fontSize} " +
                "bold=${if (font.isBold) 1 else 0} italic=${if (font.isItalic) 1 else 0} " +
                "charset=\"\" unicode=1 stretchH=100 smooth=1 aa=1 padding=0,0,0,0 spacing=0,0 outline=0")

        // Common tag
        bmfont.appendln("common lineHeight=${fontMetrics.height} " +
                "base=${fontMetrics.ascent} scaleW=${params.textureSize[0]} " +
                "scaleH=${params.textureSize[1]} pages=${atlasData.pages.size} " +
                "packed=0 alphaChnl=0 redChnl=0 greenChnl=0 blueChnl=0 " +
                "distanceRange=${params.distanceRange}")

        // Page tags
        for (i in 0 until atlasData.pages.size) {
            bmfont.appendln("page id=$i file=\"${getTextureAtlasFile(i).name}\"")
        }

        val kerningsCount = glyphs.values.map { it.kernings.values.count { k -> k != 0 } }.sum()
        val elementsCount = glyphs.size + kerningsCount
        var elementsDone = 0f

        // Char tags
        bmfont.appendln("chars count=${glyphs.size}")
        val channels = if (params.hasAlphaChannel) FontGlyph.CHANNELS_RGB else FontGlyph.CHANNELS_RGBA
        for ((char, glyph) in glyphs) {
            bmfont.appendln("char id=${char.toInt()} x=${glyph.x} y=${glyph.y} " +
                    "width=${glyph.width} height=${glyph.height} " +
                    "xoffset=${glyph.xOffset} yoffset=${glyph.yOffset} " +
                    "xadvance=${glyph.xAdvance} page=${glyph.page} chnl=$channels")
            elementsDone++
            progressListener(GenerationStep.FONT_FILE, elementsDone / elementsCount)
        }

        // Kerning tags
        bmfont.appendln("kernings count=$kerningsCount")
        for ((char, glyph) in glyphs) {
            for ((other, kerning) in glyph.kernings) {
                if (kerning != 0) {
                    bmfont.appendln("kerning first=${char.toInt()} second=${other.toInt()} amount=$kerning")
                    elementsDone++
                    progressListener(GenerationStep.FONT_FILE, elementsDone / elementsCount)
                }
            }
        }

        // Write to file
        File(params.outputDir, fontFile.nameWithoutExtension + ".fnt")
                .writeText(bmfont.toString())
    }

    private fun compress(progressListener: ProgressListener) {
        if (params.compressionLevel == 0) {
            // No compression
            return
        }
        progressListener(GenerationStep.COMPRESS, 0f)

        val atlasData = checkNotNull(atlasData)
        val pngOptimizer = PngOptimizer()
        for (i in 0 until atlasData.pages.size) {
            val file = getTextureAtlasFile(i).absolutePath
            val inputStream = BufferedInputStream(FileInputStream(file))
            inputStream.use {
                val pngImage = PngImage(inputStream)
                pngImage.fileName = file
                pngOptimizer.optimize(pngImage, file, false, params.compressionLevel)
            }
            progressListener(GenerationStep.COMPRESS, (i + 1).toFloat() / atlasData.pages.size)
        }
    }


    private fun getTextureAtlasFile(pageIndex: Int) = File(params.outputDir, fontFile.nameWithoutExtension +
            (if (pageIndex == 0) "" else (pageIndex + 1).toString()) + ".png")


    enum class GenerationStep {
        GLYPH,
        PACK,
        FONT_FILE,
        COMPRESS
    }

}

typealias ProgressListener = (step: BMFont.GenerationStep, progress: Float) -> Unit
