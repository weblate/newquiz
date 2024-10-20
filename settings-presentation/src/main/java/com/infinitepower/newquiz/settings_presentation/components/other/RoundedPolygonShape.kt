package com.infinitepower.newquiz.settings_presentation.components.other

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Shape describing star with rounded corners
 *
 * Note: The shape draws within the minimum of provided width and height so can't be used to create stretched shape.
 *
 * @param sides number of sides.
 * @param curve a double value between 0.0 - 1.0 for modifying star curve.
 * @param rotation  value between 0 - 360
 * @param iterations a value between 0 - 360 that determines the quality of star shape.
 * @author pz64
 */
class RoundedPolygonShape(
    private val sides: Int,
    private val curve: Double = 0.09,
    private val rotation: Float = 0f,
    private val iterations: Int = 360
) : Shape {

    private companion object {
        const val TWO_PI = 2 * PI
    }

    private val steps = (TWO_PI) / min(iterations, 360)
    private val rotationDegree = (PI / 180) * rotation

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline = Outline.Generic(Path().apply {
        val r = min(size.height, size.width) * 0.4 * mapRange(1.0, 0.0, 0.5, 1.0, curve)

        val xCenter = size.width * .5f
        val yCenter = size.height * .5f

        moveTo(xCenter, yCenter)

        var t = 0.0

        while (t <= TWO_PI) {
            val x = r * (cos(t - rotationDegree) * (1 + curve * cos(sides * t)))
            val y = r * (sin(t - rotationDegree) * (1 + curve * cos(sides * t)))
            lineTo((x + xCenter).toFloat(), (y + yCenter).toFloat())

            t += steps
        }

        val x = r * (cos(t - rotationDegree) * (1 + curve * cos(sides * t)))
        val y = r * (sin(t - rotationDegree) * (1 + curve * cos(sides * t)))
        lineTo((x + xCenter).toFloat(), (y + yCenter).toFloat())

    })

    private fun mapRange(a: Double, b: Double, c: Double, d: Double, x: Double): Double {
        return (x - a) / (b - a) * (d - c) + c
    }
}
