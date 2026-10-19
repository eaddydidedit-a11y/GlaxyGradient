package com.eadit.app

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface

/**
 * Draws one layer. Used by BOTH the live preview and the export, so what you see is what you get.
 * All sizes are relative to the frame width, so it looks the same at any resolution.
 */
object LayerRenderer {

    fun draw(
        canvas: Canvas,
        layer: Layer,
        frameW: Float,
        cx: Float,
        cy: Float,
        scale: Float,
        alpha: Float,
    ) {
        val a = (alpha.coerceIn(0f, 1f) * 255f).toInt()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = layer.colorArgb
            this.alpha = a
        }

        when (layer.type) {
            LayerType.TEXT -> {
                paint.textAlign = Paint.Align.CENTER
                paint.typeface = Typeface.DEFAULT_BOLD
                paint.textSize = frameW * 0.08f * scale
                paint.setShadowLayer(
                    frameW * 0.006f * scale,
                    0f,
                    frameW * 0.003f * scale,
                    Color.argb((a * 0.6f).toInt(), 0, 0, 0),
                )
                val baseline = cy - (paint.ascent() + paint.descent()) / 2f
                canvas.drawText(layer.text, cx, baseline, paint)
            }

            LayerType.RECT -> {
                val size = frameW * 0.25f * scale
                val r = size * 0.12f
                canvas.drawRoundRect(
                    cx - size / 2f, cy - size / 2f,
                    cx + size / 2f, cy + size / 2f,
                    r, r, paint,
                )
            }

            LayerType.CIRCLE -> {
                canvas.drawCircle(cx, cy, frameW * 0.12f * scale, paint)
            }
        }
    }
}
