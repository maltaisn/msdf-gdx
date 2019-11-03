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

import java.awt.geom.AffineTransform
import java.awt.geom.GeneralPath
import java.awt.geom.PathIterator


class Shape(private val contours: List<Contour>) {
    override fun toString() = contours.joinToString(" ")

    companion object {
        fun fromPath(path: GeneralPath): Shape {
            // Iterate path and build the contours and edges from it.
            val contours = mutableListOf<Contour>()
            val iterator = path.getPathIterator(AffineTransform())
            val points = mutableListOf<Point>()
            val coords = FloatArray(6)
            while (!iterator.isDone) {
                when (iterator.currentSegment(coords)) {
                    PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO -> points += LinearPoint(coords[0], -coords[1])
                    PathIterator.SEG_QUADTO -> points += QuadraticPoint(coords[0], -coords[1], coords[2], -coords[3])
                    PathIterator.SEG_CUBICTO -> points += CubicPoint(coords[0], -coords[1], coords[2], -coords[3], coords[4], -coords[5])
                    PathIterator.SEG_CLOSE -> {
                        contours += Contour(points.toList())
                        points.clear()
                    }
                }
                iterator.next()
            }
            return Shape(contours)
        }
    }
}

class Contour(private val points: List<Point>) {
    override fun toString() = "{ ${points.joinToString("; ")} }"
}

interface Point

class LinearPoint(val x: Float, val y: Float) : Point {
    override fun toString() = "$x, $y"
}

class QuadraticPoint(val x1: Float, val y1: Float,
                     val x2: Float, val y2: Float) : Point {
    override fun toString() = "($x1, $y1); $x2, $y2"
}

class CubicPoint(val x1: Float, val y1: Float,
                 val x2: Float, val y2: Float,
                 val x3: Float, val y3: Float) : Point {
    override fun toString() = "($x1, $y1; $x2, $y2); $x3, $y3"
}
