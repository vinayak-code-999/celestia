package com.kyant.backdrop.internal

import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

internal class ShapeProvider(
    private val shapeBlock: () -> Shape
) {
    var innerShape: Shape = RectangleShape
        private set

    val shape: Shape = object : Shape {
        override fun createOutline(
            size: androidx.compose.ui.geometry.Size,
            layoutDirection: LayoutDirection,
            density: Density
        ): Outline {
            val shape = shapeBlock()
            innerShape = shape
            return shape.createOutline(size, layoutDirection, density)
        }
    }
}
