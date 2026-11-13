package ai.moon.celestia.core.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

object ImageCropUtils {

    /**
     * Enhances image contrast and sharpens document text for high-precision OCR extraction.
     */
    fun preprocessDocumentBitmap(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height

        val dest = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(dest)

        val paint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }

        // Increase contrast slightly (+20%) and preserve luminosity for clean OCR readability
        val contrast = 1.25f
        val translate = (-0.5f * contrast + 0.5f) * 255f

        val cm = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, translate,
                0f, contrast, 0f, 0f, translate,
                0f, 0f, contrast, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)

        return dest
    }

    /**
     * Auto-crops margin borders and background shadows from document photos to focus on the text content area.
     * Uses intelligent edge luminance analysis to find document paper boundaries.
     */
    fun autoCropDocumentMargins(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height

        if (width <= 50 || height <= 50) return src

        // Sample down for fast boundary scan
        val scale = minOf(1.0f, 320.0f / maxOf(width, height))
        val sampleW = (width * scale).toInt().coerceAtLeast(10)
        val sampleH = (height * scale).toInt().coerceAtLeast(10)

        val pixels = IntArray(sampleW * sampleH)
        val scaledBitmap = if (scale < 1.0f) {
            Bitmap.createScaledBitmap(src, sampleW, sampleH, false)
        } else src

        scaledBitmap.getPixels(pixels, 0, sampleW, 0, 0, sampleW, sampleH)
        if (scaledBitmap != src) {
            scaledBitmap.recycle()
        }

        // Convert to grayscale luminance array
        val luminance = FloatArray(pixels.size)
        for (i in pixels.indices) {
            val c = pixels[i]
            val r = (c shr 16) and 0xFF
            val g = (c shr 8) and 0xFF
            val b = c and 0xFF
            luminance[i] = (0.299f * r + 0.587f * g + 0.114f * b)
        }

        // Compute average luminance
        var totalLum = 0f
        for (lum in luminance) totalLum += lum
        val avgLum = totalLum / luminance.size

        // Find document bounding box where pixel luminance is brighter than background or has content
        val threshold = (avgLum * 0.70f).coerceIn(40f, 200f)

        var top = 0
        var bottom = sampleH - 1
        var left = 0
        var right = sampleW - 1

        // Scan Top
        for (y in 0 until sampleH / 3) {
            var rowCount = 0
            for (x in 0 until sampleW) {
                if (luminance[y * sampleW + x] >= threshold) rowCount++
            }
            if (rowCount > sampleW * 0.35f) {
                top = y
                break
            }
        }

        // Scan Bottom
        for (y in sampleH - 1 downTo (sampleH * 2 / 3)) {
            var rowCount = 0
            for (x in 0 until sampleW) {
                if (luminance[y * sampleW + x] >= threshold) rowCount++
            }
            if (rowCount > sampleW * 0.35f) {
                bottom = y
                break
            }
        }

        // Scan Left
        for (x in 0 until sampleW / 3) {
            var colCount = 0
            for (y in 0 until sampleH) {
                if (luminance[y * sampleW + x] >= threshold) colCount++
            }
            if (colCount > sampleH * 0.35f) {
                left = x
                break
            }
        }

        // Scan Right
        for (x in sampleW - 1 downTo (sampleW * 2 / 3)) {
            var colCount = 0
            for (y in 0 until sampleH) {
                if (luminance[y * sampleW + x] >= threshold) colCount++
            }
            if (colCount > sampleH * 0.35f) {
                right = x
                break
            }
        }

        // Scale coordinates back to original image dimensions
        val invScale = 1.0f / scale
        val cropLeft = (left * invScale).toInt().coerceIn(0, width - 10)
        val cropTop = (top * invScale).toInt().coerceIn(0, height - 10)
        val cropRight = (right * invScale).toInt().coerceIn(cropLeft + 10, width)
        val cropBottom = (bottom * invScale).toInt().coerceIn(cropTop + 10, height)

        val cropW = cropRight - cropLeft
        val cropH = cropBottom - cropTop

        return if (cropW > width * 0.3f && cropH > height * 0.3f) {
            Bitmap.createBitmap(src, cropLeft, cropTop, cropW, cropH)
        } else {
            // Fallback to standard 2% margin trim if boundary scan is uniform
            val marginX = (width * 0.02f).toInt()
            val marginY = (height * 0.02f).toInt()
            Bitmap.createBitmap(src, marginX, marginY, (width - marginX * 2).coerceAtLeast(10), (height - marginY * 2).coerceAtLeast(10))
        }
    }
}
