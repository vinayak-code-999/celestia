package com.kyant.backdrop.backdrops

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Density as UnitDensity
import com.kyant.backdrop.Backdrop

@Stable
class LayerBackdrop internal constructor(
    internal val graphicsLayer: GraphicsLayer
) : Backdrop {
    internal var layerCoordinates: LayoutCoordinates? = null

    override val isCoordinatesDependent: Boolean
        get() = true

    override fun DrawScope.drawBackdrop(
        density: UnitDensity,
        coordinates: LayoutCoordinates?,
        layerBlock: (GraphicsLayerScope.() -> Unit)?
    ) {
        val targetCoords = layerCoordinates
        if (coordinates != null && targetCoords != null && coordinates.isAttached && targetCoords.isAttached) {
            val offset = coordinates.localPositionOf(targetCoords, androidx.compose.ui.geometry.Offset.Zero)
            drawContext.canvas.translate(-offset.x, -offset.y)
            drawLayer(graphicsLayer)
            drawContext.canvas.translate(offset.x, offset.y)
        } else {
            drawLayer(graphicsLayer)
        }
    }
}

@Composable
fun rememberLayerBackdrop(): LayerBackdrop {
    val graphicsLayer = rememberGraphicsLayer()
    return remember(graphicsLayer) { LayerBackdrop(graphicsLayer) }
}

fun Modifier.layerBackdrop(backdrop: LayerBackdrop): Modifier = this
    .onGloballyPositioned { coords ->
        if (coords.isAttached) {
            backdrop.layerCoordinates = coords
        }
    }
    .drawWithContent {
        backdrop.graphicsLayer.record {
            this@drawWithContent.drawContent()
        }
        drawContent()
    }
