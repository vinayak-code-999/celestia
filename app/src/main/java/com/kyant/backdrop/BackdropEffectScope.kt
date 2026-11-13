package com.kyant.backdrop

import android.graphics.RuntimeShader
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

interface BackdropEffectScope : Density {
    val shape: Shape
    val size: Size
    val layoutDirection: LayoutDirection
    var padding: Float
    var renderEffect: RenderEffect?

    fun obtainRuntimeShader(key: String, shaderString: String): RuntimeShader
    fun effect(effect: (RenderEffect?) -> RenderEffect?)
}

internal abstract class BackdropEffectScopeImpl : BackdropEffectScope {
    private val runtimeShaders = mutableMapOf<String, RuntimeShader>()
    private var currentDensity: Density = Density(1f, 1f)

    override var size: Size = Size.Zero
    override var layoutDirection: LayoutDirection = LayoutDirection.Ltr
    override var padding: Float = 0f
    override var renderEffect: RenderEffect? = null

    override val density: Float get() = currentDensity.density
    override val fontScale: Float get() = currentDensity.fontScale

    fun update(drawScope: DrawScope): Boolean {
        var isChanged = false
        if (this.currentDensity != drawScope) {
            this.currentDensity = Density(drawScope.density, drawScope.fontScale)
            isChanged = true
        }
        if (this.size != drawScope.size) {
            this.size = drawScope.size
            isChanged = true
        }
        if (this.layoutDirection != drawScope.layoutDirection) {
            this.layoutDirection = drawScope.layoutDirection
            isChanged = true
        }
        return isChanged
    }

    override fun obtainRuntimeShader(key: String, shaderString: String): RuntimeShader {
        return runtimeShaders.getOrPut(key) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                RuntimeShader(shaderString)
            } else {
                throw UnsupportedOperationException("RuntimeShader requires Android 13+ (API 33).")
            }
        }
    }

    override fun effect(effect: (RenderEffect?) -> RenderEffect?) {
        renderEffect = effect(renderEffect)
    }

    fun reset() {
        runtimeShaders.clear()
        renderEffect = null
        padding = 0f
    }
}
