package com.example.lairsheet.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
// Note: drawPath is provided implicitly by the Canvas draw scope; no explicit import is needed.
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Draws a regular polygon with the provided number of sides.  This is used as a
 * simple visual representation of polyhedral dice when dedicated SVG assets
 * aren't available.  The icon scales to fill its modifier.
 *
 * @param sides number of edges for the polygon (e.g., 4, 6, 8, 10, 12, 20)
 * @param modifier size and positioning information for this composable
 * @param color fill color of the polygon
 */
@Composable
fun DiceIcon(
    sides: Int,
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        if (sides < 3) return@Canvas
        val radius = min(size.width, size.height) / 2f
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val path = Path()
        for (i in 0 until sides) {
            val angle = (2f * Math.PI.toFloat() * i / sides) - (Math.PI.toFloat() / 2f)
            val x = centerX + radius * cos(angle)
            val y = centerY + radius * sin(angle)
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        drawPath(path = path, color = color)
    }
}